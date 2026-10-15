package com.statikk.excel_parser.service;

import com.statikk.excel_parser.config.RabbitMQConfig;
import com.statikk.excel_parser.dto.ExcelUploadResponse;
import com.statikk.excel_parser.dto.MatchImportDto;
import com.statikk.excel_parser.event.ExcelMatchesImportedEvent;
import com.statikk.excel_parser.exception.CorruptDataException;
import com.statikk.excel_parser.service.impl.ExcelParserServiceImpl;
import com.statikk.excel_parser.service.impl.ExcelValidationServiceImpl;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExcelParserServiceTest {

    private ExcelValidationServiceImpl validationService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private ExcelParserServiceImpl parserService;

    @BeforeEach
    void setUp() {
        validationService = new ExcelValidationServiceImpl();
        parserService = new ExcelParserServiceImpl(validationService, rabbitTemplate);
    }

    private byte[] createSampleXlsx(boolean corrupt) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Matches");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Resultado");
            header.createCell(1).setCellValue("Horas");
            header.createCell(2).setCellValue("Kills");
            header.createCell(3).setCellValue("Deaths");
            header.createCell(4).setCellValue("Assists");
            header.createCell(5).setCellValue("Campeon");

            Row row1 = sheet.createRow(1);
            if (corrupt) {
                row1.createCell(0).setCellValue(""); // Empty result -> corrupt
                row1.createCell(1).setCellValue("NotANumber"); // Invalid number
            } else {
                row1.createCell(0).setCellValue("WIN");
                row1.createCell(1).setCellValue(0.75);
                row1.createCell(2).setCellValue(10);
                row1.createCell(3).setCellValue(2);
                row1.createCell(4).setCellValue(5);
                row1.createCell(5).setCellValue("Ahri");
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Test
    @DisplayName("RF-04: Should parse valid XLSX file and publish RabbitMQ event")
    void shouldProcessValidXlsx() throws IOException {
        byte[] xlsxBytes = createSampleXlsx(false);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "partidas.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                xlsxBytes
        );

        ExcelUploadResponse response = parserService.processFile(file, 1L, 10L);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getTotalRowsParsed()).isEqualTo(1);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EXCEL_MATCHES_IMPORTED_ROUTING_KEY),
                any(ExcelMatchesImportedEvent.class)
        );
    }

    @Test
    @DisplayName("RF-04: Should parse valid CSV file")
    void shouldProcessValidCsv() {
        String csvData = "Resultado,Horas,Kills,Deaths,Assists,Campeon\nWIN,1.0,8,2,6,Jinx\nLOSS,0.5,1,5,2,Ezreal";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "partidas.csv",
                "text/csv",
                csvData.getBytes()
        );

        List<MatchImportDto> matches = parserService.parseFile(file);

        assertThat(matches).hasSize(2);
        assertThat(matches.get(0).getResult()).isEqualTo("WIN");
        assertThat(matches.get(0).getHoursPlayed()).isEqualTo(1.0);
        assertThat(matches.get(1).getResult()).isEqualTo("LOSS");
    }

    @Test
    @DisplayName("RNF-05 Exception: Should reject corrupted XLSX and throw CorruptDataException")
    void shouldRejectCorruptXlsx() throws IOException {
        byte[] corruptBytes = createSampleXlsx(true);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "corrupt.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                corruptBytes
        );

        assertThatThrownBy(() -> parserService.processFile(file, 1L, 10L))
                .isInstanceOf(CorruptDataException.class);
    }

    @Test
    @DisplayName("Should generate valid template bytes")
    void shouldGenerateTemplate() {
        byte[] template = parserService.generateTemplate();
        assertThat(template).isNotEmpty();
    }
}
