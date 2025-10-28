package com.bank.personasclientes.dto;


import java.util.UUID;

public record ClienteResponse(
        UUID id, String clientId, String estado,
        String nombre, String identificacion, String telefono) {}