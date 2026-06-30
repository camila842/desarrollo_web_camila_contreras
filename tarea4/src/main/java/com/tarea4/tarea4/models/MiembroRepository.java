package com.tarea4.tarea4.models;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MiembroRepository extends JpaRepository<Miembro, Integer> {
    Optional<Miembro> findByEmail(String email);
}