package es.mjsplayground.acda.ficheros.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public final class JsonContactosACsv {
    private static String requerido(JsonNode nodo, String nombre) {
        JsonNode valor = nodo.path(nombre);
        if (!valor.isTextual() || valor.textValue().isBlank()) throw new IllegalArgumentException("Falta " + nombre);
        return valor.textValue();
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Uso: entrada.json salida.csv");
        JsonNode clientes = new ObjectMapper().readTree(Path.of(args[0]).toFile()).path("clientes");
        if (!clientes.isArray()) throw new IllegalArgumentException("Se esperaba un array clientes");
        try (var out = Files.newBufferedWriter(Path.of(args[1]), StandardCharsets.UTF_8);
             CSVPrinter csv = new CSVPrinter(out, CSVFormat.RFC4180.builder()
                     .setHeader("id", "nombre", "ciudad", "telefono").get())) {
            int numero = 0;
            for (JsonNode c : clientes) {
                numero++;
                try {
                    JsonNode contacto = c.path("contacto");
                    JsonNode telefono = c.path("telefono");
                    if (!telefono.isMissingNode() && !telefono.isNull() && !telefono.isTextual())
                        throw new IllegalArgumentException("Teléfono no textual");
                    csv.printRecord(requerido(c, "id"), requerido(contacto, "nombre"),
                            requerido(contacto, "ciudad"), telefono.isTextual() ? telefono.textValue() : "");
                } catch (IllegalArgumentException ex) { System.err.println("Registro " + numero + ": " + ex.getMessage()); }
            }
        }
    }
}
