CREATE TABLE IF NOT EXISTS cuenta (
  id UUID PRIMARY KEY,
  numero VARCHAR(40) NOT NULL UNIQUE,
  tipo VARCHAR(20) NOT NULL,
  saldo_inicial NUMERIC(18, 2) NOT NULL DEFAULT 0,
  saldo NUMERIC(18, 2) NOT NULL DEFAULT 0,
  estado VARCHAR(20) NOT NULL,
  cliente_id UUID NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cuenta_cliente ON cuenta(cliente_id);

CREATE TABLE IF NOT EXISTS movimiento (
  id UUID PRIMARY KEY,
  cuenta_id UUID NOT NULL REFERENCES cuenta(id) ON DELETE CASCADE,
  fecha TIMESTAMP WITH TIME ZONE NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  valor NUMERIC(18, 2) NOT NULL,
  saldo_posterior NUMERIC(18, 2) NOT NULL,
  referencia VARCHAR(200),
  idempotency_key VARCHAR(80) NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_mov_cuenta_fecha ON movimiento(cuenta_id, fecha);