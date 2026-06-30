package com.tarea4.tarea4.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "nota",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_nota_actividad_miembro",
        columnNames = {"actividad_id", "miembro_id"}
    )
)
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "nota")
    private Integer nota;

    @ManyToOne
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    @ManyToOne
    @JoinColumn(name = "miembro_id", nullable = false)
    private Miembro miembro;

    public Nota() {
    }

    public Nota(Integer nota, Actividad actividad, Miembro miembro) {
        this.nota = nota;
        this.actividad = actividad;
        this.miembro = miembro;
    }

    public Integer getId() {
        return id;
    }

    public Integer getNota() {
        return nota;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public Miembro getMiembro() {
        return miembro;
    }
}
