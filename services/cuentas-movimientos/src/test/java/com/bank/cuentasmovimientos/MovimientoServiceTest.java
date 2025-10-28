package com.bank.cuentasmovimientos;

import com.bank.cuentasmovimientos.dto.MovimientoRequest;
import com.bank.cuentasmovimientos.service.MovimientoService;
import com.bank.cuentasmovimientos.handler.SaldoNoDisponibleException;
import com.bank.cuentasmovimientos.domain.*;
import com.bank.cuentasmovimientos.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MovimientoServiceTest {

    private CuentaRepo cuentaRepo;
    private MovimientoRepo movRepo;
    private MovimientoService service;

    @BeforeEach
    void setup() {
        cuentaRepo = mock(CuentaRepo.class);
        movRepo = mock(MovimientoRepo.class);
        service = new MovimientoService(cuentaRepo, movRepo);
    }

    @Test
    @DisplayName("Debe aplicar un movimiento CREDITO correctamente")
    void aplicar_credito_incrementa_saldo() {
        var cuenta = Cuenta.builder()
                .id(UUID.randomUUID()).numero("200").tipo(TipoCuenta.AHORROS)
                .saldoInicial(BigDecimal.ZERO).saldo(new BigDecimal("100.00"))
                .estado("ACTIVA").clienteId(UUID.randomUUID()).build();

        when(cuentaRepo.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));
        when(movRepo.findByIdempotencyKey("k1")).thenReturn(Optional.empty());

        var req = new MovimientoRequest(cuenta.getId(), TipoMovimiento.CREDITO,
                new BigDecimal("50.00"), "abono", "k1");

        var res = service.aplicar(req);

        assertEquals(TipoMovimiento.CREDITO, res.tipo());
        assertEquals(new BigDecimal("150.00"), res.saldoPosterior());
        verify(movRepo).save(Mockito.any(Movimiento.class));
    }

    @Test
    @DisplayName("Debe aplicar un movimiento DEBITO válido y disminuir saldo")
    void aplicar_debito_disminuye_saldo() {
        var cuenta = Cuenta.builder()
                .id(UUID.randomUUID()).numero("300").tipo(TipoCuenta.CORRIENTE)
                .saldoInicial(BigDecimal.ZERO).saldo(new BigDecimal("100.00"))
                .estado(Status.ACTIVA.toString()).clienteId(UUID.randomUUID()).build();

        when(cuentaRepo.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));
        when(movRepo.findByIdempotencyKey("k1")).thenReturn(Optional.empty());

        var req = new MovimientoRequest(cuenta.getId(), TipoMovimiento.DEBITO,
                new BigDecimal("40.00"), "retiro", "k1");

        var res = service.aplicar(req);

        assertEquals(TipoMovimiento.DEBITO, res.tipo());
        assertEquals(new BigDecimal("60.00"), res.saldoPosterior());
        verify(movRepo).save(Mockito.any(Movimiento.class));
    }

    @Test
    @DisplayName("Debe lanzar error cuando el saldo es insuficiente")
    void debito_sin_saldo_lanza_error() {
        var cuenta = Cuenta.builder()
                .id(UUID.randomUUID()).numero("400").tipo(TipoCuenta.AHORROS)
                .saldoInicial(BigDecimal.ZERO).saldo(new BigDecimal("10.00"))
                .estado(Status.ACTIVA.toString()).clienteId(UUID.randomUUID()).build();

        when(cuentaRepo.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));
        when(movRepo.findByIdempotencyKey("k1")).thenReturn(Optional.empty());

        var req = new MovimientoRequest(cuenta.getId(), TipoMovimiento.DEBITO,
                new BigDecimal("50.00"), "compra", "k1");

        assertThrows(SaldoNoDisponibleException.class, () -> service.aplicar(req));
        verify(movRepo, never()).save(Mockito.any());
    }

    @Test
    @DisplayName("Debe retornar el mismo movimiento si ya existe con el idempotencyKey")
    void aplicar_movimiento_duplicado_devuelve_existente() {
        var cuenta = Cuenta.builder()
                .id(UUID.randomUUID()).numero("500").tipo(TipoCuenta.AHORROS)
                .saldoInicial(BigDecimal.ZERO).saldo(new BigDecimal("100.00"))
                .estado(Status.ACTIVA.toString()).clienteId(UUID.randomUUID()).build();

        var movimientoExistente = Movimiento.builder()
                .id(UUID.randomUUID())
                .cuenta(cuenta)
                .fecha(OffsetDateTime.now())
                .tipo(TipoMovimiento.CREDITO)
                .valor(new BigDecimal("100.00"))
                .saldoPosterior(new BigDecimal("200.00"))
                .idempotencyKey("k1")
                .build();

        when(movRepo.findByIdempotencyKey("k1")).thenReturn(Optional.of(movimientoExistente));

        var req = new MovimientoRequest(cuenta.getId(), TipoMovimiento.CREDITO,
                new BigDecimal("100.00"), "duplicado", "k1");

        var res = service.aplicar(req);

        assertEquals(movimientoExistente.getId(), res.movimientoId());
        verify(movRepo, never()).save(any());
    }

    @Test
    @DisplayName("Debe generar reporte de resumen correctamente")
    void obtener_resumen_funciona_correctamente() {
        var clienteId = UUID.randomUUID();
        var cuenta = Cuenta.builder().id(UUID.randomUUID()).numero("900").build();
        var movimientos = List.of(
                Movimiento.builder().cuenta(cuenta).tipo(TipoMovimiento.CREDITO)
                        .valor(new BigDecimal("200.00")).saldoPosterior(new BigDecimal("200.00")).build(),
                Movimiento.builder().cuenta(cuenta).tipo(TipoMovimiento.DEBITO)
                        .valor(new BigDecimal("50.00")).saldoPosterior(new BigDecimal("150.00")).build()
        );

        when(movRepo.findByClienteAndFechas(clienteId, LocalDate.now().minusDays(5), LocalDate.now()))
                .thenReturn(movimientos);

        var resumen = service.obtenerResumen(clienteId, LocalDate.now().minusDays(5), LocalDate.now());

        assertEquals(new BigDecimal("200.00"), resumen.getTotalCreditos());
        assertEquals(new BigDecimal("50.00"), resumen.getTotalDebitos());
        assertEquals(new BigDecimal("200.00"), resumen.getSaldoFinal());
    }

    @Test
    @DisplayName("Debe retornar lista vacía si no hay movimientos en el detalle")
    void obtener_detalle_devuelve_lista_vacia() {
        var clienteId = UUID.randomUUID();

        when(movRepo.findByClienteAndFechas(clienteId, LocalDate.now().minusDays(5), LocalDate.now()))
                .thenReturn(List.of());

        var detalle = service.obtenerDetalle(clienteId, LocalDate.now().minusDays(5), LocalDate.now());
        assertTrue(detalle.isEmpty());
    }
}

