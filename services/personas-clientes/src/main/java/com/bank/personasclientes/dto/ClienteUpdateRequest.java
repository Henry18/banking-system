package com.bank.personasclientes.dto;

public record ClienteUpdateRequest(
        String nombre,
        String genero,
        Integer edad,
        String direccion,
        String telefono,
        String password,
        String estado
) {}

