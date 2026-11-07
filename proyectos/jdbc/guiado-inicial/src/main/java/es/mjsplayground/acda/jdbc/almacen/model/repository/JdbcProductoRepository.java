package es.mjsplayground.acda.jdbc.almacen.model.repository;

import es.mjsplayground.acda.jdbc.almacen.model.entity.Producto;
import java.sql.*;
import java.util.*;

public class JdbcProductoRepository {
  public List<Producto> listar(Connection c) throws SQLException {
    // TODO 2: consultar productos y mapear cada fila con ResultSet
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: consultar productos y mapear cada fila con ResultSet");
  }

  public Producto buscar(Connection c, long id) throws SQLException {
    // TODO 3: completar CRUD parametrizado sin cerrar la conexión recibida
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: completar CRUD parametrizado sin cerrar la conexión recibida");
  }

  public long guardar(Connection c, Producto v) throws SQLException {
    // TODO 3: completar CRUD parametrizado sin cerrar la conexión recibida
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: completar CRUD parametrizado sin cerrar la conexión recibida");
  }

  public boolean descontar(Connection c, long id, int cantidad) throws SQLException {
    // TODO 4: descontar existencias solo si hay cantidad suficiente
    throw new UnsupportedOperationException(
        "Etapa pendiente: 4: descontar existencias solo si hay cantidad suficiente");
  }

  public void eliminar(Connection c, long id) throws SQLException {
    // TODO 3: completar CRUD parametrizado sin cerrar la conexión recibida
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: completar CRUD parametrizado sin cerrar la conexión recibida");
  }
}
