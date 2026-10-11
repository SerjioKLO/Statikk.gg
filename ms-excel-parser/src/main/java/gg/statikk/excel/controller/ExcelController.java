package gg.statikk.excel.controller;

import gg.statikk.excel.dto.ImportResponse;
import gg.statikk.excel.parser.TemplateGenerator;
import gg.statikk.excel.service.ExcelImportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ExcelImportService excelImportService;
    private final TemplateGenerator templateGenerator;

    public ExcelController(ExcelImportService excelImportService, TemplateGenerator templateGenerator) {
        this.excelImportService = excelImportService;
        this.templateGenerator = templateGenerator;
    }

    /** Pública (el gateway la deja pasar sin JWT): es solo el archivo vacío de ejemplo. */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"statikk-plantilla-partidas.xlsx\"")
                .contentType(XLSX)
                .body(templateGenerator.generate());
    }

    @PostMapping(value = "/matches/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResponse> importMatches(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long playerProfileId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(excelImportService.importMatches(userId, playerProfileId, file));
    }
}
