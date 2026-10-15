package com.statikk.excel_parser.service.impl;

import com.statikk.excel_parser.config.RabbitMQConfig;
import com.statikk.excel_parser.dto.ExcelUploadResponse;
import com.statikk.excel_parser.dto.MatchImportDto;
import com.statikk.excel_parser.event.ExcelMatchesImportedEvent;
import com.statikk.excel_parser.event.ImportedMatchItem;
import com.statikk.excel_parser.exception.CorruptDataException;
import com.statikk.excel_parser.service.ExcelParserService;
import com.statikk.excel_parser.service.ExcelValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelParserServiceImpl implements ExcelParserService {

    private final ExcelValidationService validationService;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public ExcelUploadResponse processFile(MultipartFile file, Long userId, Long gameId) {
        long startTime = System.currentTimeMillis();
        validationService.validateFile(file);

        List<MatchImportDto> parsedMatches = parseFile(file);

        if (parsedMatches.isEmpty()) {
            throw new CorruptDataException("El archivo no contiene filas de partidas válidas");
        }

        // Convert to event items
        List<ImportedMatchItem> eventItems = parsedMatches.stream()
                .map(m -> ImportedMatchItem.builder()
                        .result(m.getResult())
                        .durationMinutes(m.getDurationMinutes())
                        .hoursPlayed(m.getHoursPlayed())
                        .kills(m.getKills())
                        .deaths(m.getDeaths())
                        .assists(m.getAssists())
                        .championOrCharacter(m.getChampionOrCharacter())
                        .gameMode(m.getGameMode())
                        .notes(m.getNotes())
                        .playedAt(m.getPlayedAt())
                        .build())
                .collect(Collectors.toList());

        // Publish event to RabbitMQ (RNF-03 Escalabilidad)
        ExcelMatchesImportedEvent event = ExcelMatchesImportedEvent.builder()
                .userId(userId)
                .gameId(gameId)
                .filename(file.getOriginalFilename())
                .matches(eventItems)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.EXCEL_MATCHES_IMPORTED_ROUTING_KEY, event);
            log.info("Published ExcelMatchesImportedEvent with {} items to RabbitMQ", eventItems.size());
        } catch (Exception e) {
            log.error("Failed to publish event to RabbitMQ: {}", e.getMessage(), e);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Processed Excel file '{}' in {} ms (RNF-01 < 2000ms check)", file.getOriginalFilename(), duration);

        return ExcelUploadResponse.builder()
                .filename(file.getOriginalFilename())
                .userId(userId)
                .gameId(gameId)
                .totalRowsParsed(parsedMatches.size())
                .status("SUCCESS")
                .message("Archivo procesado exitosamente. Se importaron " + parsedMatches.size() + " partidas.")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public List<MatchImportDto> parseFile(MultipartFile file) {
        if (validationService.isCsv(file)) {
            return parseCsv(file);
        } else {
            return parseXlsx(file);
        }
    }

    private List<MatchImportDto> parseXlsx(MultipartFile file) {
        List<MatchImportDto> list = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                throw new CorruptDataException("La hoja de cálculo está vacía o carece de datos");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new CorruptDataException("Falta la fila de encabezados obligatoria");
            }

            // Iterate rows
            int totalRows = sheet.getLastRowNum();
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    String result = getCellString(row.getCell(0));
                    if (result == null || result.isBlank()) {
                        throw new CorruptDataException("Fila " + (i + 1) + ": Columna 'Resultado' no puede estar vacía");
                    }
                    String normalizedResult = normalizeResult(result);

                    Double hours = getCellNumeric(row.getCell(1));
                    if (hours == null || hours < 0) {
                        throw new CorruptDataException("Fila " + (i + 1) + ": Columna 'Horas Jugadas' debe ser un número positivo");
                    }

                    Integer kills = getCellInteger(row.getCell(2), 0);
                    Integer deaths = getCellInteger(row.getCell(3), 0);
                    Integer assists = getCellInteger(row.getCell(4), 0);
                    String champion = getCellString(row.getCell(5));
                    String gameMode = getCellString(row.getCell(6));
                    String notes = getCellString(row.getCell(7));

                    list.add(MatchImportDto.builder()
                            .result(normalizedResult)
                            .hoursPlayed(hours)
                            .durationMinutes(hours * 60)
                            .kills(kills)
                            .deaths(deaths)
                            .assists(assists)
                            .championOrCharacter(champion != null ? champion : "General")
                            .gameMode(gameMode != null ? gameMode : "Standard")
                            .notes(notes)
                            .playedAt(LocalDateTime.now())
                            .build());
                } catch (Exception e) {
                    log.error("Corrupt cell at row {}: {}", i + 1, e.getMessage());
                    throw new CorruptDataException("Fila " + (i + 1) + " contiene celdas corruptas o formato inválido: " + e.getMessage());
                }
            }
        } catch (CorruptDataException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to read Excel workbook: {}", e.getMessage());
            throw new CorruptDataException("Error al leer la estructura del archivo Excel: " + e.getMessage());
        }

        return list;
    }

    private List<MatchImportDto> parseCsv(MultipartFile file) {
        List<MatchImportDto> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                throw new CorruptDataException("El archivo CSV está vacío");
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("[,;]");
                if (parts.length < 2) {
                    throw new CorruptDataException("Fila CSV " + lineNumber + ": Número insuficiente de columnas");
                }

                try {
                    String result = normalizeResult(parts[0].trim());
                    double hours = Double.parseDouble(parts[1].trim());
                    int kills = parts.length > 2 && !parts[2].trim().isEmpty() ? Integer.parseInt(parts[2].trim()) : 0;
                    int deaths = parts.length > 3 && !parts[3].trim().isEmpty() ? Integer.parseInt(parts[3].trim()) : 0;
                    int assists = parts.length > 4 && !parts[4].trim().isEmpty() ? Integer.parseInt(parts[4].trim()) : 0;
                    String champion = parts.length > 5 ? parts[5].trim() : "General";
                    String gameMode = parts.length > 6 ? parts[6].trim() : "Standard";
                    String notes = parts.length > 7 ? parts[7].trim() : "";

                    list.add(MatchImportDto.builder()
                            .result(result)
                            .hoursPlayed(hours)
                            .durationMinutes(hours * 60)
                            .kills(kills)
                            .deaths(deaths)
                            .assists(assists)
                            .championOrCharacter(champion)
                            .gameMode(gameMode)
                            .notes(notes)
                            .playedAt(LocalDateTime.now())
                            .build());
                } catch (Exception e) {
                    throw new CorruptDataException("Fila CSV " + lineNumber + " corrupta: " + e.getMessage());
                }
            }
        } catch (CorruptDataException e) {
            throw e;
        } catch (Exception e) {
            throw new CorruptDataException("Error al procesar archivo CSV: " + e.getMessage());
        }

        return list;
    }

    @Override
    public byte[] generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Estadisticas");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"Resultado (WIN/LOSS)", "Horas Jugadas", "Asesinatos (Kills)", "Muertes (Deaths)", "Asistencias (Assists)", "Campeón/Personaje", "Modo de Juego", "Notas"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample row 1
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("WIN");
            row1.createCell(1).setCellValue(0.75);
            row1.createCell(2).setCellValue(12);
            row1.createCell(3).setCellValue(2);
            row1.createCell(4).setCellValue(8);
            row1.createCell(5).setCellValue("Ahri");
            row1.createCell(6).setCellValue("Ranked Solo");
            row1.createCell(7).setCellValue("Buena partida");

            // Sample row 2
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("LOSS");
            row2.createCell(1).setCellValue(0.5);
            row2.createCell(2).setCellValue(3);
            row2.createCell(3).setCellValue(6);
            row2.createCell(4).setCellValue(4);
            row2.createCell(5).setCellValue("Yasuo");
            row2.createCell(6).setCellValue("Normal");
            row2.createCell(7).setCellValue("Desconexión");

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating Excel template: {}", e.getMessage());
            return new byte[0];
        }
    }

    private String normalizeResult(String res) {
        if (res == null) return "LOSS";
        String upper = res.trim().toUpperCase();
        if (upper.startsWith("V") || upper.equals("WIN") || upper.equals("VICTORIA")) {
            return "WIN";
        } else if (upper.startsWith("D") || upper.equals("LOSS") || upper.equals("DERROTA")) {
            return "LOSS";
        }
        return "DRAW";
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Double getCellNumeric(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Integer getCellInteger(Cell cell, int defaultValue) {
        Double num = getCellNumeric(cell);
        return num != null ? num.intValue() : defaultValue;
    }
}
