package com.statikk.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    private LocalDateTime timestamp;
}
