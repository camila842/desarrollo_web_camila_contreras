package com.tarea4.tarea4.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tarea4.tarea4.services.ApiService;

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
}