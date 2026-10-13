package gg.statikk.notification.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/** Asocia a cada sesión WebSocket el userId que reenvió el gateway, para poder enviarle mensajes SOLO a él. */
public class HeaderUserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        String userId = request.getHeaders().getFirst(RequireUserHeaderInterceptor.USER_ID_HEADER);
        return userId == null ? null : new StompPrincipal(userId);
    }
}
