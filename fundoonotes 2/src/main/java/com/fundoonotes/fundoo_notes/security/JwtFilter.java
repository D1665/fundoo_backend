package com.fundoonotes.fundoo_notes.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // GET AUTHORIZATION HEADER
        String authHeader = request.getHeader("Authorization");

        String token = null;
        String email = null;

        // CHECK IF HEADER STARTS WITH "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            try {
                // STEP 0 — REJECT BLACKLISTED (LOGGED OUT) TOKENS
                Boolean isBlacklisted = redisTemplate.hasKey("BLACKLIST:" + token);
                if (Boolean.TRUE.equals(isBlacklisted)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // STEP 1 — CHECK REDIS CACHE FIRST
                String cachedEmail = redisTemplate.opsForValue()
                        .get("TOKEN:" + token);

                if (cachedEmail != null) {
                    // Token found in Redis cache — use it directly
                    // No need to parse JWT again — FASTER!
                    email = cachedEmail;
                } else {
                    // Token not in cache — parse JWT
                    email = jwtUtil.extractEmail(token);

                    if (email != null && jwtUtil.isTokenValid(token)) {
                        // STEP 2 — SAVE TO REDIS CACHE for next requests
                        // Expires in 24 hours same as JWT
                        redisTemplate.opsForValue().set(
                                "TOKEN:" + token,
                                email,
                                24,
                                TimeUnit.HOURS
                        );
                    }
                }
            } catch (Exception e) {
                // Invalid token — continue without setting auth
            }
        }

        // SET AUTHENTICATION IN SECURITY CONTEXT
        if (email != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            email, null, new ArrayList<>()
                    );
            authToken.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}