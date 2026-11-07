package es.mjsplayground.acda.jdbc.almacen.config;

import java.sql.*;

public final class Database {
  private final String url;

  public Database(String url) {
    this.url = url;
  }

  public Connection abrir() throws SQLException {
    Connection c = DriverManager.getConnection(url);
    try (Statement s = c.createStatement()) {
      s.execute("PRAGMA foreign_keys = ON");
      s.execute("PRAGMA busy_timeout = 3000");
      return c;
    } catch (SQLException e) {
      try {
        c.close();
      } catch (SQLException cierre) {
        e.addSuppressed(cierre);
      }
      throw e;
    }
  }

  public void preparar() throws SQLException {
    try (Connection c = abrir();
        Statement s = c.createStatement()) {
      boolean nueva;
      try (ResultSet r =
          s.executeQuery(
              "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='producto'")) {
        r.next();
        nueva = r.getInt(1) == 0;
      }
      s.executeUpdate(
          "CREATE TABLE IF NOT EXISTS producto (id INTEGER PRIMARY KEY, nombre TEXT NOT NULL"
              + " UNIQUE, precio INTEGER NOT NULL CHECK(precio > 0), stock INTEGER NOT NULL"
              + " CHECK(stock >= 0))");
      s.executeUpdate(
          "CREATE TABLE IF NOT EXISTS venta (id INTEGER PRIMARY KEY, producto_id INTEGER NOT NULL"
              + " REFERENCES producto(id), cantidad INTEGER NOT NULL CHECK(cantidad > 0), total"
              + " INTEGER NOT NULL CHECK(total > 0), fecha TEXT NOT NULL DEFAULT"
              + " CURRENT_TIMESTAMP)");
      if (nueva)
        s.executeUpdate(
            "INSERT OR IGNORE INTO producto(id,nombre,precio,stock) VALUES"
                + " (1,'Teclado',2590,10),(2,'Ratón',1295,20),(3,'Monitor',15900,5)");
    }
  }
}
