package es.mjsplayground.acda.mongodb.liga.config;

import es.mjsplayground.acda.mongodb.liga.model.entity.Sede;
import es.mjsplayground.acda.mongodb.liga.model.repository.SedeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatosIniciales implements CommandLineRunner {
  private final SedeRepository sedes;

  public DatosIniciales(SedeRepository sedes) {
    this.sedes = sedes;
  }

  public void run(String... args) {
    if (!sedes.existsById("CENTRO")) sedes.insert(new Sede("CENTRO", "Pabellón Centro", "Bilbao"));
    if (!sedes.existsById("NORTE")) sedes.insert(new Sede("NORTE", "Campo Norte", "Bilbao"));
  }
}
