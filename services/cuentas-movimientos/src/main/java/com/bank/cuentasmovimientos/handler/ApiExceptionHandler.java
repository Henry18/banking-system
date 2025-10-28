package com.bank.cuentasmovimientos.handler;

import com.bank.cuentasmovimientos.handler.SaldoNoDisponibleException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(SaldoNoDisponibleException.class)
    public ResponseEntity<?> handleSaldo(SaldoNoDisponibleException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("timestamp", OffsetDateTime.now().toString(), "code","INSUFFICIENT_FUNDS", "message", ex.getMessage()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBad(IllegalArgumentException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
