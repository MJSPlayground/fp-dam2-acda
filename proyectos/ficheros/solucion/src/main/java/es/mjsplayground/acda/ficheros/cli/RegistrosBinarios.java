package es.mjsplayground.acda.ficheros.cli;

import es.mjsplayground.acda.ficheros.model.entity.Existencia;
import es.mjsplayground.acda.ficheros.model.service.ExistenciaService;
import java.nio.file.Path;
import java.util.List;

public final class RegistrosBinarios {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("Uso: crear|leer|actualizar archivo.bin [indice unidades]");
        ExistenciaService service = new ExistenciaService(); Path archivo = Path.of(args[1]);
        switch (args[0]) {
            case "crear" -> service.guardar(archivo, List.of(new Existencia(101, 0),
                    new Existencia(102, 9), new Existencia(103, 10)));
            case "leer" -> service.leer(archivo).forEach(System.out::println);
            case "actualizar" -> {
                if (args.length != 4) throw new IllegalArgumentException("Faltan índice y unidades");
                service.actualizar(archivo, Integer.parseInt(args[2]), Long.parseLong(args[3]));
            }
            default -> throw new IllegalArgumentException("Operación desconocida");
        }
    }
}
