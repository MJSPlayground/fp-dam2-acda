package es.mjsplayground.acda.ficheros.model.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.mjsplayground.acda.ficheros.model.entity.Producto;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class ProductoFormatos {
    public record Resultado(List<Producto> productos, List<String> incidencias) {
        public Resultado { productos = List.copyOf(productos); incidencias = List.copyOf(incidencias); }
    }
    private final ObjectMapper mapper = new ObjectMapper();
    private static Producto crear(String codigo, String nombre, String unidades, String precio) {
        if (unidades == null || precio == null) throw new IllegalArgumentException("Faltan unidades o precio");
        Producto p = new Producto(codigo, nombre, Integer.parseInt(unidades), new BigDecimal(precio));
        if (p.codigo() == null || p.nombre() == null) throw new IllegalArgumentException("Faltan código o nombre");
        return p;
    }
    private static DocumentBuilderFactory segura() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setFeature("http://xml.org/sax/features/external-general-entities", false);
        f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        f.setXIncludeAware(false); f.setExpandEntityReferences(false);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return f;
    }
    private static String hijo(Element padre, String nombre) {
        for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling())
            if (n instanceof Element e && e.getTagName().equals(nombre)) return e.getTextContent();
        return null;
    }
    public Resultado leer(Path origen, String formato) throws IOException {
        List<Producto> productos = new ArrayList<>(); List<String> errores = new ArrayList<>();
        switch (formato) {
            case "csv" -> {
                try (Reader in = Files.newBufferedReader(origen, StandardCharsets.UTF_8);
                     CSVParser csv = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get().parse(in)) {
                    if (!csv.getHeaderMap().keySet().containsAll(Set.of("codigo", "nombre", "unidades", "precio")))
                        throw new IOException("Faltan encabezados de productos");
                    for (var r : csv) {
                        try {
                            if (!r.isConsistent()) throw new IllegalArgumentException("Columnas incorrectas");
                            productos.add(crear(r.get("codigo"), r.get("nombre"), r.get("unidades"), r.get("precio")));
                        } catch (IllegalArgumentException ex) { errores.add(origen + " [" + r.getRecordNumber() + "]: " + ex.getMessage()); }
                    }
                }
            }
            case "json" -> {
                JsonNode raiz = mapper.readTree(origen.toFile());
                if (!raiz.isArray()) throw new IOException("Se esperaba un array de productos");
                for (int i = 0; i < raiz.size(); i++) {
                    JsonNode p = raiz.get(i);
                    try {
                        if (!p.isObject() || !p.path("codigo").isTextual() || !p.path("nombre").isTextual()
                                || !p.path("unidades").isIntegralNumber() || !p.path("precio").isNumber())
                            throw new IllegalArgumentException("Tipos incorrectos");
                        productos.add(crear(p.get("codigo").textValue(), p.get("nombre").textValue(),
                                p.get("unidades").asText(), p.get("precio").asText()));
                    } catch (IllegalArgumentException ex) { errores.add(origen + " [" + (i + 1) + "]: " + ex.getMessage()); }
                }
            }
            case "xml" -> {
                try {
                    var builder = segura().newDocumentBuilder();
                    builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
                        @Override public void fatalError(org.xml.sax.SAXParseException ex) throws org.xml.sax.SAXException { throw ex; }
                    });
                    var d = builder.parse(origen.toFile());
                    if (!d.getDocumentElement().getTagName().equals("productos")) throw new IOException("Raíz XML incorrecta");
                    var nodos = d.getDocumentElement().getChildNodes(); int numero = 0;
                    for (int i = 0; i < nodos.getLength(); i++) {
                        if (!(nodos.item(i) instanceof Element p) || !p.getTagName().equals("producto")) continue;
                        numero++;
                        try { productos.add(crear(p.getAttribute("codigo"), hijo(p, "nombre"), hijo(p, "unidades"), hijo(p, "precio"))); }
                        catch (IllegalArgumentException ex) { errores.add(origen + " [" + numero + "]: " + ex.getMessage()); }
                    }
                } catch (IOException ex) { throw ex; }
                  catch (Exception ex) { throw new IOException("XML incorrecto o parser inseguro", ex); }
            }
            default -> throw new IllegalArgumentException("Formato de producto desconocido: " + formato);
        }
        return new Resultado(productos, errores);
    }
    private static void hijo(org.w3c.dom.Document d, Element padre, String nombre, String valor) {
        Element e = d.createElement(nombre); e.setTextContent(valor); padre.appendChild(e);
    }
    public void escribir(Path destino, String formato, List<Producto> productos) throws IOException {
        switch (formato) {
            case "csv" -> {
                try (Writer out = Files.newBufferedWriter(destino, StandardCharsets.UTF_8);
                     CSVPrinter csv = new CSVPrinter(out, CSVFormat.RFC4180.builder()
                             .setHeader("codigo", "nombre", "unidades", "precio").get())) {
                    for (Producto p : productos) csv.printRecord(p.codigo(), p.nombre(), p.unidades(), p.precio());
                }
            }
            case "json" -> mapper.writerWithDefaultPrettyPrinter().writeValue(destino.toFile(), productos);
            case "xml" -> {
                try {
                    var d = segura().newDocumentBuilder().newDocument(); Element raiz = d.createElement("productos"); d.appendChild(raiz);
                    for (Producto p : productos) {
                        Element e = d.createElement("producto"); e.setAttribute("codigo", p.codigo()); raiz.appendChild(e);
                        hijo(d, e, "nombre", p.nombre()); hijo(d, e, "unidades", Integer.toString(p.unidades()));
                        hijo(d, e, "precio", p.precio().toPlainString());
                    }
                    TransformerFactory tf = TransformerFactory.newInstance();
                    tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
                    var t = tf.newTransformer(); t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                    try (var out = Files.newOutputStream(destino)) { t.transform(new DOMSource(d), new StreamResult(out)); }
                } catch (Exception ex) { throw new IOException("No se pudo escribir XML", ex); }
            }
            default -> throw new IllegalArgumentException("Formato de producto desconocido: " + formato);
        }
    }
}
