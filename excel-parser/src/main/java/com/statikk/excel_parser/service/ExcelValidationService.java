package com.statikk.excel_parser.service;

import org.springframework.web.multipart.MultipartFile;

public interface ExcelValidationService {
    void validateFile(MultipartFile file);
    boolean isXlsx(MultipartFile file);
    boolean isCsv(MultipartFile file);
}
