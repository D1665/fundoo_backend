package com.fundoonotes.fundoo_notes.controller;
import com.fundoonotes.fundoo_notes.exception.GlobalExceptionHandler;
import com.fundoonotes.fundoo_notes.service.NoteService;
import com.fundoonotes.fundoo_notes.dto.ApiResponse;
import com.fundoonotes.fundoo_notes.dto.NoteDTO;
import com.fundoonotes.fundoo_notes.dto.NoteResponseDTO;
import com.fundoonotes.fundoo_notes.dto.ReminderDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    private String getEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    // CREATE NOTE
    @PostMapping
    public ResponseEntity<ApiResponse> createNote(@Valid @RequestBody NoteDTO dto) {
        NoteResponseDTO note = noteService.createNote(dto, getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(201, "Note created successfully", note));
    }
    // GET ALL NOTES
    @GetMapping
    public ResponseEntity<ApiResponse> getAllNotes() {
        List<NoteResponseDTO> notes = noteService.getAllNotes(getEmail());
        return ResponseEntity.ok(
                new ApiResponse(200, "Notes fetched successfully", notes));
    }

    // UPDATE NOTE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateNote(@PathVariable Long id, @RequestBody NoteDTO dto) {
        NoteResponseDTO note = noteService.updateNote(id, dto, getEmail());
        return ResponseEntity.ok(
                new ApiResponse(200, "Note updated successfully", note));
    }

    // DELETE NOTE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteNote(@PathVariable Long id) {
        String message = noteService.deleteNote(id, getEmail());
        return ResponseEntity.ok(new ApiResponse(200, message));
    }

    // TOGGLE PIN
    @PatchMapping("/{id}/pin")
    public ResponseEntity<ApiResponse> togglePin(@PathVariable Long id) {
        String message = noteService.togglePin(id, getEmail());
        return ResponseEntity.ok(new ApiResponse(200, message));
    }

    // TOGGLE ARCHIVE
    @PatchMapping("/{id}/archive")
    public ResponseEntity<ApiResponse> toggleArchive(@PathVariable Long id) {
        String message = noteService.toggleArchive(id, getEmail());
        return ResponseEntity.ok(new ApiResponse(200, message));
    }

    // TOGGLE TRASH
    @PatchMapping("/{id}/trash")
    public ResponseEntity<ApiResponse> toggleTrash(@PathVariable Long id) {
        String message = noteService.toggleTrash(id, getEmail());
        return ResponseEntity.ok(new ApiResponse(200, message));

    }

    // GET PINNED NOTES
    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse> getPinnedNotes() {
        List<NoteResponseDTO> notes = noteService.getPinnedNotes(getEmail());
        return ResponseEntity.ok(
                new ApiResponse(200, "Pinned notes fetched", notes));
    }

    // GET ARCHIVED NOTES
    @GetMapping("/archived")
    public ResponseEntity<ApiResponse> getArchivedNotes() {
        List<NoteResponseDTO> notes = noteService.getArchivedNotes(getEmail());
        return ResponseEntity.ok(
                new ApiResponse(200, "Archived notes fetched", notes));
    }

    // GET TRASHED NOTES
    @GetMapping("/trash")
    public ResponseEntity<ApiResponse> getTrashedNotes() {
        List<NoteResponseDTO> notes = noteService.getTrashedNotes(getEmail());
        return ResponseEntity.ok(
                new ApiResponse(200, "Trashed notes fetched", notes));
    }

    // SEARCH NOTES
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchNotes(@RequestParam String keyword) {
        List<NoteResponseDTO> notes =
                noteService.searchNotes(keyword, getEmail());
        return ResponseEntity.ok(
                new ApiResponse(200, "Search results", notes));
    }

    // FILTER BY COLOR
    @GetMapping("/color")
    public ResponseEntity<ApiResponse> filterByColor(@RequestParam String color) {
        List<NoteResponseDTO> notes =
                noteService.filterByColor(color, getEmail());
        return ResponseEntity.ok(
                new ApiResponse(200, "Notes filtered by color", notes));
    }

    // SET REMINDER
    @PatchMapping("/{id}/reminder")
    public ResponseEntity<ApiResponse> setReminder(@PathVariable Long id, @RequestBody ReminderDTO dto) {
        NoteResponseDTO note = noteService.setReminder(id, dto, getEmail());
        return ResponseEntity.ok(
                new ApiResponse(200, "Reminder set successfully", note));
    }

    // REMOVE REMINDER
    @DeleteMapping("/{id}/reminder")
    public ResponseEntity<ApiResponse> removeReminder(@PathVariable Long id) {
        NoteResponseDTO note = noteService.removeReminder(id, getEmail());
        return ResponseEntity.ok(
                new ApiResponse(200, "Reminder removed", note));
    }
}