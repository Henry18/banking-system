package com.bank.cuentasmovimientos.service;

import com.bank.cuentasmovimientos.dto.*;
import com.bank.cuentasmovimientos.domain.*;
import com.bank.cuentasmovimientos.handler.SaldoNoDisponibleException;
import com.bank.cuentasmovimientos.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MovimientoService {
    @Autowired
    private CuentaRepo cuentaRepo;
    @Autowired
    private MovimientoRepo movRepo;

    @Transactional
    public MovimientoResponse aplicar(MovimientoRequest r){
        var dup = movRepo.findByIdempotencyKey(r.idempotencyKey());
        if(dup.isPresent()){
            var m = dup.get();
            return new MovimientoResponse(m.getId(), m.getCuenta().getId(), m.getTipo(), m.getValor(), m.getSaldoPosterior());
        }

        var cuenta = cuentaRepo.findById(r.cuentaId())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no existe"));

        BigDecimal valor = r.valor();
        if (r.tipo() == TipoMovimiento.CREDITO) {
            cuenta.acreditar(valor);
        } else {
            try { cuenta.debitar(valor); }
            catch (IllegalStateException e) { throw new SaldoNoDisponibleException("Saldo no disponible"); }
        }

        var mov = Movimiento.builder()
                .id(UUID.randomUUID())
                .cuenta(cuenta)
                .fecha(OffsetDateTime.now())
                .tipo(r.tipo())
                .valor(valor)
                .saldoPosterior(cuenta.getSaldo())
                .referencia(r.referencia())
                .idempotencyKey(r.idempotencyKey())
                .build();

        movRepo.save(mov);

        return new MovimientoResponse(mov.getId(), cuenta.getId(), r.tipo(), valor, mov.getSaldoPosterior());
    }

    public List<ReporteDetalleDTO> obtenerDetalle(UUID clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Movimiento> movimientos = movRepo.findByClienteAndFechas(clienteId, fechaInicio, fechaFin);

        return movimientos.stream().map(m -> new ReporteDetalleDTO(
                m.getFecha().toLocalDateTime(),
                m.getTipo().toString(),
                m.getValor(),
                m.getSaldoPosterior(),
                m.getReferencia(),
                m.getCuenta().getNumero()
        )).collect(Collectors.toList());
    }

    public ReporteResumenDTO obtenerResumen(UUID clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Movimiento> movimientos = movRepo.findByClienteAndFechas(clienteId, fechaInicio, fechaFin);

        BigDecimal totalCreditos = movimientos.stream()
                .filter(m -> TipoMovimiento.CREDITO.toString().equalsIgnoreCase(m.getTipo().toString()))
                .map(m -> m.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitos = movimientos.stream()
                .filter(m -> TipoMovimiento.DEBITO.toString().equalsIgnoreCase(m.getTipo().toString()))
                .map(m -> m.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoFinal = movimientos.isEmpty() ? BigDecimal.ZERO :
                movimientos.get(0).getSaldoPosterior();

        return new ReporteResumenDTO(clienteId.toString(), totalCreditos, totalDebitos, saldoFinal);
    }

    public ReporteEstadoCuentaResponse obtenerEstadoCuenta(UUID clienteId, LocalDate fechaInicio, LocalDate fechaFin) {

        List<Movimiento> movimientos = movRepo.findByClienteAndFechas(clienteId, fechaInicio, fechaFin);

        if (movimientos.isEmpty()) {
            return ReporteEstadoCuentaResponse.builder()
                    .clienteId(clienteId)
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .generadoEn(LocalDateTime.now())
                    .saldoTotalInicial(BigDecimal.ZERO)
                    .totalCreditos(BigDecimal.ZERO)
                    .totalDebitos(BigDecimal.ZERO)
                    .saldoTotalFinal(BigDecimal.ZERO)
                    .cuentas(List.of())
                    .build();
        }

        Map<Cuenta, List<Movimiento>> agrupadoPorCuenta = movimientos.stream()
                .collect(Collectors.groupingBy(Movimiento::getCuenta));

        BigDecimal totalInicial = BigDecimal.ZERO;
        BigDecimal totalCreditos = BigDecimal.ZERO;
        BigDecimal totalDebitos = BigDecimal.ZERO;
        BigDecimal totalFinal = BigDecimal.ZERO;

        List<ReporteEstadoCuentaResponse.CuentaDetalle> cuentas = new ArrayList<>();

        for (Map.Entry<Cuenta, List<Movimiento>> entry : agrupadoPorCuenta.entrySet()) {
            Cuenta cuenta = entry.getKey();
            List<Movimiento> movs = entry.getValue();

            movs.sort(Comparator.comparing(Movimiento::getFecha));

            BigDecimal saldoInicial = movs.get(0).getSaldoPosterior();
            BigDecimal sumCreditos = movs.stream()
                    .filter(m -> m.getTipo().equals(TipoMovimiento.CREDITO))
                    .map(Movimiento::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumDebitos = movs.stream()
                    .filter(m -> m.getTipo().equals(TipoMovimiento.DEBITO))
                    .map(Movimiento::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal saldoFinal = movs.get(movs.size() - 1).getSaldoPosterior();

            totalInicial = totalInicial.add(saldoInicial);
            totalCreditos = totalCreditos.add(sumCreditos);
            totalDebitos = totalDebitos.add(sumDebitos);
            totalFinal = totalFinal.add(saldoFinal);

            List<ReporteEstadoCuentaResponse.MovimientoDetalle> detalleMovs = movs.stream()
                    .map(m -> ReporteEstadoCuentaResponse.MovimientoDetalle.builder()
                            .tipo(String.valueOf(m.getTipo()))
                            .valor(m.getValor())
                            .saldoDisponible(m.getSaldoPosterior())
                            .referencia(m.getReferencia())
                            .fecha(m.getFecha().toLocalDateTime())
                            .build())
                    .toList();

            cuentas.add(ReporteEstadoCuentaResponse.CuentaDetalle.builder()
                    .cuentaId(cuenta.getId())
                    .numero(cuenta.getNumero())
                    .tipo(String.valueOf(cuenta.getTipo()))
                    .saldoInicial(saldoInicial)
                    .totalCreditos(sumCreditos)
                    .totalDebitos(sumDebitos)
                    .saldoFinal(saldoFinal)
                    .movimientos(detalleMovs)
                    .build());
        }

        return ReporteEstadoCuentaResponse.builder()
                .clienteId(clienteId)
                .nombreCliente("Cliente #" + clienteId.toString().substring(0, 8))
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .generadoEn(LocalDateTime.now())
                .saldoTotalInicial(totalInicial)
                .totalCreditos(totalCreditos)
                .totalDebitos(totalDebitos)
                .saldoTotalFinal(totalFinal)
                .cuentas(cuentas)
                .build();
    }

}
