package com.bank.cuentasmovimientos.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="movimiento", uniqueConstraints=@UniqueConstraint(name="uk_idem", columnNames="idempotency_key"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Movimiento {
    @Id private UUID id;
    @ManyToOne(optional=false)
    @JoinColumn(name="cuenta_id")
    private Cuenta cuenta;
    private OffsetDateTime fecha;
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo;
    private BigDecimal valor;
    @Column(name="saldo_posterior")
    BigDecimal saldoPosterior;
    private String referencia;
    @Column(name="idempotency_key", nullable=false)
    private String idempotencyKey;
}
