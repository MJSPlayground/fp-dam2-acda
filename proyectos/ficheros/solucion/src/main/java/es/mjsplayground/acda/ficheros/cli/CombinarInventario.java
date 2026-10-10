package es.mjsplayground.acda.ficheros.cli;

import es.mjsplayground.acda.ficheros.model.service.InventarioComercialService;
import java.nio.file.Path;
import java.util.LinkedHashMap;

public final class CombinarInventario {
    public static void main(String[] args) throws Exception {
        if (args.length != 6) throw new IllegalArgumentException(
                "Uso: entrada1 formato1 entrada2 formato2 umbral salida.csv|json");
        var entradas = new LinkedHashMap<Path, String>();
        entradas.put(Path.of(args[0]), args[1]); entradas.put(Path.of(args[2]), args[3]);
        var servicio = new InventarioComercialService();
        var resumen = servicio.combinar(entradas, Integer.parseInt(args[4]));
        String nombre = args[5], formato = nombre.substring(nombre.lastIndexOf('.') + 1);
        servicio.exportar(Path.of(nombre), formato, resumen);
        System.out.println("Productos para reposición: " + resumen.productos().size() + ", valor: " + resumen.valorTotal());
        resumen.incidencias().forEach(System.err::println);
    }
}
