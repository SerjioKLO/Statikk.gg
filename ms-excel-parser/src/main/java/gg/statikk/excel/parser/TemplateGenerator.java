package gg.statikk.excel.parser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/** Genera la plantilla oficial (.xlsx) que descarga el usuario en GET /api/excel/template. */
@Component
public class TemplateGenerator {

    public byte[] generate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            buildMatchesSheet(workbook);
            buildInstructionsSheet(workbook);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar la plantilla", ex);
        }
    }

    private void buildMatchesSheet(XSSFWorkbook workbook) {
        Sheet sheet = workbook.createSheet("Partidas");

        CellStyle headerStyle = workbook.createCellStyle();
        Font bold = workbook.createFont();
        bold.setBold(true);
        headerStyle.setFont(bold);

        Row header = sheet.createRow(0);
        List<String> headers = MatchFileReader.EXPECTED_HEADERS;
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
            // Ancho fijo: autoSizeColumn necesita fuentes del sistema, que no existen en la imagen alpine.
            sheet.setColumnWidth(i, 20 * 256);
        }

        Row example = sheet.createRow(1);
        example.createCell(0).setCellValue("League of Legends");
        example.createCell(1).setCellValue("WIN");
        example.createCell(2).setCellValue(32);
        example.createCell(3).setCellValue(9);
        example.createCell(4).setCellValue(3);
        example.createCell(5).setCellValue(12);
        example.createCell(6).setCellValue("2026-10-11T18:30:00Z");

        // Lista desplegable en la columna "result" para que no se pueda escribir cualquier cosa.
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(new String[]{"WIN", "LOSS", "DRAW"});
        DataValidation validation = helper.createValidation(constraint, new CellRangeAddressList(1, 5000, 1, 1));
        validation.setShowErrorBox(true);
        validation.createErrorBox("Resultado inválido", "Usa WIN, LOSS o DRAW");
        sheet.addValidationData(validation);
    }

    private void buildInstructionsSheet(XSSFWorkbook workbook) {
        Sheet sheet = workbook.createSheet("Instrucciones");
        sheet.setColumnWidth(0, 100 * 256);

        List<String> lines = List.of(
                "Cómo llenar la hoja 'Partidas':",
                "1. Borra la fila de ejemplo antes de importar (si no, se importará como una partida más).",
                "2. gameName y result son obligatorios. result: WIN, LOSS o DRAW.",
                "3. durationMinutes, kills, deaths y assists son enteros >= 0 (kills/deaths/assists vacíos = 0).",
                "4. playedAt: 2026-10-11T18:30:00Z, 2026-10-11 18:30 o 2026-10-11 (vacío = fecha de la importación).",
                "5. Máximo 5000 partidas por archivo. Si UNA fila es inválida, no se importa ninguna.",
                "6. También puedes subir un .csv con las mismas columnas (separado por coma o punto y coma).");

        for (int i = 0; i < lines.size(); i++) {
            sheet.createRow(i).createCell(0).setCellValue(lines.get(i));
        }
    }
}
