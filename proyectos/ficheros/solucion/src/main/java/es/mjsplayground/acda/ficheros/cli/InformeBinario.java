package es.mjsplayground.acda.ficheros.cli;

import es.mjsplayground.acda.ficheros.model.entity.Existencia;
import es.mjsplayground.acda.ficheros.model.service.ExistenciaService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public final class InformeBinario {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Uso: entrada.bin informe.csv");
        ExistenciaService service = new ExistenciaService();
        var registros = service.leer(Path.of(args[0]));
        long total = registros.stream().map(Existencia::unidades).reduce(0L, Math::addExact);
        Map<String, Long> categorias = registros.stream().collect(Collectors.groupingBy(service::categoria, Collectors.counting()));
        Existencia mayor = registros.stream().max(Comparator.comparingLong(Existencia::unidades)
                .thenComparingInt(e -> -e.id())).orElse(null);
        Path destino = Path.of(args[1]);
        try (var writer = Files.newBufferedWriter(destino, StandardCharsets.UTF_8);
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.RFC4180.builder()
                     .setHeader("id", "unidades", "clasificacion").get())) {
            for (Existencia e : registros) csv.printRecord(e.id(), e.unidades(), service.categoria(e));
        }
        System.out.printf("Registros: %d, unidades: %d, mayor: %s, categorías: %s%n",
                registros.size(), total, mayor, categorias);
    }
}
