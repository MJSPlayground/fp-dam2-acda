package es.mjsplayground.acda.ficheros.codelab;

import java.nio.file.Path;

/** Completa este programa siguiendo la práctica guiada. */
public final class InventarioCodelab {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("Uso: carpetaEntrada carpetaTrabajo extension");
        Path origen = Path.of(args[0]);
        Path trabajo = Path.of(args[1]);
        String extension = args[2];
        System.out.println("Directorio de trabajo: " + Path.of("").toAbsolutePath());
        System.out.println("Entrada: " + origen.toAbsolutePath().normalize());
        // TODO guiado: crear copias, recorrer, registrar filas y escribir el informe.
        System.out.println("Carpeta de resultados: " + trabajo.toAbsolutePath().normalize());
        System.out.println("Filtro pedido: " + extension);
    }
}
