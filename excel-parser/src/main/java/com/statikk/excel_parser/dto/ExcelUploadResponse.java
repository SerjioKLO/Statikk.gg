package com.statikk.excel_parser.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelUploadResponse {
    private String filename;
    private Long userId;
    private Long gameId;
    private Integer totalRowsParsed;
    private String status; // SUCCESS, REJECTED
    private String message;
    private LocalDateTime timestamp;
}
