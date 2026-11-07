package es.mjsplayground.acda.ficheros.inventario.demo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.csv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/** Experimentos separados del catálogo, con entradas y salidas externas. */
public final class AmpliacionDemo {
  private static final Logger log = LoggerFactory.getLogger(AmpliacionDemo.class);

  public record Cambio(String tipo, Path relativo) {}

  private static DocumentBuilderFactory fabricaSegura() throws Exception {
    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
    f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    f.setFeature("http://xml.org/sax/features/external-general-entities", false);
    f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    f.setXIncludeAware(false);
    f.setExpandEntityReferences(false);
    return f;
  }

  public static List<String> leerNombresXml(Path archivo) throws IOException {
    try {
      var parser = fabricaSegura().newDocumentBuilder();
      parser.setErrorHandler(
          new DefaultHandler() {
            @Override
            public void fatalError(SAXParseException e) throws SAXException {
              throw e;
            }
          });
      Document documento = parser.parse(archivo.toFile());
      Element raiz = documento.getDocumentElement();
      if (!raiz.getTagName().equals("productos")) throw new IOException("Raíz XML incorrecta");
      List<String> nombres = new ArrayList<>();
      for (Node nodo = raiz.getFirstChild(); nodo != null; nodo = nodo.getNextSibling()) {
        if (!(nodo instanceof Element producto) || !producto.getTagName().equals("producto"))
          continue;
        Element nombre = null;
        for (Node hijo = producto.getFirstChild(); hijo != null; hijo = hijo.getNextSibling()) {
          if (hijo instanceof Element e && e.getTagName().equals("nombre")) {
            if (nombre != null) throw new IOException("Nombre XML repetido");
            nombre = e;
          }
        }
        if (nombre == null || nombre.getTextContent().isBlank())
          throw new IOException("Falta nombre XML");
        nombres.add(nombre.getTextContent());
      }
      return List.copyOf(nombres);
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException("XML inválido o configuración segura no disponible", e);
    }
  }

  public static void escribirNombresXml(Path archivo, List<String> nombres) throws IOException {
    try {
      Document documento = fabricaSegura().newDocumentBuilder().newDocument();
      Element raiz = documento.createElement("productos");
      documento.appendChild(raiz);
      for (String texto : nombres) {
        Element producto = documento.createElement("producto");
        Element nombre = documento.createElement("nombre");
        nombre.setTextContent(texto);
        producto.appendChild(nombre);
        raiz.appendChild(producto);
      }
      TransformerFactory fabrica = TransformerFactory.newInstance();
      fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
      Transformer transformador = fabrica.newTransformer();
      transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformador.setOutputProperty(OutputKeys.INDENT, "yes");
      try (OutputStream salida = Files.newOutputStream(archivo, StandardOpenOption.CREATE_NEW)) {
        transformador.transform(new DOMSource(documento), new StreamResult(salida));
      }
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException("No se pudo generar el XML", e);
    }
  }

  /** Transforma XML de nombres en CSV de una columna; no importa un catálogo completo. */
  public static void nombresXmlACsv(Path origen, Path destino) throws IOException {
    List<String> nombres = leerNombresXml(origen);
    try (Writer salida =
            Files.newBufferedWriter(
                destino, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        CSVPrinter csv =
            new CSVPrinter(salida, CSVFormat.RFC4180.builder().setHeader("nombre").get())) {
      for (String nombre : nombres) csv.printRecord(nombre);
    }
  }

  /** Procesa registros uno a uno, incluso si una celda contiene saltos de línea. */
  public static long filtrarCsv(Path origen, Path destino, int minimo) throws IOException {
    if (minimo < 0) throw new IllegalArgumentException("Mínimo negativo");
    CSVFormat formato = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get();
    try (Reader entrada = Files.newBufferedReader(origen, StandardCharsets.UTF_8);
        CSVParser registros = formato.parse(entrada)) {
      if (!registros
          .getHeaderMap()
          .keySet()
          .containsAll(Set.of("codigo", "nombre", "unidades", "precio"))) {
        throw new IOException("Faltan encabezados del catálogo");
      }
      try (Writer salida =
              Files.newBufferedWriter(
                  destino, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
          CSVPrinter csv =
              new CSVPrinter(
                  salida,
                  CSVFormat.RFC4180
                      .builder()
                      .setHeader(registros.getHeaderNames().toArray(String[]::new))
                      .get())) {
        long aceptados = 0;
        for (CSVRecord fila : registros) {
          if (!fila.isConsistent())
            throw new IOException("Columnas incorrectas en registro " + fila.getRecordNumber());
          try {
            if (Integer.parseInt(fila.get("unidades")) >= minimo) {
              csv.printRecord(fila);
              aceptados++;
            }
          } catch (NumberFormatException e) {
            throw new IOException("Unidades inválidas en registro " + fila.getRecordNumber(), e);
          }
        }
        return aceptados;
      }
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }

  public static WatchService registrar(Path carpeta) throws IOException {
    WatchService vigilancia = carpeta.getFileSystem().newWatchService();
    try {
      carpeta.register(
          vigilancia,
          StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_MODIFY,
          StandardWatchEventKinds.ENTRY_DELETE);
      return vigilancia;
    } catch (IOException | RuntimeException e) {
      vigilancia.close();
      throw e;
    }
  }

  public static List<Cambio> esperarEventos(WatchService vigilancia, long milisegundos)
      throws InterruptedException {
    WatchKey clave = vigilancia.poll(milisegundos, TimeUnit.MILLISECONDS);
    if (clave == null) return List.of();
    List<Cambio> cambios = new ArrayList<>();
    for (WatchEvent<?> evento : clave.pollEvents()) {
      cambios.add(
          new Cambio(evento.kind().name(), evento.context() instanceof Path ruta ? ruta : null));
    }
    if (!clave.reset()) cambios.add(new Cambio("CLAVE_NO_VALIDA", null));
    return List.copyOf(cambios);
  }

  /** Devuelve si se utilizó movimiento atómico; la alternativa no promete esa garantía. */
  public static boolean escribirFiable(Path destino, String texto) throws IOException {
    Path absoluto = destino.toAbsolutePath();
    Files.createDirectories(absoluto.getParent());
    Path temporal = Files.createTempFile(absoluto.getParent(), ".demo-", ".tmp");
    try {
      try (Writer salida = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
        salida.write(texto);
      }
      try {
        Files.move(
            temporal,
            absoluto,
            StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING);
        return true;
      } catch (AtomicMoveNotSupportedException e) {
        Files.move(temporal, absoluto, StandardCopyOption.REPLACE_EXISTING);
        return false;
      }
    } finally {
      Files.deleteIfExists(temporal);
    }
  }

  public static void main(String[] args) throws Exception {
    Path carpeta = Files.createTempDirectory("inventario-ampliacion-demo-");
    Path xml = carpeta.resolve("nombres.xml");
    escribirNombresXml(xml, List.of("Taza, blanca", "Caja & soporte"));
    nombresXmlACsv(xml, carpeta.resolve("nombres.csv"));
    try (WatchService vigilancia = registrar(carpeta)) {
      boolean atomico = escribirFiable(carpeta.resolve("nota.txt"), "Demostración UTF-8: revisión");
      log.info(
          "Carpeta {}, nombres {}, sustitución atómica {}, eventos {}",
          carpeta,
          leerNombresXml(xml),
          atomico,
          esperarEventos(vigilancia, 3000));
    }
  }
}
