package com.tarea4.tarea4.models;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "actividad")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "miembro_id", nullable = false)
    private Miembro miembro;

    @NotNull
    private String dia;

    @NotNull
    private String horaInicio;

    @NotNull
    private String duracion;

    @NotNull
    private String tipo;

    @NotNull
    private String nombre;

    @Lob
    private String descripcion;

    @OneToMany(mappedBy = "actividad")
    private List<Foto> fotos;

    @OneToMany(mappedBy = "actividad")
    private List<Comentario> comentarios;

    public Actividad() {
    }

    public Actividad(
        Miembro miembro,
        String dia,
        String horaInicio,
        String duracion,
        String tipo,
        String nombre,
        String descripcion
    ) {
        this.miembro = miembro;
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.duracion = duracion;
        this.tipo = tipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Integer getId() {
        return id;
    }

    public Miembro getMiembro() {
        return miembro;
    }

    public String getDia() {
        return dia;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public String getDuracion() {
        return duracion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public List<Foto> getFotos() {
        return fotos;
    }

    public List<Comentario> getComentarios() {
        return comentarios;
    }
}