package com.bank.personasclientes.controller;

import com.bank.personasclientes.dto.*;
import com.bank.personasclientes.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/clientes")
@AllArgsConstructor
public class ClienteController {
    @Autowired
    ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Crear cliente", description = "Registra un nuevo cliente en el sistema")
    public ResponseEntity<ClienteResponse> crearCliente(@RequestBody ClienteCreateRequest request) {
        return ResponseEntity.ok(clienteService.crear(request));
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Obtiene la lista completa de clientes")
    public ResponseEntity<List<ClienteResponse>> listarClientes() {
        return ResponseEntity.ok(clienteService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar cliente por ID", description = "Devuelve la información de un cliente específico")
    public ResponseEntity<ClienteResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Modifica los datos de un cliente existente")
    public ResponseEntity<ClienteResponse> actualizarCliente(
            @PathVariable UUID id,
            @RequestBody ClienteUpdateRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente por su identificador único")
    public ResponseEntity<Void> eliminarCliente(@PathVariable UUID id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}