package com.bank.cuentasmovimientos.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ReporteEstadoCuentaResponse {

    private UUID clienteId;
    private String nombreCliente;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDateTime generadoEn;

    private BigDecimal saldoTotalInicial;
    private BigDecimal totalCreditos;
    private BigDecimal totalDebitos;
    private BigDecimal saldoTotalFinal;

    private List<CuentaDetalle> cuentas;

    @Data
    @Builder
    public static class CuentaDetalle {
        private UUID cuentaId;
        private String numero;
        private String tipo;
        private BigDecimal saldoInicial;
        private BigDecimal totalCreditos;
        private BigDecimal totalDebitos;
        private BigDecimal saldoFinal;
        private List<MovimientoDetalle> movimientos;
    }

    @Data
    @Builder
    public static class MovimientoDetalle {
        private String tipo;
        private BigDecimal valor;
        private BigDecimal saldoDisponible;
        private String referencia;
        private LocalDateTime fecha;
    }
}

