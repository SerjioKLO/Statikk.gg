package com.statikk.excel_parser.service.impl;

import com.statikk.excel_parser.exception.InvalidFileException;
import com.statikk.excel_parser.service.ExcelValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
public class ExcelValidationServiceImpl implements ExcelValidationService {

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.ms-excel",                                          // .xls
            "text/csv",                                                          // .csv
            "text/plain",                                                        // .csv on some OS
            "application/csv",
            "application/octet-stream"                                           // fallback binary
    );

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".xlsx", ".csv", ".xls");

    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("El archivo proporcionado está vacío o no es válido");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidFileException("El nombre del archivo no puede estar vacío");
        }

        String lowerFilename = originalFilename.toLowerCase();
        boolean hasValidExtension = ALLOWED_EXTENSIONS.stream().anyMatch(lowerFilename::endsWith);
        if (!hasValidExtension) {
            throw new InvalidFileException("Extensión de archivo no permitida. Solo se aceptan .xlsx o .csv");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("MIME type rejected: {}", contentType);
            throw new InvalidFileException("Tipo de contenido (MIME) no admitido: " + contentType);
        }

        log.info("File validation passed for: {} (MIME: {})", originalFilename, contentType);
    }

    @Override
    public boolean isXlsx(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls"));
    }

    @Override
    public boolean isCsv(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".csv");
    }
}
