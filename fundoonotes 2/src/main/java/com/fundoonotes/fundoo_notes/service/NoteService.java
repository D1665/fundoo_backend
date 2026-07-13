package com.fundoonotes.fundoo_notes.service;

import com.fundoonotes.fundoo_notes.dto.NoteDTO;
import com.fundoonotes.fundoo_notes.dto.NoteResponseDTO;
import com.fundoonotes.fundoo_notes.dto.ReminderDTO;

import java.util.List;

public interface NoteService {

    NoteResponseDTO createNote(NoteDTO dto, String email);

    List<NoteResponseDTO> getAllNotes(String email);

    NoteResponseDTO updateNote(Long noteId, NoteDTO dto, String email);

    String deleteNote(Long noteId, String email);

    String togglePin(Long noteId, String email);

    String toggleArchive(Long noteId, String email);

    String toggleTrash(Long noteId, String email);

    List<NoteResponseDTO> getPinnedNotes(String email);

    List<NoteResponseDTO> getArchivedNotes(String email);

    List<NoteResponseDTO> getTrashedNotes(String email);

    List<NoteResponseDTO> searchNotes(String keyword, String email);

    List<NoteResponseDTO> filterByColor(String color, String email);

    NoteResponseDTO setReminder(Long noteId, ReminderDTO dto, String email);

    NoteResponseDTO removeReminder(Long noteId, String email);
}