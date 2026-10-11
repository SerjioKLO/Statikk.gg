package gg.statikk.excel.event;

import gg.statikk.excel.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ExcelEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ExcelEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public ExcelEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishMatchesImported(ExcelMatchesImportedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, "excel.matches.imported", event);
        log.info("Evento publicado: excel.matches.imported -> importId={}, playerProfileId={}, filas={}",
                event.importId(), event.playerProfileId(), event.matches().size());
    }
}
