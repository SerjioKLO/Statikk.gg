package gg.statikk.notification.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

/**
 * Filtro sobre los frames STOMP que envía el cliente:
 *  - SUBSCRIBE: solo a /user/queue/** (Spring lo traduce a una cola exclusiva de ESA sesión). Sin esto,
 *    un cliente podría suscribirse a destinos arbitrarios del broker y leer mensajes de otros usuarios.
 *  - SEND: prohibido. Este servicio solo EMITE notificaciones; los clientes nunca publican.
 * Lanzar la excepción hace que Spring responda con un frame ERROR y cierre la conexión.
 */
public class StompSecurityInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())
                && !StompDestinations.isAllowedSubscription(accessor.getDestination())) {
            throw new MessageDeliveryException("Suscripción no permitida: " + accessor.getDestination()
                    + " (solo " + StompDestinations.ALLOWED_SUBSCRIPTION_PREFIX + "**)");
        }
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            throw new MessageDeliveryException("Los clientes no pueden enviar mensajes a este servicio");
        }
        return message;
    }
}
