package com.bank.personasclientes;

import com.bank.personasclientes.domain.Status;
import com.bank.personasclientes.dto.ClienteCreateRequest;
import com.bank.personasclientes.dto.ClienteUpdateRequest;
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

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClienteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private ClienteCreateRequest createRequest;

    @BeforeEach
    void setup() {
        createRequest = new ClienteCreateRequest(
                "Juan Pérez",
                "M",
                30,
                "CC-12345",
                "Calle 1 #22-33",
                "3001234567",
                "juan.perez",
                "123456",
                Status.ACTIVE.toString()
        );
    }

    @Test
    @DisplayName("Debe crear un cliente con su persona asociada correctamente")
    void testCrearCliente() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId", is("juan.perez")))
                .andExpect(jsonPath("$.estado", is(Status.ACTIVE.toString())))
                .andExpect(jsonPath("$.nombre", is("Juan Pérez")));
    }

    @Test
    @DisplayName("Debe obtener la lista de clientes existentes")
    void testListarClientes() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clientId", is("juan.perez")));
    }

    @Test
    @DisplayName("Debe obtener un cliente por ID")
    void testObtenerClientePorId() throws Exception {
        String response = mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var json = mapper.readTree(response);
        var id = json.get("id").asText();

        mockMvc.perform(get("/clientes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Juan Pérez")));
    }

    @Test
    @DisplayName("Debe actualizar correctamente un cliente y su persona")
    void testActualizarCliente() throws Exception {
        String response = mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        var id = mapper.readTree(response).get("id").asText();

        var updateRequest = new ClienteUpdateRequest(
                "Juan P. Gómez",
                "M",
                31,
                "Carrera 10 #22-33",
                "3009998888",
                null,
                Status.INACTIVE.toString()
        );

        mockMvc.perform(put("/clientes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Juan P. Gómez")))
                .andExpect(jsonPath("$.estado", is(Status.INACTIVE.toString())));
    }

    @Test
    @DisplayName("Debe eliminar correctamente un cliente y su persona")
    void testEliminarCliente() throws Exception {
        String response = mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        var id = mapper.readTree(response).get("id").asText();

        mockMvc.perform(delete("/clientes/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clientes/{id}", id))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Debe rechazar creación si la identificación ya existe")
    void testDuplicadoIdentificacion() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().is4xxClientError());
    }
}
