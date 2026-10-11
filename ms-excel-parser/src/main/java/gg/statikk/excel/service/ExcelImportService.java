package gg.statikk.excel.service;

import feign.FeignException;
import gg.statikk.excel.client.PlayerClient;
import gg.statikk.excel.client.PlayerProfileClientResponse;
import gg.statikk.excel.dto.ImportResponse;
import gg.statikk.excel.dto.ImportedMatchRow;
import gg.statikk.excel.dto.RowError;
import gg.statikk.excel.event.ExcelEventPublisher;
import gg.statikk.excel.event.ExcelMatchesImportedEvent;
import gg.statikk.excel.exception.ForbiddenException;
import gg.statikk.excel.exception.InvalidFileException;
import gg.statikk.excel.exception.PlayerProfileNotFoundException;
import gg.statikk.excel.exception.PlayerServiceUnavailableException;
import gg.statikk.excel.parser.FileFormat;
import gg.statikk.excel.parser.MatchFileReader;
import gg.statikk.excel.parser.MatchRowValidator;
import gg.statikk.excel.parser.RawRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ExcelImportService {

    private final PlayerClient playerClient;
    private final MatchFileReader fileReader;
    private final MatchRowValidator rowValidator;
    private final ExcelEventPublisher eventPublisher;
    private final int maxRows;

    public ExcelImportService(PlayerClient playerClient,
                              MatchFileReader fileReader,
                              MatchRowValidator rowValidator,
                              ExcelEventPublisher eventPublisher,
                              @Value("${excel.max-rows:5000}") int maxRows) {
        this.playerClient = playerClient;
        this.fileReader = fileReader;
        this.rowValidator = rowValidator;
        this.eventPublisher = eventPublisher;
        this.maxRows = maxRows;
    }

    /**
     * Flujo: tipo de archivo -> dueño del perfil (Feign) -> lectura + validación de TODAS las filas
     * -> publicar evento. Es "todo o nada": si una sola fila es inválida no se publica nada.
     * La inserción real la hace ms-matches de forma asíncrona, por eso la respuesta es 202.
     */
    public ImportResponse importMatches(Long userId, Long playerProfileId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("El archivo está vacío", List.of());
        }
        FileFormat format = FileFormat.detect(file);

        PlayerProfileClientResponse profile = fetchProfileOrThrow(playerProfileId);
        if (!profile.userId().equals(userId)) {
            throw new ForbiddenException("No puedes importar partidas en el perfil de otro jugador");
        }

        List<ImportedMatchRow> matches = parseAndValidate(fileReader.read(file, format));

        String importId = UUID.randomUUID().toString();
        eventPublisher.publishMatchesImported(new ExcelMatchesImportedEvent(
                importId, playerProfileId, userId, matches, Instant.now()));

        return new ImportResponse(importId, matches.size(), "QUEUED",
                "Importación encolada: las partidas aparecerán en tu historial en unos segundos");
    }

    private List<ImportedMatchRow> parseAndValidate(List<RawRow> rows) {
        if (rows.isEmpty()) {
            throw new InvalidFileException("El archivo no contiene filas", List.of());
        }
        validateHeader(rows.get(0));

        List<RawRow> dataRows = rows.subList(1, rows.size()).stream()
                .filter(row -> !isBlank(row))
                .toList();

        if (dataRows.isEmpty()) {
            throw new InvalidFileException("El archivo solo tiene la cabecera, no hay partidas para importar", List.of());
        }
        if (dataRows.size() > maxRows) {
            throw new InvalidFileException(
                    "Máximo " + maxRows + " partidas por archivo (el archivo trae " + dataRows.size() + ")", List.of());
        }

        List<ImportedMatchRow> valid = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();
        for (RawRow raw : dataRows) {
            ImportedMatchRow row = rowValidator.validate(raw, errors);
            if (row != null) {
                valid.add(row);
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidFileException(
                    "El archivo tiene filas inválidas; no se importó nada. Corrígelas y vuelve a subirlo", errors);
        }
        return valid;
    }

    private void validateHeader(RawRow header) {
        List<String> actual = header.cells().stream().map(h -> h.toLowerCase(Locale.ROOT)).toList();
        List<String> expected = MatchFileReader.EXPECTED_HEADERS.stream().map(h -> h.toLowerCase(Locale.ROOT)).toList();
        if (!actual.equals(expected)) {
            throw new InvalidFileException(
                    "Cabecera inválida. Se esperaba: " + String.join(", ", MatchFileReader.EXPECTED_HEADERS)
                            + " (en ese orden). Descarga la plantilla en GET /api/excel/template", List.of());
        }
    }

    private boolean isBlank(RawRow row) {
        return row.cells().stream().allMatch(String::isBlank);
    }

    private PlayerProfileClientResponse fetchProfileOrThrow(Long playerProfileId) {
        try {
            return playerClient.getProfile(playerProfileId);
        } catch (FeignException.NotFound ex) {
            throw new PlayerProfileNotFoundException(playerProfileId);
        } catch (FeignException ex) {
            throw new PlayerServiceUnavailableException(playerProfileId, ex);
        } catch (RuntimeException ex) {
            throw new PlayerServiceUnavailableException(playerProfileId, ex);
        }
    }
}
