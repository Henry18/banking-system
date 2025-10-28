package com.bank.cuentasmovimientos.dto;


import com.bank.cuentasmovimientos.domain.TipoMovimiento;

import java.math.BigDecimal;
import java.util.UUID;

public record MovimientoResponse(
        UUID movimientoId, UUID cuentaId, TipoMovimiento tipo, BigDecimal valor, BigDecimal saldoPosterior
) {}