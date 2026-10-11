package gg.statikk.excel.parser;

import gg.statikk.excel.exception.UnsupportedFileTypeException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

/**
 * Formatos aceptados y los Content-Type que se toleran para cada uno.
 *
 * Se acepta también "application/octet-stream" porque muchos clientes (curl, algunos
 * navegadores) lo envían por defecto para .xlsx/.csv. La validación real del contenido
 * la hace el parser al abrir el archivo: un .xlsx falso (ej. un .exe renombrado) falla
 * ahí con 422.
 */
public enum FileFormat {

    XLSX(".xlsx", Set.of(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/octet-stream")),

    CSV(".csv", Set.of(
            "text/csv",
            "application/csv",
            "application/vnd.ms-excel", // Windows suele etiquetar así los .csv
            "text/plain",
            "application/octet-stream"));

    private final String extension;
    private final Set<String> allowedContentTypes;

    FileFormat(String extension, Set<String> allowedContentTypes) {
        this.extension = extension;
        this.allowedContentTypes = allowedContentTypes;
    }

    public static FileFormat detect(MultipartFile file) {
        String name = file.getOriginalFilename() == null
                ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        String contentType = normalize(file.getContentType());

        for (FileFormat format : values()) {
            if (name.endsWith(format.extension)) {
                if (!format.allowedContentTypes.contains(contentType)) {
                    throw new UnsupportedFileTypeException(
                            "Content-Type no permitido para un archivo " + format.extension + ": " + contentType);
                }
                return format;
            }
        }
        throw new UnsupportedFileTypeException("Solo se aceptan archivos .xlsx o .csv");
    }

    /** "text/csv; charset=UTF-8" -> "text/csv" */
    private static String normalize(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        int semicolon = contentType.indexOf(';');
        String base = semicolon >= 0 ? contentType.substring(0, semicolon) : contentType;
        return base.trim().toLowerCase(Locale.ROOT);
    }
}
