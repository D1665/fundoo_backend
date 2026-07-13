package com.fundoonotes.fundoo_notes.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class NoteResponseDTO {

    private Long id;
    private String title;
    private String content;
    private String color;
    private boolean isPinned;
    private boolean isArchived;
    private boolean isTrashed;
    private LocalDateTime reminder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<LabelResponseDTO> labels;

    // Set only when this note is returned as part of a "shared with me" list
    private String ownerEmail;

    // Set only when this note is returned as part of a "shared with me" list
    private String myPermission;


}
