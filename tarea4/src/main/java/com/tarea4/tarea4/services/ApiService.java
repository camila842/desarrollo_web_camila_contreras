package com.tarea4.tarea4.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tarea4.tarea4.models.ActividadRepository;
import com.tarea4.tarea4.models.MiembroRepository;


import java.time.format.DateTimeFormatter;

import com.tarea4.tarea4.models.Actividad;
import com.tarea4.tarea4.models.Comentario;
import com.tarea4.tarea4.models.ComentarioRepository;
@Service
public class ApiService {

    private final MiembroRepository miembroRepository;
    private final ActividadRepository actividadRepository;
    private final ComentarioRepository comentarioRepository;

    private static final String PALABRAS_PROHIBIDAS_PATH =
        "src/main/resources/data/palabras_prohibidas.txt";

    public ApiService(
        MiembroRepository miembroRepository,
        ActividadRepository actividadRepository,
        ComentarioRepository comentarioRepository
    ) {
        this.miembroRepository = miembroRepository;
        this.actividadRepository = actividadRepository;
        this.comentarioRepository = comentarioRepository;
    }

    public List<Map<String, Object>> getMiembrosRegistradosPorDia() {
        List<Object[]> resultados = miembroRepository.countMiembrosRegistradosPorDia();
        List<Map<String, Object>> data = new ArrayList<>();

        for (Object[] row : resultados) {
            Map<String, Object> item = new HashMap<>();
            item.put("dia", row[0].toString());
            item.put("cantidad", row[1]);
            data.add(item);
        }

        return data;
    }

    public List<Map<String, Object>> getTotalActividadesPorTipo() {
        List<Object[]> resultados = actividadRepository.countActividadesPorTipo();
        List<Map<String, Object>> data = new ArrayList<>();

        for (Object[] row : resultados) {
            Map<String, Object> item = new HashMap<>();
            item.put("tipo", row[0].toString());
            item.put("cantidad", row[1]);
            data.add(item);
        }

        return data;
    }

    public List<Map<String, Object>> getTotalActividadesPorComuna() {
        List<Object[]> resultados = actividadRepository.countActividadesPorComuna();
        List<Map<String, Object>> data = new ArrayList<>();

        for (Object[] row : resultados) {
            Map<String, Object> item = new HashMap<>();
            item.put("comuna", row[0].toString());
            item.put("cantidad", row[1]);
            data.add(item);
        }

        return data;
    }
    public Map<String, Object> getComentariosByActividadId(Integer actividadId) {
        Actividad actividad = actividadRepository.findById(actividadId).orElse(null);

        if (actividad == null) {
            return Map.of(
                "ok", false,
                "errores", List.of("Actividad no encontrada")
            );
        }

        List<Comentario> comentarios = comentarioRepository.findByActividadIdOrderByFechaDesc(actividadId);
        List<Map<String, String>> data = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Comentario comentario : comentarios) {
            Map<String, String> item = new HashMap<>();
            item.put("fecha", comentario.getFecha().format(formatter));
            item.put("nombre", comentario.getNombre());
            item.put("texto", comentario.getTexto());
            data.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("comentarios", data);

        return response;
    }

    public Map<String, Object> crearComentarioApi(
        Integer actividadId,
        String nombre,
        String texto
    ) {
        Actividad actividad = actividadRepository.findById(actividadId).orElse(null);

        if (actividad == null) {
            return Map.of(
                "ok", false,
                "errores", List.of("Actividad no encontrada")
            );
        }

        List<String> errores = new ArrayList<>();

        if (nombre.length() < 3 || nombre.length() > 80) {
            errores.add("El nombre debe tener entre 3 y 80 caracteres.");
        }

        if (texto.length() < 5 || texto.length() > 300) {
            errores.add("El comentario debe tener entre 5 y 300 caracteres.");
        }

        if (Comentario.contienePalabraProhibida(texto, PALABRAS_PROHIBIDAS_PATH)) {
            errores.add("El comentario contiene palabras no permitidas.");
        }

        if (!errores.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("ok", false);
            response.put("errores", errores);
            return response;
        }

        Comentario comentario = new Comentario(
            nombre,
            texto,
            java.time.LocalDateTime.now(),
            actividad
        );

        comentarioRepository.save(comentario);

        return Map.of(
            "ok", true,
            "mensaje", "Comentario agregado correctamente."
        );
    }

    public Map<String, Object> getPalabrasProhibidas() {
        List<String> palabras = Comentario.cargarPalabrasProhibidas(PALABRAS_PROHIBIDAS_PATH);

        return Map.of(
            "ok", true,
            "palabras", palabras
        );
    }
}