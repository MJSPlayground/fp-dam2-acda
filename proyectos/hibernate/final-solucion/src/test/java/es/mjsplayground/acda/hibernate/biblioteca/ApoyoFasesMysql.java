package es.mjsplayground.acda.hibernate.biblioteca;

import es.mjsplayground.acda.hibernate.biblioteca.config.HibernateFactory;
import es.mjsplayground.acda.hibernate.biblioteca.model.entity.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import org.hibernate.*;

/** Infraestructura de prueba entregada: nunca usa biblioteca_guiado/final. */
final class ApoyoFasesMysql implements AutoCloseable {
  final String esquema = "qa_hibernate_fase_" + UUID.randomUUID().toString().replace("-", "");
  final String admin =
      System.getenv()
          .getOrDefault(
              "QA_MYSQL_ADMIN_URL",
              "jdbc:mysql://127.0.0.1:3308/mysql?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=UTC");
  final String usuario = System.getenv().getOrDefault("QA_MYSQL_ADMIN_USER", "root"),
      clave = System.getenv().getOrDefault("QA_MYSQL_ADMIN_PASSWORD", "qa_root_local");
  SessionFactory factory;

  ApoyoFasesMysql() throws SQLException {
    try (Connection c = DriverManager.getConnection(admin, usuario, clave);
        Statement s = c.createStatement()) {
      s.executeUpdate(
          "CREATE DATABASE " + esquema + " CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs");
    }
    String base = admin.contains("?") ? admin.substring(0, admin.indexOf('?')) : admin;
    String url =
        base.substring(0, base.lastIndexOf('/') + 1)
            + esquema
            + "?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    try {
      factory = HibernateFactory.crear(url, usuario, clave, "update");
    } catch (RuntimeException fallo) {
      try {
        close();
      } catch (Exception limpieza) {
        fallo.addSuppressed(limpieza);
      }
      throw fallo;
    }
  }

  long contar(String entidad) {
    try (Session s = factory.openSession()) {
      return s.createSelectionQuery("select count(x) from " + entidad + " x", Long.class)
          .getSingleResult();
    }
  }

  record Datos(long origen, long destino, long autor, long libro, long otroLibro) {}

  Datos preparar() {
    return factory.fromTransaction(
        s -> {
          Editorial e = new Editorial("Horizonte"), n = new Editorial("Norte");
          Autor a = new Autor("Alex Ríos"), b = new Autor("Luz Vega");
          s.persist(e);
          s.persist(n);
          s.persist(a);
          s.persist(b);
          Libro l =
              new Libro(
                  "9780000000001",
                  "Java y datos",
                  new BigDecimal("29.90"),
                  e,
                  List.of(a, b),
                  new Detalle("Español", 320));
          Libro otro =
              new Libro(
                  "9780000000002",
                  "Historias locales",
                  new BigDecimal("18.50"),
                  n,
                  List.of(a),
                  null);
          s.persist(l);
          s.persist(otro);
          s.flush();
          return new Datos(e.getId(), n.getId(), a.getId(), l.getId(), otro.getId());
        });
  }

  public void close() throws SQLException {
    if (factory != null) factory.close();
    if (!esquema.matches("qa_hibernate_fase_[a-f0-9]{32}"))
      throw new IllegalStateException("Esquema ajeno");
    try (Connection c = DriverManager.getConnection(admin, usuario, clave);
        Statement s = c.createStatement()) {
      s.executeUpdate("DROP DATABASE " + esquema);
    }
  }
}
