package com.bank.cuentasmovimientos.repository;

import com.bank.cuentasmovimientos.domain.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CuentaRepo extends JpaRepository<Cuenta, UUID> {
    Optional<Cuenta> findByNumero(String numero);
}