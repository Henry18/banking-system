package com.bank.cuentasmovimientos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CuentaCreateRequest(
        @NotBlank String numero, @NotBlank String tipo,
        @DecimalMin("0") BigDecimal saldoInicial, @NotNull UUID clienteId
) {}
