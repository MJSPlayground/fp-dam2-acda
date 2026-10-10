package es.mjsplayground.acda.ficheros.cli;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import static java.nio.file.StandardWatchEventKinds.*;

public final class AmpliacionesDemo {
    private AmpliacionesDemo() {}
    public static long filtrarCsv(Path origen, Path destino) throws IOException {
        long aceptados = 0;
        CSVFormat entrada = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get();
        CSVFormat salida = CSVFormat.RFC4180.builder().setHeader("id", "nombre").get();
        try (Reader in = Files.newBufferedReader(origen, StandardCharsets.UTF_8);
             CSVParser parser = entrada.parse(in);
             Writer out = Files.newBufferedWriter(destino, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(out, salida)) {
            for (CSVRecord fila : parser) {
                if (!fila.isConsistent()) continue;
                try {
                    if (Integer.parseInt(fila.get("id")) % 2 == 0) {
                        printer.printRecord(fila.get("id"), fila.get("nombre")); aceptados++;
                    }
                } catch (IllegalArgumentException ex) { /* Registro inválido: se omite. */ }
            }
        }
        return aceptados;
    }
    public static long copiarCanal(Path origen, Path destino) throws IOException {
        long bytes = 0;
        try (FileChannel in = FileChannel.open(origen, StandardOpenOption.READ);
             FileChannel out = FileChannel.open(destino, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);
            while (in.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) bytes += out.write(buffer);
                buffer.clear();
            }
        }
        return bytes;
    }
    public static boolean escribirFiable(Path destino, String contenido) throws IOException {
        Path absoluto = destino.toAbsolutePath();
        Files.createDirectories(absoluto.getParent());
        Path temporal = Files.createTempFile(absoluto.getParent(), ".nuevo-", ".tmp");
        boolean atomico = true;
        try {
            Files.writeString(temporal, contenido, StandardCharsets.UTF_8);
            try { Files.move(temporal, absoluto, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ex) {
                atomico = false;
                Files.move(temporal, absoluto, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporal); }
        return atomico;
    }
    public static void observar(Path carpeta) throws IOException, InterruptedException {
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            carpeta.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watcher.take();
                for (WatchEvent<?> evento : key.pollEvents()) {
                    if (evento.kind() == OVERFLOW) System.out.println("Eventos perdidos: vuelve a explorar " + carpeta);
                    else System.out.println(evento.kind() + ": " + carpeta.resolve((Path) evento.context()));
                }
                if (!key.reset()) break;
            }
        }
    }
    public static void main(String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("Uso: watch carpeta | stream/copy/atomic origen destino");
        switch (args[0]) {
            case "watch" -> observar(Path.of(args[1]));
            case "stream" -> System.out.println("Aceptados: " + filtrarCsv(Path.of(args[1]), Path.of(args[2])));
            case "copy" -> System.out.println("Bytes: " + copiarCanal(Path.of(args[1]), Path.of(args[2])));
            case "atomic" -> System.out.println("Movimiento atómico: " + escribirFiable(Path.of(args[1]), args[2]));
            default -> throw new IllegalArgumentException("Comando desconocido: " + args[0]);
        }
    }
}
