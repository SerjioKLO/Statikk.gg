package com.statikk.excel_parser.service;

import com.statikk.excel_parser.dto.ExcelUploadResponse;
import com.statikk.excel_parser.dto.MatchImportDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExcelParserService {
    ExcelUploadResponse processFile(MultipartFile file, Long userId, Long gameId);
    List<MatchImportDto> parseFile(MultipartFile file);
    byte[] generateTemplate();
}
