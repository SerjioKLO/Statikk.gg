package gg.statikk.excel.parser;

import gg.statikk.excel.exception.InvalidFileException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Convierte un .xlsx o .csv en una lista de filas de texto (la primera es la cabecera).
 * No valida contenido: eso lo hace MatchRowValidator.
 */
@Component
public class MatchFileReader {

    /** Orden fijo de columnas; es el contrato de la plantilla oficial. */
    public static final List<String> EXPECTED_HEADERS = List.of(
            "gameName", "result", "durationMinutes", "kills", "deaths", "assists", "playedAt");

    private static final int COLUMNS = EXPECTED_HEADERS.size();

    public List<RawRow> read(MultipartFile file, FileFormat format) {
        try {
            return switch (format) {
                case XLSX -> readXlsx(file.getInputStream());
                case CSV -> readCsv(file.getBytes());
            };
        } catch (IOException | RuntimeException ex) {
            throw new InvalidFileException(
                    "No se pudo leer el archivo: está corrupto o no es un " + format + " válido", List.of());
        }
    }

    private List<RawRow> readXlsx(InputStream in) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            List<RawRow> rows = new ArrayList<>();
            for (Row row : sheet) {
                List<String> cells = new ArrayList<>(COLUMNS);
                for (int c = 0; c < COLUMNS; c++) {
                    cells.add(cellToString(row.getCell(c), formatter, evaluator));
                }
                rows.add(new RawRow(row.getRowNum() + 1, cells));
            }
            return rows;
        }
    }

    private String cellToString(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }
        // Una fecha escrita en Excel es un número con formato de fecha: la pasamos a ISO-8601 (UTC).
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toInstant(ZoneOffset.UTC).toString();
        }
        return formatter.formatCellValue(cell, evaluator).trim();
    }

    private List<RawRow> readCsv(byte[] bytes) throws IOException {
        String content = new String(bytes, StandardCharsets.UTF_8);
        if (content.startsWith("\uFEFF")) { // BOM que agrega Excel al exportar "CSV UTF-8"
            content = content.substring(1);
        }

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(detectDelimiter(content))
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        List<RawRow> rows = new ArrayList<>();
        try (CSVParser parser = format.parse(new StringReader(content))) {
            for (CSVRecord record : parser) {
                List<String> cells = new ArrayList<>(COLUMNS);
                for (int c = 0; c < COLUMNS; c++) {
                    cells.add(c < record.size() ? record.get(c) : "");
                }
                rows.add(new RawRow((int) record.getRecordNumber(), cells));
            }
        }
        return rows;
    }

    /** Excel en español exporta CSV separado por ";" en vez de ",": lo detectamos por la cabecera. */
    private char detectDelimiter(String content) {
        int newline = content.indexOf('\n');
        String firstLine = newline >= 0 ? content.substring(0, newline) : content;
        long semicolons = firstLine.chars().filter(ch -> ch == ';').count();
        long commas = firstLine.chars().filter(ch -> ch == ',').count();
        return semicolons > commas ? ';' : ',';
    }
}
