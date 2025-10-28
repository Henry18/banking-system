package com.bank.cuentasmovimientos.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDetalleDTO {
    private LocalDateTime fecha;
    private String tipo;
    private BigDecimal valor;
    private BigDecimal saldo;
    private String referencia;
    private String numeroCuenta;
}
