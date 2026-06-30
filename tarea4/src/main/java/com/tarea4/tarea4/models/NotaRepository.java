package com.tarea4.tarea4.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Integer> {

    @Query("SELECT AVG(n.nota) FROM Nota n WHERE n.actividad.id = :actividadId")
    Double findPromedioByActividadId(Integer actividadId);

    Long countByActividadId(Integer actividadId);

    // Verifica si el miembro ya evaluó esta actividad.
    // Spring Data deriva la query de actividad.id + miembro.id sin @Query.
    boolean existsByActividadIdAndMiembroId(Integer actividadId, Integer miembroId);
}
