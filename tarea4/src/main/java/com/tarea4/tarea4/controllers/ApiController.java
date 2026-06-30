package com.tarea4.tarea4.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
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
}