package es.mjsplayground.acda.ficheros.cli;

import es.mjsplayground.acda.ficheros.model.dao.XmlClienteDao;
import es.mjsplayground.acda.ficheros.model.entity.Cliente;
import es.mjsplayground.acda.ficheros.model.service.ClienteService;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

public final class ClientesBasicos {
    private static List<Cliente> csv(Path origen) throws Exception {
        List<Cliente> clientes = new ArrayList<>(); Set<String> ids = new HashSet<>();
        try (Reader in = Files.newBufferedReader(origen, StandardCharsets.UTF_8);
             CSVParser csv = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get().parse(in)) {
            if (!csv.getHeaderMap().keySet().containsAll(Set.of("id", "nombre", "ciudad")))
                throw new IllegalArgumentException("Faltan encabezados id,nombre,ciudad");
            for (var r : csv) {
                if (!r.isConsistent()) { System.err.println("Registro " + r.getRecordNumber() + ": columnas incorrectas"); continue; }
                Cliente c = new Cliente(r.get("id"), r.get("nombre"), r.get("ciudad"), "");
                String error = ClienteService.validar(c);
                if (error == null && !ids.add(c.id())) error = "Id duplicado";
                if (error == null) clientes.add(c);
                else System.err.println("Registro " + r.getRecordNumber() + ": " + error);
            }
        }
        return clientes;
    }
    private static void csv(Path destino, List<Cliente> clientes) throws Exception {
        try (Writer out = Files.newBufferedWriter(destino, StandardCharsets.UTF_8);
             CSVPrinter csv = new CSVPrinter(out, CSVFormat.RFC4180.builder().setHeader("id", "nombre", "ciudad").get())) {
            for (Cliente c : clientes) csv.printRecord(c.id(), c.nombre(), c.ciudad());
        }
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("Uso: xml-csv|csv-xml origen destino");
        Path origen = Path.of(args[1]), destino = Path.of(args[2]);
        if (args[0].equals("xml-csv")) {
            var importacion = new XmlClienteDao().leer(origen);
            List<Cliente> validos = new ArrayList<>();
            importacion.incidencias().forEach(System.err::println);
            int numero = 0;
            for (Cliente c : importacion.clientes()) {
                numero++;
                String error = ClienteService.validar(c);
                if (error == null) validos.add(c);
                else System.err.println("Registro " + numero + ": " + error);
            }
            csv(destino, validos);
        }
        else if (args[0].equals("csv-xml")) new XmlClienteDao().escribir(destino, csv(origen));
        else throw new IllegalArgumentException("Operación desconocida");
    }
}
