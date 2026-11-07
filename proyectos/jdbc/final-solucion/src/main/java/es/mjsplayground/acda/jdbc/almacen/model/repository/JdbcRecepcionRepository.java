package es.mjsplayground.acda.jdbc.almacen.model.repository;

import es.mjsplayground.acda.jdbc.almacen.model.entity.*;
import java.sql.*;
import java.util.*;

public class JdbcRecepcionRepository {
  public void preparar(Connection c) throws SQLException {
    try (Statement s = c.createStatement()) {
      s.executeUpdate(
          "CREATE TABLE IF NOT EXISTS recepcion (id INTEGER PRIMARY KEY, producto_id INTEGER NOT"
              + " NULL REFERENCES producto(id), cantidad INTEGER NOT NULL CHECK(cantidad>0), total"
              + " INTEGER NOT NULL CHECK(total>0), fecha TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
    }
  }

  public void incrementar(Connection c, long id, int unidades) throws SQLException {
    try (PreparedStatement p = c.prepareStatement("UPDATE producto SET stock=stock+? WHERE id=?")) {
      p.setInt(1, unidades);
      p.setLong(2, id);
      if (p.executeUpdate() != 1) throw new SQLException("Producto inexistente");
    }
  }

  public void registrar(Connection c, LineaRecepcion linea, long total) throws SQLException {
    try (PreparedStatement p =
        c.prepareStatement("INSERT INTO recepcion(producto_id,cantidad,total) VALUES(?,?,?)")) {
      p.setLong(1, linea.productoId());
      p.setInt(2, linea.cantidad());
      p.setLong(3, total);
      p.executeUpdate();
    }
  }

  public void precio(Connection c, long id, long centimos) throws SQLException {
    try (PreparedStatement p = c.prepareStatement("UPDATE producto SET precio=? WHERE id=?")) {
      p.setLong(1, centimos);
      p.setLong(2, id);
      if (p.executeUpdate() != 1) throw new SQLException("Producto inexistente");
    }
  }

  public List<Producto> bajoMaximo(Connection c, int maximo) throws SQLException {
    List<Producto> datos = new ArrayList<>();
    try (PreparedStatement p =
        c.prepareStatement(
            "SELECT id,nombre,precio,stock FROM producto WHERE stock<=? ORDER BY nombre")) {
      p.setInt(1, maximo);
      try (ResultSet r = p.executeQuery()) {
        while (r.next())
          datos.add(new Producto(r.getLong(1), r.getString(2), r.getLong(3), r.getInt(4)));
      }
    }
    return datos;
  }

  public List<Recepcion> historial(Connection c) throws SQLException {
    List<Recepcion> datos = new ArrayList<>();
    try (PreparedStatement p =
            c.prepareStatement(
                "SELECT p.nombre,r.cantidad,r.total,r.fecha FROM recepcion r JOIN producto p ON"
                    + " p.id=r.producto_id ORDER BY r.id DESC");
        ResultSet r = p.executeQuery()) {
      while (r.next())
        datos.add(new Recepcion(r.getString(1), r.getInt(2), r.getLong(3), r.getString(4)));
    }
    return datos;
  }
}
