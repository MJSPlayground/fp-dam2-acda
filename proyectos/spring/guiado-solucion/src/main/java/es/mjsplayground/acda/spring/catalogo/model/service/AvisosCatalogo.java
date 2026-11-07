package es.mjsplayground.acda.spring.catalogo.model.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;

@Component
public class AvisosCatalogo {
  private final Notificador notificador;

  public AvisosCatalogo(Notificador notificador) {
    this.notificador = notificador;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void confirmado(CambioCatalogo cambio) {
    notificador.informar(cambio);
  }
}
