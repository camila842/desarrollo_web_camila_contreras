package com.tarea4.tarea4.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Integer> {
    List<Actividad> findByMiembroId(Integer miembroId);
    List<Actividad> findByMiembroIdAndTipo(Integer miembroId, String tipo);
}