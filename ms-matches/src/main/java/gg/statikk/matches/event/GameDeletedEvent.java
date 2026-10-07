package gg.statikk.matches.event;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameDeletedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long gameId;
    private String gameName;
    private LocalDateTime timestamp;
}
