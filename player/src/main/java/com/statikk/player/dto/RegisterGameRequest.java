package com.statikk.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterGameRequest {

    @NotBlank(message = "Game name cannot be empty")
    @Size(min = 1, max = 100, message = "Game name must be between 1 and 100 characters")
    private String gameName;

    private String genre;
    private String platform;
}
