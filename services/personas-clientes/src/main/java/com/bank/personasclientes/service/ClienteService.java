package com.bank.personasclientes.service;

import com.bank.personasclientes.dto.*;
import com.bank.personasclientes.domain.*;
import com.bank.personasclientes.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ClienteService {
    @Autowired
    private PersonaRepo personaRepo;
    @Autowired
    private ClienteRepo clienteRepo;

    @Transactional
    public ClienteResponse crear(ClienteCreateRequest r) {
        personaRepo.findByIdentificacion(r.identificacion()).ifPresent(p -> {
            throw new IllegalArgumentException("Identificación ya existe");
        });
        clienteRepo.findByClientId(r.clientId()).ifPresent(c -> {
            throw new IllegalArgumentException("clientId ya existe");
        });

        var persona = Persona.builder()
                .id(UUID.randomUUID()).nombre(r.nombre()).genero(r.genero())
                .edad(r.edad()).identificacion(r.identificacion())
                .direccion(r.direccion()).telefono(r.telefono()).build();
        personaRepo.save(persona);

        var cliente = Cliente.builder()
                .id(UUID.randomUUID())
                .clientId(r.clientId())
                .passwordHash(BCrypt.hashpw(r.password(), BCrypt.gensalt()))
                .estado(r.estado())
                .persona(persona).build();
        clienteRepo.save(cliente);

        return new ClienteResponse(cliente.getId(), cliente.getClientId(), cliente.getEstado(),
                persona.getNombre(), persona.getIdentificacion(), persona.getTelefono());
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepo.findAll().stream()
                .map(c -> new ClienteResponse(
                        c.getId(),
                        c.getClientId(),
                        c.getEstado(),
                        c.getPersona().getNombre(),
                        c.getPersona().getIdentificacion(),
                        c.getPersona().getTelefono()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(UUID id) {
        var cliente = clienteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        var persona = cliente.getPersona();
        return new ClienteResponse(cliente.getId(), cliente.getClientId(), cliente.getEstado(),
                persona.getNombre(), persona.getIdentificacion(), persona.getTelefono());
    }

    @Transactional
    public ClienteResponse actualizar(UUID id, ClienteUpdateRequest r) {
        var cliente = clienteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        var persona = cliente.getPersona();

        persona.setNombre(r.nombre());
        persona.setGenero(r.genero());
        persona.setEdad(r.edad());
        persona.setDireccion(r.direccion());
        persona.setTelefono(r.telefono());
        personaRepo.save(persona);

        cliente.setEstado(r.estado());
        if (r.password() != null && !r.password().isBlank()) {
            cliente.setPasswordHash(BCrypt.hashpw(r.password(), BCrypt.gensalt()));
        }
        clienteRepo.save(cliente);

        return new ClienteResponse(cliente.getId(), cliente.getClientId(), cliente.getEstado(),
                persona.getNombre(), persona.getIdentificacion(), persona.getTelefono());
    }

    @Transactional
    public void eliminar(UUID id) {
        var cliente = clienteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        var persona = cliente.getPersona();

        clienteRepo.delete(cliente);
        personaRepo.delete(persona);
    }
}
