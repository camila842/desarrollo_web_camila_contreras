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

    public static Boolean validateComentario(String nombre, String texto) {
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

        String textoLower = texto.toLowerCase();

        for (String palabra : PALABRAS_PROHIBIDAS) {
            if (textoLower.contains(palabra.toLowerCase())) {
                return false;
            }
        }

        return true;
    }
}