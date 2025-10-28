package com.bank.cuentasmovimientos.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteResumenDTO {
    private String clienteId;
    private BigDecimal totalCreditos;
    private BigDecimal totalDebitos;
    private BigDecimal saldoFinal;
}

