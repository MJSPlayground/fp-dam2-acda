package es.mjsplayground.acda.spring.catalogo;

import java.sql.*;
import java.util.UUID;

final class MySqlEsquema {
  final String nombre = "qa_spring_" + UUID.randomUUID().toString().replace("-", "");
  boolean creado;
  final String admin =
      System.getenv()
          .getOrDefault(
              "QA_MYSQL_ADMIN_URL",
              "jdbc:mysql://127.0.0.1:3309/mysql?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=UTC");
  final String usuario = System.getenv().getOrDefault("QA_MYSQL_ADMIN_USER", "root"),
      clave = System.getenv().getOrDefault("QA_MYSQL_ADMIN_PASSWORD", "qa_root_local");

  synchronized String url() {
    if (!creado)
      try (Connection c = DriverManager.getConnection(admin, usuario, clave);
          Statement s = c.createStatement()) {
        s.executeUpdate(
            "CREATE DATABASE " + nombre + " CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs");
        creado = true;
      } catch (SQLException e) {
        throw new IllegalStateException("No se pudo preparar esquema QA propio", e);
      }
    String base = admin.contains("?") ? admin.substring(0, admin.indexOf('?')) : admin;
    return base.substring(0, base.lastIndexOf('/') + 1)
        + nombre
        + "?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=UTC";
  }

  synchronized void eliminar() throws SQLException {
    if (creado) {
      if (!nombre.matches("qa_spring_[a-f0-9]{32}"))
        throw new IllegalStateException("Esquema no permitido");
      try (Connection c = DriverManager.getConnection(admin, usuario, clave);
          Statement s = c.createStatement()) {
        s.executeUpdate("DROP DATABASE " + nombre);
      }
      creado = false;
    }
  }
}
