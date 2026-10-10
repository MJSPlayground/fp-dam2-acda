package es.mjsplayground.acda.ficheros.cli;

import es.mjsplayground.acda.ficheros.model.entity.Resultado;
import es.mjsplayground.acda.ficheros.model.service.ClienteService;
import java.nio.file.Path;

public final class ConvertirClientes {
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("Uso: formatoEntrada archivoEntrada formatoSalida archivoSalida");
            System.exit(2);
        }
        Resultado r = new ClienteService().convertir(args[0], Path.of(args[1]), args[2], Path.of(args[3]));
        System.out.printf("Aceptados: %d, incidencias: %d%n", r.clientes().size(), r.incidencias().size());
        r.incidencias().forEach(System.out::println);
    }
}
