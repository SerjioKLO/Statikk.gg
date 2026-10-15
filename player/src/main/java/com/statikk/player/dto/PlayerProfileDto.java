package com.statikk.player.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerProfileDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String nickname;
    private String bio;
    private String avatarUrl;
    private String primaryRegion;
    private List<PlayerGameDto> games;
    private LocalDateTime createdAt;
}
