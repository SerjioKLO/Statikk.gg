package gg.statikk.notification.websocket;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Rechaza el handshake si no trae X-User-Id numérico. Ese header lo pone ms-gateway DESPUÉS de validar el JWT;
 * si falta, alguien llegó directo al servicio sin pasar por el gateway.
 */
public class RequireUserHeaderInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String userId = request.getHeaders().getFirst(USER_ID_HEADER);
        if (userId == null || !userId.matches("\\d+")) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // nada que hacer
    }
}
