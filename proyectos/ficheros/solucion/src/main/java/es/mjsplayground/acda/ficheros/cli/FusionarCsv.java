package es.mjsplayground.acda.ficheros.cli;

import es.mjsplayground.acda.ficheros.model.service.FusionCsvService;
import java.nio.file.Path;
import java.util.List;

public final class FusionarCsv {
    public static void main(String[] args) throws Exception {
        if (args.length != 4) throw new IllegalArgumentException("Uso: entrada1.csv entrada2.csv ciudad salida.csv");
        FusionCsvService service = new FusionCsvService();
        var r = service.fusionar(List.of(Path.of(args[0]), Path.of(args[1])), args[2]);
        service.exportar(Path.of(args[3]), r);
        System.out.printf("Exportados: %d, incidencias: %d, total: %s%n",
                r.filtrados().size(), r.incidencias().size(), r.total());
        r.incidencias().forEach(System.err::println);
    }
}
