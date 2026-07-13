package com.fundoonotes.fundoo_notes.service.impl;

import com.fundoonotes.fundoo_notes.dto.LabelResponseDTO;
import com.fundoonotes.fundoo_notes.dto.NoteDTO;
import com.fundoonotes.fundoo_notes.dto.NoteResponseDTO;
import com.fundoonotes.fundoo_notes.dto.ReminderDTO;
import com.fundoonotes.fundoo_notes.model.Collaborator;
import com.fundoonotes.fundoo_notes.model.Note;
import com.fundoonotes.fundoo_notes.model.User;
import com.fundoonotes.fundoo_notes.repository.CollaboratorRepository;
import com.fundoonotes.fundoo_notes.repository.NoteRepository;
import com.fundoonotes.fundoo_notes.repository.UserRepository;
import com.fundoonotes.fundoo_notes.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    // CONVERT NOTE TO RESPONSE DTO
    private NoteResponseDTO toDTO(Note note) {
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
        List<LabelResponseDTO> labelDTOs = note.getLabels()
                .stream()
                .map(label -> new LabelResponseDTO(label.getId(), label.getName()))
                .collect(Collectors.toList());
        dto.setLabels(labelDTOs);
        return dto;
    }

    // GET LOGGED IN USER
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // GET NOTE BELONGING TO USER (owner only — for destructive/organizing actions)
    private Note getNoteOfUser(Long noteId, User user) {
        return noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new RuntimeException("Note not found or you don't have permission"));
    }

    // GET NOTE EDITABLE BY USER — owner OR a collaborator with WRITE permission
    private Note getEditableNote(Long noteId, User user) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (note.getUser().getId().equals(user.getId())) {
            return note;
        }

        Collaborator collaborator = collaboratorRepository
                .findByNoteAndUser(note, user)
                .orElseThrow(() -> new RuntimeException("Note not found or you don't have permission"));

        if (collaborator.getPermission() != Collaborator.Permission.WRITE) {
            throw new RuntimeException("You only have read access to this note");
        }

        return note;
    }

    @Override
    public NoteResponseDTO createNote(NoteDTO dto, String email) {
        User user = getUser(email);
        Note note = new Note();
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setColor(dto.getColor() != null ? dto.getColor() : "#FFFFFF");
        note.setUser(user);
        return toDTO(noteRepository.save(note));
    }

    @Override
    public List<NoteResponseDTO> getAllNotes(String email) {
        User user = getUser(email);
        return noteRepository
                .findByUserAndIsTrashedFalseAndIsArchivedFalse(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NoteResponseDTO updateNote(Long noteId, NoteDTO dto, String email) {
        User user = getUser(email);
        Note note = getEditableNote(noteId, user);
        if (dto.getTitle() != null) note.setTitle(dto.getTitle());
        if (dto.getContent() != null) note.setContent(dto.getContent());
        if (dto.getColor() != null) note.setColor(dto.getColor());
        return toDTO(noteRepository.save(note));
    }

    @Override
    public String deleteNote(Long noteId, String email) {
        User user = getUser(email);
        Note note = getNoteOfUser(noteId, user);
        noteRepository.delete(note);
        return "Note deleted successfully";
    }

    @Override
    public String togglePin(Long noteId, String email) {
        User user = getUser(email);
        Note note = getNoteOfUser(noteId, user);
        note.setPinned(!note.isPinned());
        noteRepository.save(note);
        return note.isPinned() ? "Note pinned" : "Note unpinned";
    }

    @Override
    public String toggleArchive(Long noteId, String email) {
        User user = getUser(email);
        Note note = getNoteOfUser(noteId, user);
        note.setArchived(!note.isArchived());
        noteRepository.save(note);
        return note.isArchived() ? "Note archived" : "Note unarchived";
    }

    @Override
    public String toggleTrash(Long noteId, String email) {
        User user = getUser(email);
        Note note = getNoteOfUser(noteId, user);
        note.setTrashed(!note.isTrashed());
        noteRepository.save(note);
        return note.isTrashed() ? "Note moved to trash" : "Note restored";
    }

    @Override
    public List<NoteResponseDTO> getPinnedNotes(String email) {
        User user = getUser(email);
        return noteRepository
                .findByUserAndIsPinnedTrueAndIsTrashedFalse(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteResponseDTO> getArchivedNotes(String email) {
        User user = getUser(email);
        return noteRepository
                .findByUserAndIsArchivedTrueAndIsTrashedFalse(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteResponseDTO> getTrashedNotes(String email) {
        User user = getUser(email);
        return noteRepository
                .findByUserAndIsTrashedTrue(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteResponseDTO> searchNotes(String keyword, String email) {
        User user = getUser(email);
        return noteRepository.searchNotes(user, keyword)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteResponseDTO> filterByColor(String color, String email) {
        User user = getUser(email);
        return noteRepository
                .findByUserAndColorAndIsTrashedFalseAndIsArchivedFalse(user, color)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NoteResponseDTO setReminder(Long noteId, ReminderDTO dto, String email) {
        User user = getUser(email);
        Note note = getNoteOfUser(noteId, user);
        if (dto.getReminderTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reminder time must be in the future");
        }
        note.setReminder(dto.getReminderTime());
        return toDTO(noteRepository.save(note));
    }

    @Override
    public NoteResponseDTO removeReminder(Long noteId, String email) {
        User user = getUser(email);
        Note note = getNoteOfUser(noteId, user);
        note.setReminder(null);
        return toDTO(noteRepository.save(note));
    }
}