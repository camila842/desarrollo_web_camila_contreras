package com.tarea4.tarea4.models;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comentario")
public class Comentario {

    private static final List<String> PALABRAS_PROHIBIDAS = List.of(
        "holi",
        "que tal",
        "weeena"
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String nombre;

    @NotNull
    private String texto;

    @NotNull
    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    public Comentario() {
    }

    public Comentario(String nombre, String texto, LocalDateTime fecha, Actividad actividad) {
        this.nombre = nombre;
        this.texto = texto;
        this.fecha = fecha;
        this.actividad = actividad;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTexto() {
        return texto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public static List<String> cargarPalabrasProhibidas(String palabrasProhibidasPath) {
        List<String> palabras = new ArrayList<>();

        try {
            List<String> lineas = Files.readAllLines(Path.of(palabrasProhibidasPath));

            for (String linea : lineas) {
                String palabra = linea.trim();

                if (!palabra.isEmpty()) {
                    palabras.add(palabra.toLowerCase());
                }
            }
        } catch (IOException e) {
            return palabras;
        }

        return palabras;
    }

    public static Boolean contienePalabraProhibida(String texto, String palabrasProhibidasPath) {
        if (texto == null) {
            return false;
        }

        String textoNormalizado = texto.toLowerCase();
        List<String> palabrasProhibidas = cargarPalabrasProhibidas(palabrasProhibidasPath);

        for (String palabra : palabrasProhibidas) {
            if (textoNormalizado.contains(palabra.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public static Boolean validateComentario(
        String nombre,
        String texto,
        String palabrasProhibidasPath
    ) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }

        if (nombre.length() > 80) {
            return false;
        }

        if (texto.length() > 300) {
            return false;
        }

        if (contienePalabraProhibida(texto, palabrasProhibidasPath)) {
            return false;
        }

        return true;
    }
}