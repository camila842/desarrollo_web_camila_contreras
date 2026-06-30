package com.tarea4.tarea4.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import com.tarea4.tarea4.models.Actividad;
import com.tarea4.tarea4.models.ActividadRepository;
import com.tarea4.tarea4.models.Comentario;
import com.tarea4.tarea4.models.ComentarioRepository;
import com.tarea4.tarea4.models.Comuna;
import com.tarea4.tarea4.models.ComunaRepository;
import com.tarea4.tarea4.models.Foto;
import com.tarea4.tarea4.models.FotoRepository;
import com.tarea4.tarea4.models.Miembro;
import com.tarea4.tarea4.models.MiembroRepository;
//import com.tarea4.tarea4.models.Nota;
import com.tarea4.tarea4.models.NotaRepository;
import com.tarea4.tarea4.models.Region;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class AppService {

    private final MiembroRepository miembroRepository;
    private final ComunaRepository comunaRepository;
    private final ActividadRepository actividadRepository;
    private final FotoRepository fotoRepository;
    private final ComentarioRepository comentarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotaRepository notaRepository;
    private final String pathStatic;

    public AppService(
            MiembroRepository miembroRepository,
            ComunaRepository comunaRepository,
            ActividadRepository actividadRepository,
            FotoRepository fotoRepository,
            ComentarioRepository comentarioRepository,
            PasswordEncoder passwordEncoder,
            NotaRepository notaRepository) throws IOException {
        this.miembroRepository = miembroRepository;
        this.comunaRepository = comunaRepository;
        this.actividadRepository = actividadRepository;
        this.fotoRepository = fotoRepository;
        this.comentarioRepository = comentarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.notaRepository = notaRepository;

        Path staticDir = Paths.get(
                ResourceUtils.getFile("classpath:static").getAbsolutePath());

        this.pathStatic = staticDir.toString();
        System.out.println("Static path resolved to: " + this.pathStatic);
    }

    public Miembro registerUser(
            String username,
            String lastname,
            String email,
            String rol,
            String password,
            Integer comunaId) {
        Comuna comuna = null;

        if (comunaId != null) {
            comuna = comunaRepository.findById(comunaId).orElse(null);
        }

        Boolean emailExists = miembroRepository.findByEmail(email).isPresent();

        String validationError = Miembro.validateRegisterUser(
                username,
                lastname,
                email,
                rol,
                password,
                comunaId,
                emailExists);

        if (!validationError.isEmpty()) {
            throw new IllegalArgumentException(validationError);
        }

        Miembro miembro = new Miembro(
                username,
                lastname,
                rol,
                email,
                passwordEncoder.encode(password),
                LocalDateTime.now(),
                comuna);

        return miembroRepository.save(miembro);
    }

    public Foto registerFoto(
            Integer actividadId,
            String rutaArchivo,
            String nombreArchivo) {
        Actividad actividad = actividadRepository.findById(actividadId).orElse(null);

        if (actividad == null) {
            throw new IllegalArgumentException("La actividad no existe.");
        }

        Foto foto = new Foto(
                rutaArchivo,
                nombreArchivo,
                actividad);

        return fotoRepository.save(foto);
    }

    public Foto registerFotoFromUpload(
            Integer actividadId,
            MultipartFile archivo) throws Exception {
        Actividad actividad = actividadRepository.findById(actividadId).orElse(null);

        if (actividad == null) {
            throw new IllegalArgumentException("La actividad no existe.");
        }

        String originalFilename = archivo.getOriginalFilename();

        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Archivo inválido.");
        }

        String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

        Path uploadsDir = Paths.get(pathStatic + "/uploads");

        if (!Files.exists(uploadsDir)) {
            Files.createDirectories(uploadsDir);
        }

        Path finalPath = Paths.get(pathStatic + "/uploads/" + safeFilename);

        try (InputStream inputStream = archivo.getInputStream()) {
            Files.copy(inputStream, finalPath, StandardCopyOption.REPLACE_EXISTING);
        }

        String rutaDb = "uploads/" + safeFilename;

        Foto foto = new Foto(
                finalPath.toString(),
                rutaDb,
                actividad);

        return fotoRepository.save(foto);
    }

    public Integer registerActivity(
            String nombre,
            List<String> dias,
            String horaInicio,
            String horaFin,
            String tipo,
            Integer miembroId,
            MultipartFile archivo,
            String descripcion) {
        String validationError = Actividad.validateActivity(
                nombre,
                dias,
                horaInicio,
                horaFin,
                tipo,
                archivo,
                miembroId,
                descripcion);

        if (!validationError.isEmpty()) {
            throw new IllegalArgumentException(validationError);
        }
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);

        if (miembro == null) {
            throw new IllegalArgumentException("El miembro no existe.");
        }

        String duracion = calcularDuracion(horaInicio, horaFin);
        Integer lastId = null;

        for (String dia : dias) {
            Actividad actividad = new Actividad(
                    miembro,
                    dia,
                    horaInicio,
                    duracion,
                    tipo,
                    nombre,
                    (descripcion != null && !descripcion.trim().isEmpty()) ? descripcion.trim() : null);

            Actividad savedActividad = actividadRepository.save(actividad);
            lastId = savedActividad.getId();
        }

        return lastId;
    }

    public List<Map<String, Object>> getComunas() {
        List<Comuna> comunas = comunaRepository.findAllByOrderByRegionIdAscNombreAsc();

        Map<Integer, Map<String, Object>> regionesMap = new HashMap<>();

        for (Comuna comuna : comunas) {
            Region region = comuna.getRegion();
            Integer regionId = region.getId();

            if (!regionesMap.containsKey(regionId)) {
                Map<String, Object> regionData = new HashMap<>();
                regionData.put("id", regionId);
                regionData.put("nombre", region.getNombre());
                regionData.put("comunas", new ArrayList<Map<String, Object>>());

                regionesMap.put(regionId, regionData);
            }

            Map<String, Object> comunaData = new HashMap<>();
            comunaData.put("id", comuna.getId());
            comunaData.put("nombre", comuna.getNombre());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> comunasRegion = (List<Map<String, Object>>) regionesMap.get(regionId)
                    .get("comunas");

            comunasRegion.add(comunaData);
        }

        return new ArrayList<>(regionesMap.values());
    }

    public List<Map<String, String>> getLastMembers(Integer limit) {
        List<Miembro> miembros = miembroRepository.findAllByOrderByFechaRegistroDesc();

        List<Map<String, String>> data = new ArrayList<>();
        int count = 0;

        for (Miembro miembro : miembros) {
            if (count >= limit) {
                break;
            }

            Map<String, String> row = new HashMap<>();
            row.put("nombre", miembro.getNombre());
            row.put("email", miembro.getEmail());

            List<Actividad> actividades = miembro.getActividades();

            if (actividades != null && !actividades.isEmpty()) {
                Actividad actividad = actividades.get(0);
                row.put("actividad", actividad.getNombre());
                row.put("tipo", actividad.getTipo());
            } else {
                row.put("actividad", null);
                row.put("tipo", null);
            }

            data.add(row);
            count++;
        }

        return data;
    }

    public Miembro getUserByEmail(String email) {
        return miembroRepository.findByEmail(email).orElse(null);
    }

    public Miembro loginUser(String email, String password) {
        String validationError = Miembro.validateLoginUser(email, password);

        if (!validationError.isEmpty()) {
            throw new IllegalArgumentException(validationError);
        }
        Miembro user = getUserByEmail(email);

        if (user == null) {
            return null;
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }

        return user;
    }

    public Miembro getMiembroById(Integer memberId) {
        return miembroRepository.findById(memberId).orElse(null);
    }

    public Actividad getActividadById(Integer actividadId) {
        return actividadRepository.findById(actividadId).orElse(null);
    }

    public List<Actividad> getActividadesByMiembroId(
            Integer memberId,
            String tipo,
            String ordenAttr,
            String ordenDir) {
        Sort sort = construirOrdenActividad(ordenAttr, ordenDir);

        if (tipo != null && !tipo.isEmpty()) {
            return actividadRepository.findByMiembroIdAndTipo(memberId, tipo, sort);
        }

        return actividadRepository.findByMiembroId(memberId, sort);
    }

    private Sort construirOrdenActividad(String ordenAttr, String ordenDir) {
        String columna;

        if ("nombre".equals(ordenAttr)) {
            columna = "nombre";
        } else if ("tipo".equals(ordenAttr)) {
            columna = "tipo";
        } else if ("dia".equals(ordenAttr)) {
            columna = "dia";
        } else if ("hora_inicio".equals(ordenAttr)) {
            columna = "horaInicio";
        } else if ("duracion".equals(ordenAttr)) {
            columna = "duracion";
        } else {
            columna = "id";
        }

        if ("desc".equals(ordenDir)) {
            return Sort.by(columna).descending();
        }

        return Sort.by(columna).ascending();
    }

    public Comentario crearComentario(
            String nombre,
            String texto,
            Integer actividadId,
            String palabrasProhibidasPath) {
        if (!Comentario.validateComentario(nombre, texto, palabrasProhibidasPath)) {
            throw new IllegalArgumentException("El comentario no es válido.");
        }

        Actividad actividad = actividadRepository.findById(actividadId).orElse(null);

        if (actividad == null) {
            throw new IllegalArgumentException("La actividad no existe.");
        }

        Comentario comentario = new Comentario(
                nombre,
                texto,
                LocalDateTime.now(),
                actividad);

        return comentarioRepository.save(comentario);
    }

    public List<Comentario> getComentariosByActividadId(Integer actividadId) {
        return comentarioRepository.findByActividadIdOrderByFechaDesc(actividadId);
    }

    private String calcularDuracion(String horaInicio, String horaFin) {
        LocalTime init = LocalTime.parse(horaInicio);
        LocalTime end = LocalTime.parse(horaFin);

        long totalMinutes = Duration.between(init, end).toMinutes();

        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        return String.format("%02d:%02d", hours, minutes);
    }

    public List<Miembro> getMiembrosPaginados(
            Integer limit,
            Integer offset,
            String tipo,
            String ordenAttr,
            String ordenDir) {
        int page = offset / limit;

        Sort sort = construirOrdenMiembro(ordenAttr, ordenDir);
        Pageable pageable = PageRequest.of(page, limit, sort);

        Page<Miembro> miembrosPage;

        if (tipo != null && !tipo.isEmpty()) {
            miembrosPage = miembroRepository.findDistinctByTipoActividad(tipo, pageable);
        } else {
            miembrosPage = miembroRepository.findAll(pageable);
        }

        return miembrosPage.getContent();
    }

    public Long countMiembros(String tipo) {
        if (tipo != null && !tipo.isEmpty()) {
            return miembroRepository.countDistinctByTipoActividad(tipo);
        }

        return miembroRepository.count();
    }

    private Sort construirOrdenMiembro(String ordenAttr, String ordenDir) {
        String columna;

        if ("nombre".equals(ordenAttr)) {
            columna = "nombre";
        } else if ("apellido".equals(ordenAttr)) {
            columna = "apellido";
        } else if ("email".equals(ordenAttr)) {
            columna = "email";
        } else if ("fecha_registro".equals(ordenAttr)) {
            columna = "fechaRegistro";
        } else {
            columna = "id";
        }

        if ("desc".equals(ordenDir)) {
            return Sort.by(columna).descending();
        }

        return Sort.by(columna).ascending();
    }

    public Double getPromedioNota(Integer actividadId) {
        return notaRepository.findPromedioByActividadId(actividadId);
    }

    public Long getCantidadNotas(Integer actividadId) {
        return notaRepository.countByActividadId(actividadId);
    }

    public boolean yaEvaluoActividad(Integer actividadId, Integer miembroId) {
        return notaRepository.existsByActividadIdAndMiembroId(actividadId, miembroId);
    }
}