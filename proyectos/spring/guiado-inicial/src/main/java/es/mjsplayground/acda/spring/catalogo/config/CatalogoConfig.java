package es.mjsplayground.acda.spring.catalogo.config;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@EnableConfigurationProperties(CatalogoProperties.class)
public class CatalogoConfig {
  @Bean
  Clock reloj() {
    return Clock.systemUTC();
  }
}
