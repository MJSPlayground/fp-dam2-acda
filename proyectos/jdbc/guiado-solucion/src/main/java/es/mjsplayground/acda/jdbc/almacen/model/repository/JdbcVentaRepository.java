package es.mjsplayground.acda.jdbc.almacen.model.repository;

import es.mjsplayground.acda.jdbc.almacen.model.entity.Venta;
import java.sql.*;
import java.util.*;

public class JdbcVentaRepository {
  public boolean existenPara(Connection c, long id) throws SQLException {
    try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM venta WHERE producto_id=?")) {
      p.setLong(1, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next();
      }
    }
  }

  public void registrar(Connection c, long producto, int cantidad, long total) throws SQLException {
    try (PreparedStatement p =
        c.prepareStatement("INSERT INTO venta(producto_id,cantidad,total) VALUES(?,?,?)")) {
      p.setLong(1, producto);
      p.setInt(2, cantidad);
      p.setLong(3, total);
      p.executeUpdate();
    }
  }

  public List<Venta> listar(Connection c) throws SQLException {
    List<Venta> datos = new ArrayList<>();
    try (PreparedStatement p =
            c.prepareStatement(
                "SELECT v.id,p.nombre,v.cantidad,v.total,v.fecha FROM venta v JOIN producto p ON"
                    + " p.id=v.producto_id ORDER BY v.id DESC");
        ResultSet r = p.executeQuery()) {
      while (r.next())
        datos.add(
            new Venta(r.getLong(1), r.getString(2), r.getInt(3), r.getLong(4), r.getString(5)));
    }
    return datos;
  }
}
