package com.statikk.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenValidationResponse {
    private boolean valid;
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String message;
}
