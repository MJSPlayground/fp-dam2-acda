package es.mjsplayground.acda.ficheros.cli;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

/** Demostración solo para archivos creados por este mismo programa. */
public final class SerializacionDemo {
    public record Ficha(String id, String nombre) implements Serializable {
        private static final long serialVersionUID = 1L;
    }
    public static void escribir(Path archivo, Ficha ficha) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(archivo))) {
            out.writeObject(ficha);
        }
    }
    public static Ficha leerPropio(Path archivo) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(archivo))) {
            ObjectInputFilter filtro = ObjectInputFilter.Config.createFilter(
                    "maxdepth=5;maxrefs=20;maxbytes=10000;es.mjsplayground.acda.ficheros.cli.SerializacionDemo$Ficha;java.base/*;!*");
            in.setObjectInputFilter(filtro);
            return (Ficha) in.readObject();
        }
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Uso: archivoExterno.bin");
        Path archivo = Path.of(args[0]);
        escribir(archivo, new Ficha("C01", "Ana"));
        System.out.println(leerPropio(archivo));
    }
}
