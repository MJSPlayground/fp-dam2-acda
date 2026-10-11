package es.mjsplayground.acda.ficheros.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.stream.Stream;

public final class OrganizarDirectorios {
    public static void main(String[] args) throws IOException {
        if (args.length < 2 || args.length > 3 || (args.length == 3 && !"--reemplazar".equals(args[2])))
            throw new IllegalArgumentException("Uso: carpetaEntrada carpetaTrabajo [--reemplazar]");
        Path origen = Path.of(args[0]), trabajo = Path.of(args[1]);
        if (!Files.isDirectory(origen)) throw new IOException("No es un directorio: " + origen);
        Path copias = Files.createDirectories(trabajo.resolve("copias"));
        Path muestra = origen.resolve("clientes.csv");
        if (args.length == 3) Files.copy(muestra, copias.resolve("clientes.csv"), StandardCopyOption.REPLACE_EXISTING);
        else Files.copy(muestra, copias.resolve("clientes.csv"));
        try (Stream<Path> rutas = Files.walk(origen, 3)) {
            for (Path ruta : rutas.filter(Files::isRegularFile).sorted().toList()) {
                if (ruta.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv"))
                    System.out.printf("%s: %d bytes%n", origen.relativize(ruta), Files.size(ruta));
            }
        }
    }
}
