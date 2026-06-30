package com.tarea4.tarea4.models;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
//import jakarta.persistence.Lob;
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

    // @Lob
    @Column(length = 500)
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
            String descripcion) {
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

    private static final Set<String> VALID_TIPOS = Set.of(
            "artistica",
            "deportiva",
            "tecnologica",
            "social",
            "recreativa",
            "otra");

    private static final Set<String> VALID_FILE_TYPES = Set.of(
            "png",
            "jpg",
            "jpeg",
            "mp4");

    public static String validateActivity(
            String nombre,
            List<String> dias,
            String horaInicio,
            String horaFin,
            String tipo,
            MultipartFile archivo,
            Integer miembroId) {
        if (miembroId == null) {
            return "Debes iniciar sesión para registrar una actividad.";
        }

        if (nombre == null || nombre.trim().length() < 3) {
            return "El nombre debe tener al menos 3 caracteres.";
        }

        if (dias == null || dias.isEmpty()) {
            return "Debes seleccionar al menos un día.";
        }

        if (horaInicio == null || horaFin == null || horaInicio.isEmpty() || horaFin.isEmpty()) {
            return "Debe haber hora de inicio y término.";
        }

        String timeError = validateTimeActivity(horaInicio, horaFin);
        if (!timeError.isEmpty()) {
            return timeError;
        }

        if (tipo == null || !VALID_TIPOS.contains(tipo)) {
            return "Debes elegir el tipo de actividad.";
        }

        if (archivo != null && !archivo.isEmpty()) {
            String fileError = validateFile(archivo);
            if (!fileError.isEmpty()) {
                return fileError;
            }
        }

        return "";
    }

    public static String validateTimeActivity(String horaInicio, String horaFin) {
        try {
            LocalTime init = LocalTime.parse(horaInicio);
            LocalTime end = LocalTime.parse(horaFin);

            if (!end.isAfter(init)) {
                return "La hora de término debe ser mayor a la de inicio.";
            }

            return "";
        } catch (DateTimeParseException e) {
            return "Formato de hora inválido.";
        }
    }

    private static String validateFile(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return "Debes ingresar al menos un archivo.";
        }

        String filename = archivo.getOriginalFilename();

        if (filename == null || filename.isEmpty() || !filename.contains(".")) {
            return "Debes ingresar al menos un archivo.";
        }

        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        if (!VALID_FILE_TYPES.contains(ext)) {
            return "Solo se soportan archivos .png, .jpg, .jpeg, .mp4.";
        }

        return "";
    }
}