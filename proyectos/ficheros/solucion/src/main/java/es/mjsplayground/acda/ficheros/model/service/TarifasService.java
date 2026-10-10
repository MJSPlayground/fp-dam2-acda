package es.mjsplayground.acda.ficheros.model.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class TarifasService {
    public record Tarifa(String codigo, String nombre, String categoria, String tarifa, BigDecimal importe) {}
    private record Clave(String codigo, String tarifa) {}
    public record Resultado(List<Tarifa> filas, List<String> incidencias) {
        public Resultado { filas = List.copyOf(filas); incidencias = List.copyOf(incidencias); }
    }
    private final ObjectMapper mapper = new ObjectMapper();
    private static String texto(JsonNode n, String clave) {
        JsonNode v = n.path(clave); return v.isTextual() ? v.textValue() : null;
    }
    private static String hijo(Element padre, String nombre) {
        for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling())
            if (n instanceof Element e && e.getTagName().equals(nombre)) return e.getTextContent();
        return null;
    }
    private static List<Element> hijos(Element padre, String nombre) {
        List<Element> elementos = new ArrayList<>();
        for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling())
            if (n instanceof Element e && e.getTagName().equals(nombre)) elementos.add(e);
        return elementos;
    }
    private static void agregar(Map<Clave, Tarifa> unicos, List<String> errores, Tarifa t, String origen) {
        if (t.codigo() == null || t.codigo().isBlank() || t.nombre() == null || t.nombre().isBlank()
                || t.categoria() == null || t.categoria().isBlank() || t.tarifa() == null || t.tarifa().isBlank()
                || t.importe() == null || t.importe().signum() < 0) {
            errores.add(origen + ": campos obligatorios o importe incorrectos"); return;
        }
        t = new Tarifa(t.codigo(), t.nombre(), t.categoria(), t.tarifa(), t.importe().stripTrailingZeros());
        if (unicos.putIfAbsent(new Clave(t.codigo(), t.tarifa()), t) != null)
            errores.add(origen + ": código y tarifa duplicados");
    }
    public Resultado leerJson(Path origen, String categoria) throws IOException {
        JsonNode raiz = mapper.readTree(origen.toFile());
        if (!raiz.isArray()) throw new IOException("Se esperaba un array de productos");
        Map<Clave, Tarifa> unicos = new LinkedHashMap<>(); List<String> errores = new ArrayList<>();
        for (int i = 0; i < raiz.size(); i++) {
            JsonNode p = raiz.get(i); String base = origen + " [" + (i + 1) + "]";
            if (!p.isObject() || !p.path("precios").isArray()) { errores.add(base + ": producto o precios incorrectos"); continue; }
            if (texto(p, "codigo") == null || texto(p, "categoria") == null) {
                errores.add(base + ": código o categoría ausente"); continue;
            }
            if (!categoria.equalsIgnoreCase(texto(p, "categoria"))) continue;
            for (JsonNode precio : p.path("precios")) {
                BigDecimal importe = precio.path("importe").isNumber() ? precio.path("importe").decimalValue() : null;
                agregar(unicos, errores, new Tarifa(texto(p, "codigo"), texto(p, "nombre"), texto(p, "categoria"),
                        texto(precio, "tarifa"), importe), base);
            }
        }
        return new Resultado(new ArrayList<>(unicos.values()), errores);
    }
    public Resultado leerXml(Path origen, String categoria) throws IOException {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            f.setXIncludeAware(false); f.setExpandEntityReferences(false);
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var builder = f.newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
                @Override public void fatalError(org.xml.sax.SAXParseException ex) throws org.xml.sax.SAXException { throw ex; }
            });
            var d = builder.parse(origen.toFile());
            if (!d.getDocumentElement().getTagName().equals("productos")) throw new IOException("Raíz XML incorrecta");
            Map<Clave, Tarifa> unicos = new LinkedHashMap<>(); List<String> errores = new ArrayList<>();
            int numero = 0;
            for (Element p : hijos(d.getDocumentElement(), "producto")) {
                numero++;
                if (p.getAttribute("codigo").isBlank() || hijo(p, "categoria") == null) {
                    errores.add(origen + " [" + numero + "]: código o categoría ausente"); continue;
                }
                if (!categoria.equalsIgnoreCase(hijo(p, "categoria"))) continue;
                for (Element precio : hijos(p, "precio")) {
                    BigDecimal importe;
                    try { importe = new BigDecimal(precio.getTextContent().trim()); }
                    catch (NumberFormatException ex) { importe = null; }
                    agregar(unicos, errores, new Tarifa(p.getAttribute("codigo"), hijo(p, "nombre"),
                            hijo(p, "categoria"), precio.getAttribute("tarifa"), importe), origen + " [" + numero + "]");
                }
            }
            return new Resultado(new ArrayList<>(unicos.values()), errores);
        } catch (IOException ex) { throw ex; }
          catch (Exception ex) { throw new IOException("XML inválido o parser inseguro: " + origen, ex); }
    }
    public void escribirCsv(Path destino, Resultado resultado) throws IOException {
        try (Writer out = Files.newBufferedWriter(destino, StandardCharsets.UTF_8);
             CSVPrinter csv = new CSVPrinter(out, CSVFormat.RFC4180.builder()
                     .setHeader("codigo", "nombre", "categoria", "tarifa", "importe").get())) {
            for (Tarifa t : resultado.filas()) csv.printRecord(t.codigo(), t.nombre(), t.categoria(), t.tarifa(), t.importe());
        }
    }
    public void reconstruirJson(Path csvOrigen, Path jsonDestino) throws IOException {
        ObjectNode raiz = mapper.createObjectNode(); ArrayNode productos = raiz.putArray("productos");
        Map<String, ObjectNode> mapa = new LinkedHashMap<>();
        try (Reader in = Files.newBufferedReader(csvOrigen, StandardCharsets.UTF_8);
             CSVParser csv = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get().parse(in)) {
            if (!csv.getHeaderMap().keySet().containsAll(Set.of("codigo", "nombre", "categoria", "tarifa", "importe")))
                throw new IOException("Faltan encabezados para reconstruir JSON");
            for (var r : csv) {
                ObjectNode p = mapa.get(r.get("codigo"));
                if (p == null) {
                    p = productos.addObject(); p.put("codigo", r.get("codigo")); p.put("nombre", r.get("nombre"));
                    p.put("categoria", r.get("categoria")); p.putArray("precios"); mapa.put(r.get("codigo"), p);
                }
                ObjectNode precio = ((ArrayNode) p.get("precios")).addObject();
                precio.put("tarifa", r.get("tarifa")); precio.put("importe", new BigDecimal(r.get("importe")));
            }
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonDestino.toFile(), raiz);
    }
}
