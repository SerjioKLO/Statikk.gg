package gg.statikk.gateway.filter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * El API WebSocket de los navegadores NO permite enviar headers personalizados (ni Authorization) en el
 * handshake. Por eso, SOLO para rutas /ws/**, el gateway acepta el JWT también como parámetro de consulta
 * "access_token" y lo elimina de la URL antes de reenviar la petición al microservicio.
 *
 * Compromiso conocido: un token en la URL puede quedar en logs de proxies o del navegador. Para reducir el
 * riesgo solo se acepta en /ws/**, nunca se reenvía downstream y los tokens de ms-auth son de corta vida.
 *
 * Clase sin dependencias de Spring a propósito, para poder probarla de forma aislada.
 */
public final class WebSocketTokenSupport {

    public static final String WS_PATH_PREFIX = "/ws/";
    public static final String TOKEN_PARAM = "access_token";

    private WebSocketTokenSupport() {
    }

    public static boolean isWebSocketPath(String path) {
        return path != null && path.startsWith(WS_PATH_PREFIX);
    }

    /** @return el valor de access_token, o null si falta o está vacío. */
    public static String tokenFromQuery(Map<String, List<String>> queryParams) {
        List<String> values = queryParams.get(TOKEN_PARAM);
        if (values == null || values.isEmpty() || values.get(0) == null || values.get(0).isBlank()) {
            return null;
        }
        return values.get(0);
    }

    /**
     * Devuelve la misma URI sin el parámetro access_token (y sin tocar los demás ni su codificación).
     * Se reconstruye con los componentes "raw" para no volver a codificar caracteres ya codificados.
     */
    public static URI stripTokenParam(URI uri) {
        String rawQuery = uri.getRawQuery();
        if (rawQuery == null) {
            return uri;
        }

        List<String> kept = new ArrayList<>();
        boolean removed = false;
        for (String pair : rawQuery.split("&")) {
            int equals = pair.indexOf('=');
            String name = equals >= 0 ? pair.substring(0, equals) : pair;
            if (name.equals(TOKEN_PARAM)) {
                removed = true;
            } else if (!pair.isEmpty()) {
                kept.add(pair);
            }
        }
        if (!removed) {
            return uri;
        }

        StringBuilder rebuilt = new StringBuilder();
        if (uri.getScheme() != null) {
            rebuilt.append(uri.getScheme()).append(':');
        }
        if (uri.getRawAuthority() != null) {
            rebuilt.append("//").append(uri.getRawAuthority());
        }
        if (uri.getRawPath() != null) {
            rebuilt.append(uri.getRawPath());
        }
        if (!kept.isEmpty()) {
            rebuilt.append('?').append(String.join("&", kept));
        }
        if (uri.getRawFragment() != null) {
            rebuilt.append('#').append(uri.getRawFragment());
        }
        return URI.create(rebuilt.toString());
    }
}
