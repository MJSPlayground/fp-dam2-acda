package es.mjsplayground.acda.spring.catalogo;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.spring.catalogo.config.*;
import es.mjsplayground.acda.spring.catalogo.model.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ComponentesTest {
  ApplicationContextRunner contexto =
      new ApplicationContextRunner()
          .withUserConfiguration(
              CatalogoConfig.class, NotificadorLog.class, NotificadorSilencioso.class)
          .withPropertyValues("catalogo.max-stock=1000");

  @Test
  void constructorYBeanClockSinBaseDeDatos() {
    contexto.run(
        c -> {
          assertNotNull(c.getBean(java.time.Clock.class));
          assertEquals(1000, c.getBean(CatalogoProperties.class).maxStock());
          assertInstanceOf(NotificadorLog.class, c.getBean(Notificador.class));
        });
  }

  @Test
  void perfilSustituyeComponenteSinCambiarServicio() {
    contexto
        .withInitializer(c -> c.getEnvironment().setActiveProfiles("silencioso"))
        .run(c -> assertInstanceOf(NotificadorSilencioso.class, c.getBean(Notificador.class)));
  }
}
