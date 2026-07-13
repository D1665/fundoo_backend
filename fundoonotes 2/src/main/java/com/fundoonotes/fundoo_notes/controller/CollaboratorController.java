package com.fundoonotes.fundoo_notes.controller;

import com.fundoonotes.fundoo_notes.dto.ApiResponse;
import com.fundoonotes.fundoo_notes.dto.CollaboratorDTO;
import com.fundoonotes.fundoo_notes.dto.CollaboratorResponseDTO;
import com.fundoonotes.fundoo_notes.dto.NoteResponseDTO;
import com.fundoonotes.fundoo_notes.service.CollaboratorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CollaboratorController {

    @Autowired
    private CollaboratorService collaboratorService;

    private String getEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    // ADD COLLABORATOR TO A NOTE (owner only)
    @PostMapping("/api/notes/{noteId}/collaborators")
    public ResponseEntity<ApiResponse> addCollaborator(@PathVariable Long noteId,
                                                       @Valid @RequestBody CollaboratorDTO dto) {
        CollaboratorResponseDTO collaborator = collaboratorService.addCollaborator(noteId, dto, getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(201, "Collaborator added successfully", collaborator));
    }

    // REMOVE COLLABORATOR FROM A NOTE (owner only)
    @DeleteMapping("/api/notes/{noteId}/collaborators/{email}")
    public ResponseEntity<ApiResponse> removeCollaborator(@PathVariable Long noteId, @PathVariable String email) {
        String message = collaboratorService.removeCollaborator(noteId, email, getEmail());
        return ResponseEntity.ok(new ApiResponse(200, message));
    }

    // LIST COLLABORATORS ON A NOTE (owner or existing collaborator)
    @GetMapping("/api/notes/{noteId}/collaborators")
    public ResponseEntity<ApiResponse> getCollaborators(@PathVariable Long noteId) {
        List<CollaboratorResponseDTO> collaborators = collaboratorService.getCollaborators(noteId, getEmail());
        return ResponseEntity.ok(
                new ApiResponse(200, "Collaborators fetched successfully", collaborators));
    }

    // NOTES SHARED WITH ME
    @GetMapping("/api/notes/shared")
    public ResponseEntity<ApiResponse> getSharedNotes() {
        List<NoteResponseDTO> notes = collaboratorService.getSharedNotes(getEmail());
        return ResponseEntity.ok(
                new ApiResponse(200, "Shared notes fetched successfully", notes));
    }
}