package es.mjsplayground.acda.jdbc.almacen.model.repository;

import es.mjsplayground.acda.jdbc.almacen.model.entity.Producto;
import java.sql.*;
import java.util.*;

public class JdbcProductoRepository {
  public List<Producto> listar(Connection c) throws SQLException {
    List<Producto> datos = new ArrayList<>();
    try (PreparedStatement p =
            c.prepareStatement("SELECT id,nombre,precio,stock FROM producto ORDER BY nombre");
        ResultSet r = p.executeQuery()) {
      while (r.next())
        datos.add(
            new Producto(
                r.getLong("id"), r.getString("nombre"), r.getLong("precio"), r.getInt("stock")));
    }
    return datos;
  }

  public Producto buscar(Connection c, long id) throws SQLException {
    try (PreparedStatement p =
        c.prepareStatement("SELECT id,nombre,precio,stock FROM producto WHERE id=?")) {
      p.setLong(1, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next()
            ? new Producto(
                r.getLong("id"), r.getString("nombre"), r.getLong("precio"), r.getInt("stock"))
            : null;
      }
    }
  }

  public long guardar(Connection c, Producto v) throws SQLException {
    String sql =
        v.id() == 0
            ? "INSERT INTO producto(nombre,precio,stock) VALUES(?,?,?)"
            : "UPDATE producto SET nombre=?,precio=?,stock=? WHERE id=?";
    try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      p.setString(1, v.nombre());
      p.setLong(2, v.precioCentimos());
      p.setInt(3, v.stock());
      if (v.id() != 0) p.setLong(4, v.id());
      if (p.executeUpdate() != 1) throw new SQLException("El producto ya no existe");
      if (v.id() != 0) return v.id();
      try (ResultSet r = p.getGeneratedKeys()) {
        if (r.next()) return r.getLong(1);
        throw new SQLException("No se recibió la clave");
      }
    }
  }

  public boolean descontar(Connection c, long id, int cantidad) throws SQLException {
    try (PreparedStatement p =
        c.prepareStatement("UPDATE producto SET stock=stock-? WHERE id=? AND stock>=?")) {
      p.setInt(1, cantidad);
      p.setLong(2, id);
      p.setInt(3, cantidad);
      return p.executeUpdate() == 1;
    }
  }

  public void eliminar(Connection c, long id) throws SQLException {
    try (PreparedStatement p = c.prepareStatement("DELETE FROM producto WHERE id=?")) {
      p.setLong(1, id);
      if (p.executeUpdate() != 1) throw new SQLException("El producto ya no existe");
    }
  }
}
