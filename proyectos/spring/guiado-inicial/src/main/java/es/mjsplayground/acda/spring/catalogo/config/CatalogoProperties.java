package es.mjsplayground.acda.spring.catalogo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "catalogo")
public record CatalogoProperties(int maxStock) {
  public CatalogoProperties {
    if (maxStock < 1 || maxStock > 1000000)
      throw new IllegalArgumentException("catalogo.max-stock entre 1 y 1000000");
  }
}
