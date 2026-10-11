package es.mjsplayground.acda.ficheros.codelab;

import java.nio.file.Path;

/** Completa este programa siguiendo la práctica guiada. */
public final class InventarioCodelab {
    public static void main(String[] args) throws Exception {
        if (args.length != 0 && args.length != 3 && args.length != 4)
            throw new IllegalArgumentException("Uso: [carpetaEntrada carpetaTrabajo extension [--reemplazar]]");
        Path origen = Path.of(args.length == 0 ? "trabajo-codelab/entrada" : args[0]);
        Path trabajo = Path.of(args.length == 0 ? "trabajo-codelab/salida" : args[1]);
        String extension = args.length == 0 ? "csv" : args[2];
        if (args.length == 4 && !"--reemplazar".equals(args[3]))
            throw new IllegalArgumentException("La única opción adicional admitida es --reemplazar");
        System.out.println("Directorio de trabajo: " + Path.of("").toAbsolutePath());
        System.out.println("Entrada: " + origen.toAbsolutePath().normalize());
        // TODO guiado: crear copias, recorrer, registrar filas y escribir el informe.
        System.out.println("Carpeta de resultados: " + trabajo.toAbsolutePath().normalize());
        System.out.println("Filtro pedido: " + extension);
    }
}
