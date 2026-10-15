package com.statikk.excel_parser.controller;

import com.statikk.excel_parser.dto.ExcelUploadResponse;
import com.statikk.excel_parser.service.ExcelParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelParserController {

    private final ExcelParserService parserService;

    /**
     * RF-04: Carga Masiva de Estadísticas vía Excel (.xlsx / .csv)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExcelUploadResponse> uploadSpreadsheet(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("gameId") Long gameId) {
        log.info("RF-04: Received spreadsheet upload request: filename={}, userId={}, gameId={}",
                file.getOriginalFilename(), userId, gameId);
        ExcelUploadResponse response = parserService.processFile(file, userId, gameId);
        return ResponseEntity.ok(response);
    }

    /**
     * Download Excel template for user data entry
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] excelBytes = parserService.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statikk_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
