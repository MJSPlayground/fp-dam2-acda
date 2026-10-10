package es.mjsplayground.acda.ficheros.cli;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CopiarMuestras {
    private static final String[] ARCHIVOS = {"clientes.csv", "guiada-clientes.csv", "clientes.xml",
            "contactos-anidados.json", "almacen-norte.csv", "almacen-sur.json", "tarifas.json", "tarifas.xml"};
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Uso: carpetaExterna");
        Path carpeta = Files.createDirectories(Path.of(args[0]));
        for (String nombre : ARCHIVOS) {
            try (InputStream in = CopiarMuestras.class.getResourceAsStream("/data/" + nombre)) {
                if (in == null) throw new IllegalStateException("Falta recurso /data/" + nombre);
                Files.copy(in, carpeta.resolve(nombre));
            }
        }
        System.out.println("Muestras copiadas en " + carpeta.toAbsolutePath());
    }
}
