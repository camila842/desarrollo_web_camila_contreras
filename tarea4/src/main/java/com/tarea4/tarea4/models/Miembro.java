package com.tarea4.tarea4.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "miembro")
public class Miembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String nombre;

    private String apellido;

    private String rol;

    @NotNull
    private String email;

    private String passwordHash;

    @NotNull
    private LocalDateTime fechaRegistro;

    @ManyToOne
    @JoinColumn(name = "comuna_id")
    private Comuna comuna;

    @OneToMany(mappedBy = "miembro")
    private List<Actividad> actividades;

    public Miembro() {
    }

    public Miembro(
        String nombre,
        String apellido,
        String rol,
        String email,
        String passwordHash,
        LocalDateTime fechaRegistro,
        Comuna comuna
    ) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.rol = rol;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fechaRegistro = fechaRegistro;
        this.comuna = comuna;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getRol() {
        return rol;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public Comuna getComuna() {
        return comuna;
    }

    public List<Actividad> getActividades() {
        return actividades;
    }
    private static final Set<String> VALID_ROLES = Set.of(
        "Estudiante Pre Grado",
        "Estudiante Post Grado",
        "Funcionario",
        "Académico"
    );

    private static final Set<Character> SPECIAL_CHARS = Set.of(
        '@', '¿', '?', '{', '}', '.'
    );

    public static String validateRegisterUser(
        String username,
        String lastname,
        String email,
        String rol,
        String password,
        Integer comunaId,
        Boolean emailExists
    ) {
        if (username == null || username.trim().length() < 3) {
            return "El nombre debe tener al menos 3 caracteres.";
        }

        if (lastname == null || lastname.trim().length() < 3) {
            return "El apellido debe tener al menos 3 caracteres.";
        }

        if (rol == null || !VALID_ROLES.contains(rol)) {
            return "Seleccione un rol válido.";
        }

        String emailError = validateEmail(email);
        if (!emailError.isEmpty()) {
            return emailError;
        }

        if (password == null || password.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }

        Boolean hasSpecialChar = false;
        for (char c : password.toCharArray()) {
            if (SPECIAL_CHARS.contains(c)) {
                hasSpecialChar = true;
                break;
            }
        }

        if (!hasSpecialChar) {
            return "La contraseña debe tener al menos un carácter especial: @, ¿, ?, {, }, .";
        }

        if (comunaId == null) {
            return "Seleccione una comuna.";
        }

        if (emailExists) {
            return "Ya existe un usuario registrado con ese correo.";
        }

        return "";
    }

    public static String validateLoginUser(String email, String password) {
        String emailError = validateEmail(email);
        if (!emailError.isEmpty()) {
            return emailError;
        }

        if (password == null || password.isEmpty()) {
            return "Ingrese su contraseña.";
        }

        return "";
    }

    private static String validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "Email inválido.";
        }

        String[] parts = email.split("@", -1);

        if (parts.length != 2) {
            return "Email inválido.";
        }

        String name = parts[0];
        String domain = parts[1];

        if (name.length() < 3) {
            return "Email inválido.";
        }

        if (!domain.contains(".")) {
            return "Email inválido.";
        }

        String[] domainParts = domain.split("\\.", -1);
        for (String part : domainParts) {
            if (part.isEmpty()) {
                return "Email inválido.";
            }
        }

        return "";
    }
}