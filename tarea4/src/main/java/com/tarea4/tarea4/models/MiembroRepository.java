package com.tarea4.tarea4.models;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MiembroRepository extends JpaRepository<Miembro, Integer> {
    Optional<Miembro> findByEmail(String email);

    List<Miembro> findAllByOrderByFechaRegistroDesc();

    @Query("""
        SELECT DATE(m.fechaRegistro), COUNT(m.id)
        FROM Miembro m
        GROUP BY DATE(m.fechaRegistro)
        ORDER BY DATE(m.fechaRegistro)
    """)
    List<Object[]> countMiembrosRegistradosPorDia();

    Page<Miembro> findAll(Pageable pageable);

    @Query("""
        SELECT DISTINCT m
        FROM Miembro m
        JOIN m.actividades a
        WHERE a.tipo = :tipo
    """)
    Page<Miembro> findDistinctByTipoActividad(String tipo, Pageable pageable);

    @Query("""
        SELECT COUNT(DISTINCT m.id)
        FROM Miembro m
        JOIN m.actividades a
        WHERE a.tipo = :tipo
    """)
    Long countDistinctByTipoActividad(String tipo);
}