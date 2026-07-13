package com.fundoonotes.fundoo_notes.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ApiResponse {

    private int status;
    private String message;
    private Object data;

    // Constructor for error responses
    public ApiResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }



}