package com.fundoonotes.fundoo_notes.service;

import com.fundoonotes.fundoo_notes.dto.LabelDTO;
import com.fundoonotes.fundoo_notes.dto.LabelResponseDTO;
import com.fundoonotes.fundoo_notes.model.Note;

import java.util.List;

public interface LabelService {

    LabelResponseDTO createLabel(LabelDTO dto, String email);

    List<LabelResponseDTO> getAllLabels(String email);

    LabelResponseDTO updateLabel(Long labelId, LabelDTO dto, String email);

    String deleteLabel(Long labelId, String email);

    String addLabelToNote(Long noteId, Long labelId, String email);

    String removeLabelFromNote(Long noteId, Long labelId, String email);

    List<Note> getNotesByLabel(Long labelId, String email);
}