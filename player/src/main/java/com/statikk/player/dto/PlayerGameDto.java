package com.statikk.player.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerGameDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String gameName;
    private String genre;
    private String platform;
    private LocalDateTime registeredAt;
}
