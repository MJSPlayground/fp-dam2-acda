package es.mjsplayground.acda.ficheros.codelab;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Completa las fases indicadas en la práctica guiada. */
public final class InventarioCodelab {
    /** Una fila del inventario; este modelo de datos ya está preparado. */
    public record ArchivoInventariado(Path relativa, long bytes, Instant modificado) {}

    public static void main(String[] args) throws IOException {
        if (args.length != 0 && args.length != 3 && args.length != 4)
            throw new IllegalArgumentException("Uso: [carpetaEntrada carpetaTrabajo extension [--reemplazar]]");
        if (args.length == 4 && !"--reemplazar".equals(args[3]))
            throw new IllegalArgumentException("La única opción adicional admitida es --reemplazar");
        Path origen = Path.of(args.length == 0 ? "trabajo-codelab/entrada" : args[0]);
        Path trabajo = Path.of(args.length == 0 ? "trabajo-codelab/salida" : args[1]);
        String extension = args.length == 0 ? "csv" : args[2];
        boolean reemplazar = args.length == 4;
        System.out.println("Directorio de trabajo: " + Path.of("").toAbsolutePath());
        System.out.println("Entrada: " + origen.toAbsolutePath().normalize());
        System.out.println("Carpeta de resultados: " + trabajo.toAbsolutePath().normalize());
        System.out.println("Filtro pedido: " + extension);

        // TODO 2: comprobar que origen es un directorio y validar extension.
        // TODO 3: impedir salida dentro de entrada; crear copias y copiar clientes.csv.
        // Usa reemplazar solo cuando se haya pedido --reemplazar.
        // TODO 4: listar y mostrar únicamente las entradas del primer nivel.

        List<ArchivoInventariado> filas = new ArrayList<>();
        List<String> incidencias = new ArrayList<>();
        String sufijo = "." + extension.toLowerCase(Locale.ROOT);
        var visitante = new SimpleFileVisitor<Path>() {
            @Override public FileVisitResult visitFile(Path p, BasicFileAttributes a) {
                // TODO 6: seleccionar archivos y guardar ruta, bytes y fecha en filas.
                return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult visitFileFailed(Path p, IOException e) {
                // TODO 5: registrar el fallo en incidencias y continuar.
                return FileVisitResult.CONTINUE;
            }
        };
        // TODO 5: recorrer origen con visitante hasta cuatro niveles.
        // TODO 7: ordenar filas y calcular totalBytes y mayor (con desempate estable).
        long totalBytes = 0;
        ArchivoInventariado mayor = null;

        Path informe = trabajo.resolve("inventario.csv");
        // TODO 8: cuando la salida exista, llama a ExportadorCsv.guardar(informe, filas).
        // TODO 9: mostrar incidencias con su ruta; conservar las filas válidas.
        System.out.printf("Archivos: %d, bytes: %d%n", filas.size(), totalBytes);
        System.out.println("Mayor: " + (mayor == null ? "ninguno" : mayor.relativa()));
        System.out.println("Informe previsto: " + informe.toAbsolutePath().normalize());
        if (reemplazar) System.out.println("Opción --reemplazar solicitada.");
    }
}
