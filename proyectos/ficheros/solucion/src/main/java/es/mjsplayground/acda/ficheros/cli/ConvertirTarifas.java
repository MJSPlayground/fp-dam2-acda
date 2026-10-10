package es.mjsplayground.acda.ficheros.cli;

import es.mjsplayground.acda.ficheros.model.service.TarifasService;
import java.nio.file.Path;

public final class ConvertirTarifas {
    public static void main(String[] args) throws Exception {
        if (args.length != 4) throw new IllegalArgumentException("Uso: json|xml origen categoría salida.csv");
        TarifasService service = new TarifasService();
        var r = switch (args[0]) {
            case "json" -> service.leerJson(Path.of(args[1]), args[2]);
            case "xml" -> service.leerXml(Path.of(args[1]), args[2]);
            default -> throw new IllegalArgumentException("Formato de entrada desconocido");
        };
        service.escribirCsv(Path.of(args[3]), r);
        System.out.println("Precios exportados: " + r.filas().size());
        r.incidencias().forEach(System.err::println);
    }
}
