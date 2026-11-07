package es.mjsplayground.acda.jdbc.almacen.model.service;

import es.mjsplayground.acda.jdbc.almacen.config.Database;
import es.mjsplayground.acda.jdbc.almacen.model.entity.*;
import es.mjsplayground.acda.jdbc.almacen.model.repository.*;
import java.math.*;
import java.sql.*;
import java.util.*;
import org.slf4j.*;

public class OperacionesService {
  private static final Logger log = LoggerFactory.getLogger(OperacionesService.class);
  private final Database db;
  private final JdbcRecepcionRepository recepciones;
  private final JdbcProductoRepository productos = new JdbcProductoRepository();

  public OperacionesService(Database db) {
    this(db, new JdbcRecepcionRepository());
  }

  public OperacionesService(Database db, JdbcRecepcionRepository recepciones) {
    this.db = db;
    this.recepciones = recepciones;
  }

  public void preparar() {
    try (Connection c = db.abrir()) {
      recepciones.preparar(c);
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  public List<Producto> buscar(String texto, int maximo) {
    if (texto == null || maximo < 0)
      throw new ReglaNegocioException("Escribe un filtro y un máximo de existencias no negativo.");
    String filtro = texto.strip().toLowerCase(Locale.ROOT);
    try (Connection c = db.abrir()) {
      return recepciones.bajoMaximo(c, maximo).stream()
          .filter(p -> p.nombre().toLowerCase(Locale.ROOT).contains(filtro))
          .toList();
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  public long revisarPrecio(long id, BigDecimal porcentaje) {
    if (porcentaje == null
        || porcentaje.stripTrailingZeros().scale() > 2
        || porcentaje.compareTo(BigDecimal.valueOf(-50)) < 0
        || porcentaje.compareTo(BigDecimal.valueOf(100)) > 0)
      throw new ReglaNegocioException(
          "El porcentaje debe estar entre -50 y 100, con hasta dos decimales.");
    try (Connection c = db.abrir()) {
      c.setAutoCommit(false);
      try {
        Producto p = productos.buscar(c, id);
        if (p == null) throw new ReglaNegocioException("Selecciona un producto existente.");
        long nuevo =
            BigDecimal.valueOf(p.precioCentimos())
                .multiply(BigDecimal.ONE.add(porcentaje.movePointLeft(2)))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        if (nuevo <= 0 || nuevo > 100_000_000)
          throw new ReglaNegocioException("El nuevo precio debe estar entre 0,01 y 1.000.000 €.");
        recepciones.precio(c, id, nuevo);
        c.commit();
        return nuevo;
      } catch (SQLException | RuntimeException e) {
        revertir(c, e);
        throw e;
      }
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  public long recibirUna(LineaRecepcion linea) {
    return recibir(linea == null ? null : List.of(linea), Long.MAX_VALUE);
  }

  public long recibir(List<LineaRecepcion> lineas, long presupuesto) {
    if (lineas == null || lineas.isEmpty() || presupuesto <= 0)
      throw new ReglaNegocioException("Añade al menos una línea y un presupuesto positivo.");
    Set<Long> ids = new HashSet<>();
    long total = 0;
    try {
      for (LineaRecepcion l : lineas) {
        if (l == null
            || l.cantidad() <= 0
            || l.costeUnitarioCentimos() <= 0
            || l.costeUnitarioCentimos() > 100_000_000
            || !ids.add(l.productoId()))
          throw new ReglaNegocioException(
              "Cada producto debe aparecer una vez, con unidades positivas y coste entre 0,01 y"
                  + " 1.000.000 €.");
        total = Math.addExact(total, Math.multiplyExact(l.costeUnitarioCentimos(), l.cantidad()));
      }
    } catch (ArithmeticException e) {
      throw new ReglaNegocioException("El total de la entrega es demasiado grande.");
    }
    if (total > presupuesto)
      throw new ReglaNegocioException("La entrega supera el presupuesto autorizado.");
    try (Connection c = db.abrir()) {
      c.setAutoCommit(false);
      try {
        for (LineaRecepcion l : lineas) {
          Producto p = productos.buscar(c, l.productoId());
          if (p == null) throw new ReglaNegocioException("Uno de los productos ya no existe.");
          try {
            Math.addExact(p.stock(), l.cantidad());
          } catch (ArithmeticException e) {
            throw new ReglaNegocioException("Las existencias resultantes son demasiado grandes.");
          }
          recepciones.incrementar(c, l.productoId(), l.cantidad());
          recepciones.registrar(c, l, Math.multiplyExact(l.costeUnitarioCentimos(), l.cantidad()));
        }
        c.commit();
        log.info("Entrega confirmada: líneas={}, totalCentimos={}", lineas.size(), total);
        return total;
      } catch (SQLException | RuntimeException e) {
        revertir(c, e);
        throw e;
      }
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  public List<Recepcion> historial() {
    try (Connection c = db.abrir()) {
      return recepciones.historial(c);
    } catch (SQLException e) {
      throw fallo(e);
    }
  }

  private void revertir(Connection c, Exception causa) {
    try {
      c.rollback();
    } catch (SQLException e) {
      causa.addSuppressed(e);
    }
  }

  private PersistenciaException fallo(SQLException e) {
    log.error("Error al registrar operaciones de almacén", e);
    return new PersistenciaException(
        "No se pudo completar la operación. Consulta el registro técnico.", e);
  }
}
