package com.tarea4.tarea4.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tarea4.tarea4.models.ActividadRepository;
import com.tarea4.tarea4.models.MiembroRepository;

@Service
public class ApiService {

    private final MiembroRepository miembroRepository;
    private final ActividadRepository actividadRepository;

    public ApiService(
        MiembroRepository miembroRepository,
        ActividadRepository actividadRepository
    ) {
        this.miembroRepository = miembroRepository;
        this.actividadRepository = actividadRepository;
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
}