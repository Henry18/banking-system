package com.bank.cuentasmovimientos.integration;

import com.bank.cuentasmovimientos.domain.Status;
import com.bank.cuentasmovimientos.dto.MovimientoRequest;
import com.bank.cuentasmovimientos.domain.Cuenta;
import com.bank.cuentasmovimientos.domain.TipoCuenta;
import com.bank.cuentasmovimientos.domain.TipoMovimiento;
import com.bank.cuentasmovimientos.repository.CuentaRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReporteEstadoCuentaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CuentaRepo cuentaRepo;

    private UUID clienteId;
    private UUID cuentaId;

    @BeforeEach
    void setup() {
        clienteId = UUID.randomUUID();

        var cuenta = Cuenta.builder()
                .id(UUID.randomUUID())
                .numero("777")
                .tipo(TipoCuenta.AHORROS)
                .saldoInicial(BigDecimal.ZERO)
                .saldo(new BigDecimal("500.00"))
                .estado(Status.ACTIVA.toString())
                .clienteId(clienteId)
                .build();

        cuentaRepo.save(cuenta);
        cuentaId = cuenta.getId();
    }

    @Test
    @DisplayName("Debe generar un estado de cuenta correcto con totales de créditos y débitos")
    void generarEstadoCuenta_ok() throws Exception {
        var credito = new MovimientoRequest(
                cuentaId, TipoMovimiento.CREDITO,
                new BigDecimal("200.00"), "Abono de prueba", "key-cred"
        );
        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(credito)))
                .andExpect(status().isOk());

        var debito = new MovimientoRequest(
                cuentaId, TipoMovimiento.DEBITO,
                new BigDecimal("50.00"), "Retiro ATM", "key-deb"
        );
        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(debito)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reportes/estado-cuenta")
                        .param("clienteId", clienteId.toString())
                        .param("fechaInicio", LocalDate.now().minusDays(10).toString())
                        .param("fechaFin", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId", is(clienteId.toString())))
                .andExpect(jsonPath("$.totalCreditos", is(200.0)))
                .andExpect(jsonPath("$.totalDebitos", is(50.0)))
                .andExpect(jsonPath("$.saldoTotalFinal", greaterThan(0.0)))
                .andExpect(jsonPath("$.cuentas", hasSize(1)))
                .andExpect(jsonPath("$.cuentas[0].movimientos", hasSize(2)))
                .andExpect(jsonPath("$.cuentas[0].totalCreditos", is(200.0)))
                .andExpect(jsonPath("$.cuentas[0].totalDebitos", is(50.0)));
    }

    @Test
    @DisplayName("Debe devolver totales en cero cuando no hay movimientos")
    void generarEstadoCuenta_sin_movimientos() throws Exception {
        mockMvc.perform(get("/reportes/estado-cuenta")
                        .param("clienteId", clienteId.toString())
                        .param("fechaInicio", LocalDate.now().minusDays(10).toString())
                        .param("fechaFin", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCreditos", is(0.0)))
                .andExpect(jsonPath("$.totalDebitos", is(0.0)))
                .andExpect(jsonPath("$.saldoTotalFinal", is(0.0)))
                .andExpect(jsonPath("$.cuentas", hasSize(0)));
    }
}
