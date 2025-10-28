package com.bank.personasclientes.repository;
import com.bank.personasclientes.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepo extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByClientId(String clientId);
}