CREATE TABLE IF NOT EXISTS persona (
  id UUID PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  genero VARCHAR(20),
  edad INT CHECK (edad >= 0),
  identificacion VARCHAR(50) NOT NULL UNIQUE,
  direccion VARCHAR(200),
  telefono VARCHAR(40)
);

CREATE TABLE IF NOT EXISTS cliente (
  id UUID PRIMARY KEY,
  client_id VARCHAR(60) NOT NULL UNIQUE,
  password_hash VARCHAR(200) NOT NULL,
  estado VARCHAR(20) NOT NULL,
  persona_id UUID NOT NULL UNIQUE REFERENCES persona(id) ON DELETE CASCADE
);