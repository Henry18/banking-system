package com.bank.personasclientes.domain;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(name="cliente",
  uniqueConstraints=@UniqueConstraint(name="uk_cliente_clientid", columnNames="client_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {
  @Id private UUID id;

  @Column(name="client_id", nullable=false) private String clientId;
  @Column(name="password_hash", nullable=false) private String passwordHash;
  @Column(nullable=false) private String estado;

  @OneToOne(optional=false) @JoinColumn(name="persona_id", unique=true)
  private Persona persona;
}