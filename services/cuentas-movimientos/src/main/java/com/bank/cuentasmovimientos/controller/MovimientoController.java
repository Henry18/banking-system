package com.bank.cuentasmovimientos.controller;

import com.bank.cuentasmovimientos.dto.*;
import com.bank.cuentasmovimientos.service.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/movimientos") @RequiredArgsConstructor
public class MovimientoController {
    private final MovimientoService service;

    @PostMapping
    public ResponseEntity<MovimientoResponse> crear(@Valid @RequestBody MovimientoRequest r){
        return ResponseEntity.ok(service.aplicar(r));
    }
}