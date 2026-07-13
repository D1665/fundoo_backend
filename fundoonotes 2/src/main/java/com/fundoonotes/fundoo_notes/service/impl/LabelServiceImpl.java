package com.fundoonotes.fundoo_notes.service.impl;

import com.fundoonotes.fundoo_notes.dto.LabelDTO;
import com.fundoonotes.fundoo_notes.dto.LabelResponseDTO;
import com.fundoonotes.fundoo_notes.model.Label;
import com.fundoonotes.fundoo_notes.model.Note;
import com.fundoonotes.fundoo_notes.model.User;
import com.fundoonotes.fundoo_notes.repository.LabelRepository;
import com.fundoonotes.fundoo_notes.repository.NoteRepository;
import com.fundoonotes.fundoo_notes.repository.UserRepository;
import com.fundoonotes.fundoo_notes.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabelServiceImpl implements LabelService {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private LabelResponseDTO toDTO(Label label) {
        return new LabelResponseDTO(label.getId(), label.getName());
    }

    @Override
    public LabelResponseDTO createLabel(LabelDTO dto, String email) {
        User user = getUser(email);
        if (labelRepository.existsByNameAndUser(dto.getName(), user)) {
            throw new RuntimeException("Label with this name already exists");
        }
        Label label = new Label();
        label.setName(dto.getName());
        label.setUser(user);
        return toDTO(labelRepository.save(label));
    }

    @Override
    public List<LabelResponseDTO> getAllLabels(String email) {
        return labelRepository.findByUser(getUser(email))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LabelResponseDTO updateLabel(Long labelId, LabelDTO dto, String email) {
        User user = getUser(email);
        Label label = labelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> new RuntimeException("Label not found or you don't have permission"));
        if (labelRepository.existsByNameAndUser(dto.getName(), user)) {
            throw new RuntimeException("Label with this name already exists");
        }
        label.setName(dto.getName());
        return toDTO(labelRepository.save(label));
    }

    @Override
    public String deleteLabel(Long labelId, String email) {
        User user = getUser(email);
        Label label = labelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> new RuntimeException("Label not found or you don't have permission"));
        labelRepository.delete(label);
        return "Label deleted successfully";
    }

    @Override
    public String addLabelToNote(Long noteId, Long labelId, String email) {
        User user = getUser(email);
        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new RuntimeException("Note not found or you don't have permission"));
        Label label = labelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> new RuntimeException("Label not found or you don't have permission"));
        if (note.getLabels().contains(label)) {
            throw new RuntimeException("Label already added to this note");
        }
        note.getLabels().add(label);
        noteRepository.save(note);
        return "Label added to note successfully";
    }

    @Override
    public String removeLabelFromNote(Long noteId, Long labelId, String email) {
        User user = getUser(email);
        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new RuntimeException("Note not found or you don't have permission"));
        Label label = labelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> new RuntimeException("Label not found or you don't have permission"));
        note.getLabels().remove(label);
        noteRepository.save(note);
        return "Label removed from note successfully";
    }

    @Override
    public List<Note> getNotesByLabel(Long labelId, String email) {
        User user = getUser(email);
        labelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> new RuntimeException("Label not found or you don't have permission"));
        return noteRepository
                .findByUserAndLabels_IdAndIsTrashedFalse(user, labelId);
    }
}