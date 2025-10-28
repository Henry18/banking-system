package com.bank.cuentasmovimientos.controller;

import com.bank.cuentasmovimientos.domain.Status;
import com.bank.cuentasmovimientos.dto.CuentaCreateRequest;
import com.bank.cuentasmovimientos.domain.Cuenta;
import com.bank.cuentasmovimientos.domain.TipoCuenta;
import com.bank.cuentasmovimientos.repository.CuentaRepo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController @RequestMapping("/cuentas") @RequiredArgsConstructor
public class CuentaController {
    private final CuentaRepo repo;

    @Operation(summary = "Crea cuenta",
            description = "Crea la cuenta y retorna su id para otras operaciones")
    @PostMapping
    public ResponseEntity<Cuenta> crear(@Valid @RequestBody CuentaCreateRequest r){
        var c = Cuenta.builder()
                .id(UUID.randomUUID())
                .numero(r.numero())
                .tipo(TipoCuenta.valueOf(r.tipo().toUpperCase()))
                .saldoInicial(r.saldoInicial() == null ? BigDecimal.ZERO : r.saldoInicial())
                .saldo(r.saldoInicial() == null ? BigDecimal.ZERO : r.saldoInicial())
                .estado(Status.ACTIVA.toString())
                .clienteId(r.clienteId())
                .build();
        return ResponseEntity.ok(repo.save(c));
    }

    @Operation(summary = "Consultar movimientos por cuenta",
            description = "Devuelve todos los movimientos asociados a una cuenta específica.")
    @GetMapping("/{id}")
    public ResponseEntity<Cuenta> get(@Parameter(description = "Identificador único de la cuenta", required = true)
                                      @PathVariable UUID id){
        return ResponseEntity.of(repo.findById(id));
    }
}