package com.statikk.excel_parser.service;

import com.statikk.excel_parser.exception.InvalidFileException;
import com.statikk.excel_parser.service.impl.ExcelValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcelValidationServiceTest {

    private ExcelValidationServiceImpl validationService;

    @BeforeEach
    void setUp() {
        validationService = new ExcelValidationServiceImpl();
    }

    @Test
    @DisplayName("RNF-02: Should pass validation for valid .xlsx file")
    void shouldValidateValidXlsx() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "matches.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "dummy content".getBytes()
        );

        validationService.validateFile(file);
        assertThat(validationService.isXlsx(file)).isTrue();
        assertThat(validationService.isCsv(file)).isFalse();
    }

    @Test
    @DisplayName("RNF-02: Should pass validation for valid .csv file")
    void shouldValidateValidCsv() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "matches.csv",
                "text/csv",
                "Result,Hours\nWIN,1.0".getBytes()
        );

        validationService.validateFile(file);
        assertThat(validationService.isCsv(file)).isTrue();
        assertThat(validationService.isXlsx(file)).isFalse();
    }

    @Test
    @DisplayName("RNF-02 Security Exception: Should throw InvalidFileException on unallowed extension")
    void shouldRejectExecutableFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.exe",
                "application/x-msdownload",
                "malicious content".getBytes()
        );

        assertThatThrownBy(() -> validationService.validateFile(file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("Extensión de archivo no permitida");
    }

    @Test
    @DisplayName("Should throw InvalidFileException when file is empty")
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        assertThatThrownBy(() -> validationService.validateFile(file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("vacío");
    }
}
