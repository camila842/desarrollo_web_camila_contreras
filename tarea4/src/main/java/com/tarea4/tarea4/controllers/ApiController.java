package com.tarea4.tarea4.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tarea4.tarea4.services.ApiService;

import jakarta.servlet.http.HttpSession;

@RestController
public class ApiController {

    private final ApiService apiService;

    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/api/miembros-por-dia")
    public List<Map<String, Object>> getMiembrosRegistradosPorDia() {
        return apiService.getMiembrosRegistradosPorDia();
    }

    @GetMapping("/api/actividades-por-tipo")
    public List<Map<String, Object>> getTotalActividadesPorTipo() {
        return apiService.getTotalActividadesPorTipo();
    }

    @GetMapping("/api/actividades-por-comuna")
    public List<Map<String, Object>> getTotalActividadesPorComuna() {
        return apiService.getTotalActividadesPorComuna();
    }

    @GetMapping("/api/activities/{actividad_id}/comentarios")
    public ResponseEntity<Map<String, Object>> getComentarios(
        @PathVariable("actividad_id") Integer actividadId
    ) {
        Map<String, Object> response = apiService.getComentariosByActividadId(actividadId);

        if (Boolean.FALSE.equals(response.get("ok"))) {
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/activities/{actividad_id}/comentarios")
    public ResponseEntity<Map<String, Object>> agregarComentario(
        @PathVariable("actividad_id") Integer actividadId,
        @RequestBody Map<String, String> body
    ) {
        Map<String, Object> response = apiService.crearComentarioApi(
            actividadId,
            body.getOrDefault("nombre", "").trim(),
            body.getOrDefault("texto", "").trim()
        );

        if (Boolean.FALSE.equals(response.get("ok"))) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/palabras-prohibidas")
    public Map<String, Object> getPalabrasProhibidas() {
        return apiService.getPalabrasProhibidas();
    }

    @GetMapping("/api/actividades/buscar")
    public ResponseEntity<Map<String, Object>> buscarActividades(
        @RequestParam("q") String q,
        HttpSession session
    ) {
        Integer miembroId = (Integer) session.getAttribute("miembro_id");
        Map<String, Object> response = apiService.buscarActividades(q, miembroId);

        if (Boolean.FALSE.equals(response.get("ok"))) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/activities/{actividad_id}/notas")
    public ResponseEntity<Map<String, Object>> agregarNota(
        @PathVariable("actividad_id") Integer actividadId,
        @RequestBody Map<String, Object> body,
        HttpSession session
    ) {
        Integer miembroId = (Integer) session.getAttribute("miembro_id");

        if (miembroId == null) {
            return ResponseEntity.status(401).body(Map.of(
                "ok", false,
                "errores", List.of("Debes iniciar sesión para evaluar.")
            ));
        }

        Integer nota = null;
        Object notaRaw = body.get("nota");

        if (notaRaw instanceof Integer i) {
            nota = i;
        } else if (notaRaw instanceof Long l) {
            nota = l.intValue();
        } else if (notaRaw instanceof Double d) {
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                nota = d.intValue();
            }
        }

        Map<String, Object> response = apiService.agregarNota(actividadId, nota, miembroId);

        if (Boolean.FALSE.equals(response.get("ok"))) {
            if (Boolean.TRUE.equals(response.get("yaEvaluo"))) {
                return ResponseEntity.status(409).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}