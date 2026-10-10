package es.mjsplayground.acda.ficheros.model.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.mjsplayground.acda.ficheros.model.entity.ResumenOperacion;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.w3c.dom.Element;

public final class InformeOperacionesDao {
    public void escribir(Path destino, String formato, List<ResumenOperacion> resumen) throws IOException {
        switch (formato) {
            case "csv" -> {
                try (var out = Files.newBufferedWriter(destino, StandardCharsets.UTF_8);
                     CSVPrinter csv = new CSVPrinter(out, CSVFormat.RFC4180.builder()
                             .setHeader("cliente", "producto", "unidades", "importe").get())) {
                    for (var r : resumen) csv.printRecord(r.clienteId(), r.productoCodigo(), r.unidades(), r.importe());
                }
            }
            case "json" -> new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(destino.toFile(), resumen);
            case "xml" -> {
                try {
                    var d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                    Element raiz = d.createElement("resumen"); d.appendChild(raiz);
                    for (var r : resumen) {
                        Element fila = d.createElement("fila"); fila.setAttribute("cliente", r.clienteId());
                        fila.setAttribute("producto", r.productoCodigo()); fila.setAttribute("unidades", Long.toString(r.unidades()));
                        fila.setAttribute("importe", r.importe().toPlainString()); raiz.appendChild(fila);
                    }
                    TransformerFactory tf = TransformerFactory.newInstance();
                    tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
                    var t = tf.newTransformer(); t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                    try (var out = Files.newOutputStream(destino)) { t.transform(new DOMSource(d), new StreamResult(out)); }
                } catch (Exception ex) { throw new IOException("No se pudo generar el informe XML", ex); }
            }
            default -> throw new IllegalArgumentException("Formato de informe desconocido: " + formato);
        }
    }
}
