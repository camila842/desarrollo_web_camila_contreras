package com.tarea4.tarea4.models;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Integer> {

    List<Actividad> findByMiembroId(Integer miembroId);

    List<Actividad> findByMiembroId(Integer miembroId, Sort sort);

    List<Actividad> findByMiembroIdAndTipo(Integer miembroId, String tipo);

    List<Actividad> findByMiembroIdAndTipo(Integer miembroId, String tipo, Sort sort);

    @Query("""
        SELECT a.tipo, COUNT(a.id)
        FROM Actividad a
        GROUP BY a.tipo
        ORDER BY a.tipo
    """)
    List<Object[]> countActividadesPorTipo();

    @Query("""
        SELECT c.nombre, COUNT(a.id)
        FROM Actividad a
        JOIN a.miembro m
        JOIN m.comuna c
        GROUP BY c.nombre
        ORDER BY c.nombre
    """)
    List<Object[]> countActividadesPorComuna();
}