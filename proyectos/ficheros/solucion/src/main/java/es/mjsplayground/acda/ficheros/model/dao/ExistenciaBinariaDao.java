package es.mjsplayground.acda.ficheros.model.dao;

import es.mjsplayground.acda.ficheros.model.entity.Existencia;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ExistenciaBinariaDao {
    private static final int VERSION = 1;
    private static final int CABECERA = Integer.BYTES;
    private static final int REGISTRO = Integer.BYTES + Long.BYTES;
    public void escribir(Path archivo, List<Existencia> registros) throws IOException {
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(archivo))) {
            out.writeInt(VERSION);
            for (Existencia e : registros) { out.writeInt(e.id()); out.writeLong(e.unidades()); }
        }
    }
    public List<Existencia> leer(Path archivo) throws IOException {
        long longitud = Files.size(archivo);
        if (longitud < CABECERA || (longitud - CABECERA) % REGISTRO != 0)
            throw new IOException("Archivo binario truncado: " + archivo);
        List<Existencia> registros = new ArrayList<>();
        try (DataInputStream in = new DataInputStream(Files.newInputStream(archivo))) {
            if (in.readInt() != VERSION) throw new IOException("Versión binaria desconocida");
            for (long i = CABECERA; i < longitud; i += REGISTRO) registros.add(new Existencia(in.readInt(), in.readLong()));
        }
        return registros;
    }
    public void actualizar(Path archivo, int indice, long unidades) throws IOException {
        if (indice < 0 || unidades < 0) throw new IllegalArgumentException("Índice o unidades negativos");
        try (RandomAccessFile raf = new RandomAccessFile(archivo.toFile(), "rw")) {
            if (raf.length() < CABECERA || (raf.length() - CABECERA) % REGISTRO != 0)
                throw new IOException("Archivo binario truncado");
            raf.seek(0);
            if (raf.readInt() != VERSION) throw new IOException("Versión binaria desconocida");
            long posicion = CABECERA + REGISTRO * (long) indice;
            if (posicion + REGISTRO > raf.length()) throw new IllegalArgumentException("Registro fuera de rango");
            raf.seek(posicion + Integer.BYTES);
            raf.writeLong(unidades);
        }
    }
}
