package com.bank.cuentasmovimientos;

import com.bank.cuentasmovimientos.domain.TipoMovimiento;
import com.bank.cuentasmovimientos.dto.MovimientoRequest;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MovimientoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private UUID cuentaId;

    @BeforeEach
    void setup() {
        cuentaId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Debe registrar un movimiento de crédito correctamente")
    void testCrearCredito() throws Exception {
        MovimientoRequest request = new MovimientoRequest(
                cuentaId,
                TipoMovimiento.CREDITO,
                new BigDecimal("500.00"),
                "abono inicial",
                "mov-001"
        );

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo", is(TipoMovimiento.CREDITO.toString())))
                .andExpect(jsonPath("$.valor", is(500.0)));
    }

    @Test
    @DisplayName("Debe registrar un movimiento de débito y ajustar saldo")
    void testCrearDebito() throws Exception {
        MovimientoRequest request = new MovimientoRequest(
                cuentaId,
                TipoMovimiento.DEBITO,
                new BigDecimal("200.00"),
                "retiro cajero",
                "mov-002"
        );

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo", is(TipoMovimiento.DEBITO.toString())))
                .andExpect(jsonPath("$.valor", is(200.0)));
    }

    @Test
    @DisplayName("Debe evitar duplicados por idempotencyKey")
    void testIdempotencia() throws Exception {
        MovimientoRequest request = new MovimientoRequest(
                cuentaId,
                TipoMovimiento.CREDITO,
                new BigDecimal("100.00"),
                "prueba duplicado",
                "mov-003"
        );

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Debe listar los movimientos por cuenta correctamente")
    void testListarMovimientosPorCuenta() throws Exception {
        MovimientoRequest request = new MovimientoRequest(
                cuentaId,
                TipoMovimiento.CREDITO,
                new BigDecimal("100.00"),
                "mov test",
                "mov-004"
        );
        mockMvc.perform(post("/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        mockMvc.perform(get("/movimientos/cuenta/{cuentaId}", cuentaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].tipo", is(TipoMovimiento.CREDITO.toString())));
    }
}
