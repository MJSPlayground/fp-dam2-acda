package es.mjsplayground.acda.ficheros.model.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.mjsplayground.acda.ficheros.model.entity.ProductoAlmacen;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public final class InventarioComercialService {
    public record Resumen(List<ProductoAlmacen> productos, List<String> incidencias, BigDecimal valorTotal) {
        public Resumen { productos = List.copyOf(productos); incidencias = List.copyOf(incidencias); }
    }
    private final ObjectMapper mapper = new ObjectMapper();
    private static ProductoAlmacen validar(ProductoAlmacen p) {
        if (p.codigo() == null || p.codigo().isBlank() || p.nombre() == null || p.nombre().isBlank()
                || p.almacen() == null || p.almacen().isBlank() || p.unidades() < 0
                || p.precio() == null || p.precio().signum() <= 0)
            throw new IllegalArgumentException("Código, nombre, almacén, unidades o precio incorrectos");
        return p;
    }
    private List<ProductoAlmacen> leerCsv(Path archivo, List<String> errores) throws IOException {
        List<ProductoAlmacen> salida = new ArrayList<>();
        try (Reader in = Files.newBufferedReader(archivo, StandardCharsets.UTF_8);
             CSVParser csv = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get().parse(in)) {
            if (!csv.getHeaderMap().keySet().containsAll(Set.of("codigo", "nombre", "almacen", "unidades", "precio")))
                throw new IOException("Faltan encabezados obligatorios");
            for (CSVRecord r : csv) {
                try {
                    if (!r.isConsistent()) throw new IllegalArgumentException("Columnas incorrectas");
                    salida.add(validar(new ProductoAlmacen(r.get("codigo"), r.get("nombre"), r.get("almacen"),
                            Integer.parseInt(r.get("unidades")), new BigDecimal(r.get("precio")))));
                } catch (IllegalArgumentException ex) { errores.add(archivo + " [" + r.getRecordNumber() + "]: " + ex.getMessage()); }
            }
        }
        return salida;
    }
    private List<ProductoAlmacen> leerJson(Path archivo, List<String> errores) throws IOException {
        JsonNode raiz = mapper.readTree(archivo.toFile());
        if (!raiz.isArray()) throw new IOException("Se esperaba un array de productos");
        List<ProductoAlmacen> salida = new ArrayList<>();
        for (int i = 0; i < raiz.size(); i++) {
            JsonNode n = raiz.get(i);
            try {
                if (!n.isObject() || !n.path("unidades").isIntegralNumber() || !n.path("precio").isNumber())
                    throw new IllegalArgumentException("Tipos incorrectos");
                salida.add(validar(new ProductoAlmacen(n.path("codigo").asText(null), n.path("nombre").asText(null),
                        n.path("almacen").asText(null), n.path("unidades").intValue(), n.path("precio").decimalValue())));
            } catch (IllegalArgumentException ex) { errores.add(archivo + " [" + (i + 1) + "]: " + ex.getMessage()); }
        }
        return salida;
    }
    public Resumen combinar(Map<Path, String> fuentes, int umbral) {
        if (umbral < 0) throw new IllegalArgumentException("Umbral negativo");
        LinkedHashMap<String, ProductoAlmacen> combinados = new LinkedHashMap<>();
        List<String> errores = new ArrayList<>();
        for (var fuente : fuentes.entrySet()) {
            try {
                List<ProductoAlmacen> leidos = switch (fuente.getValue().toLowerCase()) {
                    case "csv" -> leerCsv(fuente.getKey(), errores);
                    case "json" -> leerJson(fuente.getKey(), errores);
                    default -> throw new IOException("Formato no admitido: " + fuente.getValue());
                };
                for (ProductoAlmacen p : leidos) {
                    ProductoAlmacen anterior = combinados.get(p.codigo());
                    if (anterior == null) combinados.put(p.codigo(), p);
                    else if (!anterior.nombre().equals(p.nombre()) || anterior.precio().compareTo(p.precio()) != 0)
                        errores.add(fuente.getKey() + ": conflicto de nombre o precio para " + p.codigo());
                    else combinados.put(p.codigo(), new ProductoAlmacen(p.codigo(), p.nombre(),
                            anterior.almacen() + "+" + p.almacen(), Math.addExact(anterior.unidades(), p.unidades()), p.precio()));
                }
            } catch (IOException ex) { errores.add(fuente.getKey() + ": " + ex.getMessage()); }
        }
        List<ProductoAlmacen> seleccionados = combinados.values().stream()
                .filter(p -> p.unidades() < umbral).sorted(Comparator.comparing(ProductoAlmacen::codigo)).toList();
        BigDecimal total = seleccionados.stream().map(p -> p.precio().multiply(BigDecimal.valueOf(p.unidades())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Resumen(seleccionados, errores, total);
    }
    public void exportar(Path destino, String formato, Resumen resumen) throws IOException {
        if (formato.equalsIgnoreCase("json")) { mapper.writerWithDefaultPrettyPrinter().writeValue(destino.toFile(), resumen); return; }
        if (!formato.equalsIgnoreCase("csv")) throw new IllegalArgumentException("Formato de salida no admitido");
        try (Writer out = Files.newBufferedWriter(destino, StandardCharsets.UTF_8);
             CSVPrinter csv = new CSVPrinter(out, CSVFormat.RFC4180.builder()
                     .setHeader("codigo", "nombre", "almacen", "unidades", "precio", "valor").get())) {
            for (ProductoAlmacen p : resumen.productos()) csv.printRecord(p.codigo(), p.nombre(), p.almacen(),
                    p.unidades(), p.precio(), p.precio().multiply(BigDecimal.valueOf(p.unidades())));
        }
    }
}
