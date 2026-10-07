package gg.statikk.matches.event;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelMatchesImportedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long gameId;
    private String filename;
    private List<ImportedMatchItem> matches;
    private LocalDateTime timestamp;
}
