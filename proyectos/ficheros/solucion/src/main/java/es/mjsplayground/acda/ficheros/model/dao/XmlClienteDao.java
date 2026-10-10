package es.mjsplayground.acda.ficheros.model.dao;

import es.mjsplayground.acda.ficheros.model.entity.Cliente;
import es.mjsplayground.acda.ficheros.model.entity.Importacion;
import es.mjsplayground.acda.ficheros.model.entity.Incidencia;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public final class XmlClienteDao implements ClienteDao {
    private static DocumentBuilderFactory segura() throws ParserConfigurationException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setFeature("http://xml.org/sax/features/external-general-entities", false);
        f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        f.setXIncludeAware(false);
        f.setExpandEntityReferences(false);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return f;
    }
    private static String hijo(Element padre, String nombre) {
        for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling())
            if (n instanceof Element e && e.getTagName().equals(nombre)) return e.getTextContent();
        return null;
    }
    @Override public Importacion leer(Path origen) throws IOException {
        try (InputStream in = Files.newInputStream(origen)) {
            var builder = segura().newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
                @Override public void fatalError(org.xml.sax.SAXParseException ex) throws SAXException { throw ex; }
            });
            Document d = builder.parse(in);
            if (!d.getDocumentElement().getTagName().equals("clientes")) throw new IOException("Raíz XML distinta de clientes");
            List<Cliente> clientes = new ArrayList<>();
            List<Incidencia> incidencias = new ArrayList<>();
            var nodos = d.getDocumentElement().getChildNodes();
            for (int i = 0, numero = 0; i < nodos.getLength(); i++) {
                if (!(nodos.item(i) instanceof Element e) || !e.getTagName().equals("cliente")) continue;
                numero++;
                String nombre = hijo(e, "nombre"), ciudad = hijo(e, "ciudad");
                if (nombre == null || ciudad == null) {
                    incidencias.add(new Incidencia(origen.toString(), numero, "Falta nombre o ciudad"));
                } else clientes.add(new Cliente(e.getAttribute("id"), nombre, ciudad, hijo(e, "correo")));
            }
            return new Importacion(clientes, incidencias);
        } catch (ParserConfigurationException | SAXException ex) {
            throw new IOException("XML no válido o parser inseguro: " + origen, ex);
        }
    }
    private static void agregar(Document d, Element padre, String nombre, String valor) {
        Element e = d.createElement(nombre);
        e.setTextContent(valor == null ? "" : valor);
        padre.appendChild(e);
    }
    @Override public void escribir(Path destino, List<Cliente> clientes) throws IOException {
        try {
            Document d = segura().newDocumentBuilder().newDocument();
            Element raiz = d.createElement("clientes"); d.appendChild(raiz);
            for (Cliente c : clientes) {
                Element e = d.createElement("cliente"); e.setAttribute("id", c.id()); raiz.appendChild(e);
                agregar(d, e, "nombre", c.nombre()); agregar(d, e, "ciudad", c.ciudad());
                if (c.correo() != null && !c.correo().isBlank()) agregar(d, e, "correo", c.correo());
            }
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            try (OutputStream out = Files.newOutputStream(destino)) { t.transform(new DOMSource(d), new StreamResult(out)); }
        } catch (Exception ex) { throw new IOException("No se pudo escribir XML: " + destino, ex); }
    }
}
