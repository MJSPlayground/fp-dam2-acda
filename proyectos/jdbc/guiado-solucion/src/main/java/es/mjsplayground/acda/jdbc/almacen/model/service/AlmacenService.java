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
    if (p == null
        || p.id() < 0
        || p.nombre() == null
        || p.nombre().isBlank()
        || p.nombre().strip().length() > 80
        || p.precioCentimos() <= 0
        || p.precioCentimos() > 100_000_000
        || p.stock() < 0)
      throw new ReglaNegocioException(
          "Nombre de 1 a 80 caracteres, precio entre 0,01 y 1.000.000 € y existencias no"
              + " negativas.");
    try (Connection c = db.abrir()) {
      return productos.guardar(
          c, new Producto(p.id(), p.nombre().strip(), p.precioCentimos(), p.stock()));
    } catch (SQLException e) {
      throw fallo(e);
    }
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
    if (cantidad <= 0) throw new ReglaNegocioException("La cantidad debe ser positiva.");
    try (Connection c = db.abrir()) {
      c.setAutoCommit(false);
      try {
        Producto p = productos.buscar(c, id);
        if (p == null) throw new ReglaNegocioException("Selecciona un producto existente.");
        long total = Math.multiplyExact(p.precioCentimos(), cantidad);
        if (!productos.descontar(c, id, cantidad))
          throw new ReglaNegocioException("No hay existencias suficientes.");
        ventas.registrar(c, id, cantidad, total);
        c.commit();
        log.info(
            "Venta registrada: producto={}, cantidad={}, totalCentimos={}", id, cantidad, total);
        return total;
      } catch (SQLException | RuntimeException e) {
        try {
          c.rollback();
        } catch (SQLException rollback) {
          e.addSuppressed(rollback);
        }
        throw e;
      }
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  private PersistenciaException fallo(SQLException e) {
    log.error("Fallo de persistencia en almacén", e);
    return new PersistenciaException(
        "No se pudo guardar o consultar el almacén. Comprueba nombres duplicados, permisos y el"
            + " registro técnico.",
        e);
  }
}
