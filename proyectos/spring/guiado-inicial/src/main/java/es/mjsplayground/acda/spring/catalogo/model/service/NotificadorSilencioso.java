package es.mjsplayground.acda.spring.catalogo.model.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("silencioso")
public class NotificadorSilencioso implements Notificador {
  public void informar(CambioCatalogo cambio) {
    /* implementación alternativa: sin aviso local */
  }
}
