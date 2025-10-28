package com.bank.personasclientes.repository;

import com.bank.personasclientes.domain.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PersonaRepo extends JpaRepository<Persona, UUID> {
    Optional<Persona> findByIdentificacion(String identificacion);
}