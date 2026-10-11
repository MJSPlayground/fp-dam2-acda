package es.mjsplayground.acda.ficheros.codelab;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Crea desde cero datos externos reproducibles; no contiene la solución del inventario. */
public final class PrepararDatos {
    public static void main(String[] args) throws Exception {
        if (args.length > 1) throw new IllegalArgumentException("Uso: [carpetaNueva]");
        Path raiz = Path.of(args.length == 0 ? "trabajo-codelab/entrada" : args[0]);
        if (raiz.toAbsolutePath().getParent() != null) Files.createDirectories(raiz.toAbsolutePath().getParent());
        if (Files.exists(raiz) && !Files.isDirectory(raiz))
            throw new IllegalArgumentException("La ruta de entrada ya existe y no es una carpeta: " + raiz.toAbsolutePath());
        if (Files.exists(raiz)) {
            System.out.println("Los datos ya están preparados en " + raiz.toAbsolutePath().normalize());
            System.out.println("Conservo tus cambios. Para generar otra muestra, indica una carpeta nueva en Program arguments de IntelliJ.");
            return;
        }
        Files.createDirectory(raiz); // Deliberadamente falla si ya existe: no destruye trabajo previo.
        Files.createDirectories(raiz.resolve("historico/2025"));
        Files.createDirectory(raiz.resolve("vacio"));
        Files.writeString(raiz.resolve("clientes.csv"), "A,B\n", StandardCharsets.UTF_8); // 4 bytes
        Files.writeString(raiz.resolve("notas.txt"), "lectura\n", StandardCharsets.UTF_8);
        Files.writeString(raiz.resolve("ventas.CSV"), "X,Y\n", StandardCharsets.UTF_8); // empate: 4 bytes
        Files.writeString(raiz.resolve("historico/enero.csv"), "1,2,3\n", StandardCharsets.UTF_8); // 6 bytes
        Files.writeString(raiz.resolve("historico/2025/marzo.CsV"), "a,b,c,d\n", StandardCharsets.UTF_8); // 8 bytes
        System.out.println("Datos creados en " + raiz.toAbsolutePath().normalize());
    }
}
