package com.fundoonotes.fundoo_notes.service.impl;

import com.fundoonotes.fundoo_notes.dto.CollaboratorDTO;
import com.fundoonotes.fundoo_notes.dto.CollaboratorResponseDTO;
import com.fundoonotes.fundoo_notes.dto.LabelResponseDTO;
import com.fundoonotes.fundoo_notes.dto.NoteResponseDTO;
import com.fundoonotes.fundoo_notes.model.Collaborator;
import com.fundoonotes.fundoo_notes.model.Note;
import com.fundoonotes.fundoo_notes.model.User;
import com.fundoonotes.fundoo_notes.repository.CollaboratorRepository;
import com.fundoonotes.fundoo_notes.repository.NoteRepository;
import com.fundoonotes.fundoo_notes.repository.UserRepository;
import com.fundoonotes.fundoo_notes.service.CollaboratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollaboratorServiceImpl implements CollaboratorService {

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // A note can only be shared/managed by its OWNER — not by an existing collaborator
    private Note getOwnedNote(Long noteId, String ownerEmail) {
        User owner = getUser(ownerEmail);
        return noteRepository.findByIdAndUser(noteId, owner)
                .orElseThrow(() -> new RuntimeException(
                        "Note not found or you are not the owner"));
    }

    private CollaboratorResponseDTO toDTO(Collaborator c) {
        return new CollaboratorResponseDTO(
                c.getId(),
                c.getUser().getEmail(),
                c.getUser().getFirstName(),
                c.getUser().getLastName(),
                c.getPermission().name()
        );
    }

    @Override
    public CollaboratorResponseDTO addCollaborator(Long noteId, CollaboratorDTO dto, String ownerEmail) {
        Note note = getOwnedNote(noteId, ownerEmail);

        if (dto.getEmail().equalsIgnoreCase(ownerEmail)) {
            throw new RuntimeException("You cannot add yourself as a collaborator");
        }

        User collaboratorUser = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "No registered user found with email: " + dto.getEmail()));

        Collaborator collaborator = collaboratorRepository
                .findByNoteAndUser(note, collaboratorUser)
                .orElse(new Collaborator());

        collaborator.setNote(note);
        collaborator.setUser(collaboratorUser);

        try {
            collaborator.setPermission(
                    Collaborator.Permission.valueOf(dto.getPermission().toUpperCase()));
        } catch (Exception e) {
            collaborator.setPermission(Collaborator.Permission.READ);
        }

        return toDTO(collaboratorRepository.save(collaborator));
    }

    @Override
    public String removeCollaborator(Long noteId, String collaboratorEmail, String ownerEmail) {
        Note note = getOwnedNote(noteId, ownerEmail);
        User collaboratorUser = userRepository.findByEmail(collaboratorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        collaboratorRepository.findByNoteAndUser(note, collaboratorUser)
                .orElseThrow(() -> new RuntimeException("This user is not a collaborator on this note"));

        collaboratorRepository.deleteByNoteAndUser(note, collaboratorUser);
        return "Collaborator removed successfully";
    }

    @Override
    public List<CollaboratorResponseDTO> getCollaborators(Long noteId, String requesterEmail) {
        User requester = getUser(requesterEmail);

        // Requester must be either the owner OR a collaborator on the note
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        boolean isOwner = note.getUser().getId().equals(requester.getId());
        boolean isCollaborator = collaboratorRepository
                .findByNoteAndUser(note, requester).isPresent();

        if (!isOwner && !isCollaborator) {
            throw new RuntimeException("You don't have access to this note");
        }

        return collaboratorRepository.findByNote(note)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteResponseDTO> getSharedNotes(String email) {
        User user = getUser(email);

        return collaboratorRepository.findByUser(user)
                .stream()
                .filter(c -> !c.getNote().isTrashed())
                .map(c -> {
                    Note note = c.getNote();
                    NoteResponseDTO dto = new NoteResponseDTO();
                    dto.setId(note.getId());
                    dto.setTitle(note.getTitle());
                    dto.setContent(note.getContent());
                    dto.setColor(note.getColor());
                    dto.setPinned(note.isPinned());
                    dto.setArchived(note.isArchived());
                    dto.setTrashed(note.isTrashed());
                    dto.setReminder(note.getReminder());
                    dto.setCreatedAt(note.getCreatedAt());
                    dto.setUpdatedAt(note.getUpdatedAt());
                    dto.setOwnerEmail(note.getUser().getEmail());
                    dto.setMyPermission(c.getPermission().name());
                    dto.setLabels(note.getLabels().stream()
                            .map(l -> new LabelResponseDTO(l.getId(), l.getName()))
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
