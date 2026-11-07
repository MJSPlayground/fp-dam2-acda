package es.mjsplayground.acda.spring.catalogo.model.service;

import org.slf4j.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!silencioso")
public class NotificadorLog implements Notificador {
  private static final Logger log = LoggerFactory.getLogger(NotificadorLog.class);

  public void informar(CambioCatalogo c) {
    log.info("Confirmado {} id={} instante={}", c.operacion(), c.id(), c.instante());
  }
}
