package es.mjsplayground.acda.ficheros.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

public final class OrganizarDirectorios {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) throw new IllegalArgumentException("Uso: carpetaEntrada carpetaTrabajo");
        Path origen = Path.of(args[0]), trabajo = Path.of(args[1]);
        Path copias = Files.createDirectories(trabajo.resolve("copias"));
        Files.copy(origen.resolve("clientes.csv"), copias.resolve("clientes.csv"));
        try (Stream<Path> rutas = Files.walk(origen, 3)) {
            for (Path ruta : rutas.filter(Files::isRegularFile).sorted().toList()) {
                if (ruta.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv"))
                    System.out.printf("%s: %d bytes%n", origen.relativize(ruta), Files.size(ruta));
            }
        }
    }
}
