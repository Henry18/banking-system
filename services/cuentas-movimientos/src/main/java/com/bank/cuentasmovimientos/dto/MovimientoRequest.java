package com.bank.cuentasmovimientos.dto;

import com.bank.cuentasmovimientos.domain.TipoMovimiento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record MovimientoRequest(
        @NotNull UUID cuentaId,
        @NotNull TipoMovimiento tipo,
        @DecimalMin(value="0.01") BigDecimal valor,
        String referencia,
        @NotBlank String idempotencyKey
) {}
