package com.fundoonotes.fundoo_notes.service;

import com.fundoonotes.fundoo_notes.dto.CollaboratorDTO;
import com.fundoonotes.fundoo_notes.dto.CollaboratorResponseDTO;
import com.fundoonotes.fundoo_notes.dto.NoteResponseDTO;

import java.util.List;

public interface CollaboratorService {

    // Only the note OWNER can add a collaborator
    CollaboratorResponseDTO addCollaborator(Long noteId, CollaboratorDTO dto, String ownerEmail);

    // Only the note OWNER can remove a collaborator
    String removeCollaborator(Long noteId, String collaboratorEmail, String ownerEmail);

    // Owner (or a collaborator on the note) can view the collaborator list
    List<CollaboratorResponseDTO> getCollaborators(Long noteId, String requesterEmail);

    // Notes that have been shared WITH the logged-in user
    List<NoteResponseDTO> getSharedNotes(String email);
}
