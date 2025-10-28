package com.bank.cuentasmovimientos.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name="cuenta")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cuenta {
    @Id private UUID id;
    @Column(nullable=false, unique=true)
    private String numero;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TipoCuenta tipo;
    @Column(name="saldo_inicial", nullable=false)
    private BigDecimal saldoInicial;
    @Column(nullable=false)
    private BigDecimal saldo;
    @Column(nullable=false)
    private String estado;
    @Column(name="cliente_id", nullable=false) private UUID clienteId;

    public void acreditar(BigDecimal valor){ this.saldo = this.saldo.add(valor); }
    public void debitar(BigDecimal valor){
        if(this.saldo.compareTo(valor) < 0) throw new IllegalStateException("Saldo no disponible");
        this.saldo = this.saldo.subtract(valor);
    }
}
