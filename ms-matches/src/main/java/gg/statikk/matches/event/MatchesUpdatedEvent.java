package gg.statikk.matches.event;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchesUpdatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long gameId;
    private String eventType; // IMPORTED, CREATED, UPDATED, DELETED
    private int matchCount;
    private LocalDateTime timestamp;
}
