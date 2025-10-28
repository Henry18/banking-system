package com.bank.personasclientes.dto;


import jakarta.validation.constraints.*;

public record ClienteCreateRequest(
        @NotBlank String nombre,
        String genero,
        @Min(0) Integer edad,
        @NotBlank String identificacion,
        String direccion,
        String telefono,
        @NotBlank String clientId,
        @NotBlank String password,
        @NotBlank String estado
) {}