package com.fundoonotes.fundoo_notes.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor



public class CollaboratorResponseDTO {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String permission;






}
