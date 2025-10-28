package com.bank.cuentasmovimientos.repository;

import com.bank.cuentasmovimientos.domain.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MovimientoRepo extends JpaRepository<Movimiento, UUID> {
    Optional<Movimiento> findByIdempotencyKey(String key);

    @Query("""
       SELECT m FROM Movimiento m
       WHERE m.cuenta.clienteId = :clienteId
         AND m.fecha >= COALESCE(:fechaInicio, m.fecha)
         AND m.fecha <= COALESCE(:fechaFin, m.fecha)
       ORDER BY m.fecha DESC
       """)
    List<Movimiento> findByClienteAndFechas(UUID clienteId, LocalDate fechaInicio, LocalDate fechaFin);
}