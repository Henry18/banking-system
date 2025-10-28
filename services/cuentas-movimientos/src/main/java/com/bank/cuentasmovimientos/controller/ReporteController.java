package com.bank.cuentasmovimientos.controller;

import com.bank.cuentasmovimientos.dto.ReporteDetalleDTO;
import com.bank.cuentasmovimientos.dto.ReporteEstadoCuentaResponse;
import com.bank.cuentasmovimientos.dto.ReporteResumenDTO;
import com.bank.cuentasmovimientos.service.MovimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reportes")
@Tag(name = "Reportes", description = "Consulta de movimientos por cliente y rango de fechas")
@RequiredArgsConstructor
public class ReporteController {

    private final MovimientoService reporteService;

    @Operation(summary = "Generar reporte de movimientos por cliente")
    @GetMapping
    public ResponseEntity<?> generarReporte(
            @Parameter(description = "Identificador único del cliente", required = true)
            @RequestParam(name = "clientId") UUID clienteId,
            @Parameter(description = "Fecha inicial (yyyy-MM-dd)", required = false)
            @RequestParam(required = false, name = "fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha final (yyyy-MM-dd)", required = false)
            @RequestParam(required = false, name = "fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "Modo de reporte: detalle o resumen", required = false)
            @RequestParam(defaultValue = "detalle", name = "modo") String modo) {

        if ("resumen".equalsIgnoreCase(modo)) {
            ReporteResumenDTO resumen = reporteService.obtenerResumen(clienteId, fechaInicio, fechaFin);
            return ResponseEntity.ok(resumen);
        } else {
            List<ReporteDetalleDTO> detalle = reporteService.obtenerDetalle(clienteId, fechaInicio, fechaFin);
            return ResponseEntity.ok(detalle);
        }
    }

    @GetMapping("/estado-cuenta")
    @Operation(summary = "Reporte avanzado de estado de cuenta",
            description = "Devuelve el estado de cuenta consolidado por cliente, cuentas y movimientos.")
    public ResponseEntity<ReporteEstadoCuentaResponse> obtenerEstadoCuenta(
            @RequestParam(name = "clienteId") UUID clienteId,
            @RequestParam(name = "fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        ReporteEstadoCuentaResponse reporte = reporteService.obtenerEstadoCuenta(clienteId, fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }
}

