package com.fundoonotes.fundoo_notes.dto;

import lombok.*;

import java.time.LocalDateTime;
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ReminderDTO {

    private LocalDateTime reminderTime;



}
