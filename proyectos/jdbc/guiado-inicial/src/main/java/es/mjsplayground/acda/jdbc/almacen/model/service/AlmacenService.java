package es.mjsplayground.acda.jdbc.almacen.model.service;

import es.mjsplayground.acda.jdbc.almacen.config.Database;
import es.mjsplayground.acda.jdbc.almacen.model.entity.*;
import es.mjsplayground.acda.jdbc.almacen.model.repository.*;
import java.sql.*;
import java.util.*;
import org.slf4j.*;

public class AlmacenService {
  private static final Logger log = LoggerFactory.getLogger(AlmacenService.class);
  private final Database db;
  private final JdbcProductoRepository productos;
  private final JdbcVentaRepository ventas;

  public AlmacenService(Database db) {
    this(db, new JdbcProductoRepository(), new JdbcVentaRepository());
  }

  public AlmacenService(Database db, JdbcProductoRepository productos, JdbcVentaRepository ventas) {
    this.db = db;
    this.productos = productos;
    this.ventas = ventas;
  }

  public List<Producto> listar() {
    try (Connection c = db.abrir()) {
      return productos.listar(c);
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  public List<Venta> ventas() {
    try (Connection c = db.abrir()) {
      return ventas.listar(c);
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  public long guardar(Producto p) {
    // TODO 3: validar nombre, precio y stock antes de delegar la escritura
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: validar nombre, precio y stock antes de delegar la escritura");
  }

  public void eliminar(long id) {
    try (Connection c = db.abrir()) {
      if (ventas.existenPara(c, id))
        throw new ReglaNegocioException("Conserva los productos que tienen ventas registradas.");
      productos.eliminar(c, id);
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  public long vender(long id, int cantidad) {
    // TODO 5: validar cantidad y coordinar descuento y venta en una transacción
    throw new UnsupportedOperationException(
        "Etapa pendiente: 5: validar cantidad y coordinar descuento y venta en una transacción");
  }

  private PersistenciaException fallo(SQLException e) {
    log.error("Fallo de persistencia en almacén", e);
    return new PersistenciaException(
        "No se pudo guardar o consultar el almacén. Comprueba nombres duplicados, permisos y el"
            + " registro técnico.",
        e);
  }
}
