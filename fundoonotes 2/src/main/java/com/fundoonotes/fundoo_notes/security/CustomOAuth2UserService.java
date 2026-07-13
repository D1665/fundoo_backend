package com.fundoonotes.fundoo_notes.security;

import com.fundoonotes.fundoo_notes.model.User;
import com.fundoonotes.fundoo_notes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Called by Spring Security after Google redirects back with the user's
 * profile. We either find the matching local User row or create a new
 * one (auto-verified, no password) and hand the profile back so the
 * success handler can issue our own JWT.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String googleId = oAuth2User.getAttribute("sub");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not available from Google");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFirstName(firstName != null ? firstName : "Google");
            user.setLastName(lastName != null ? lastName : "User");
            user.setPassword(null);
            user.setProvider("GOOGLE");
            user.setProviderId(googleId);
            user.setVerified(true); // Google already verified the email
            userRepository.save(user);
        } else if (!"GOOGLE".equals(user.getProvider())) {
            // A LOCAL account with this email already exists — link it
            user.setProvider("GOOGLE");
            user.setProviderId(googleId);
            user.setVerified(true);
            userRepository.save(user);
        }

        return oAuth2User;
    }
}
