package com.bank.personasclientes.domain;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity @Table(name="persona",
  uniqueConstraints=@UniqueConstraint(name="uk_persona_ident", columnNames="identificacion"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Persona {
  @Id private UUID id;
  private String nombre;
  private String genero;
  private Integer edad;
  @Column(nullable=false) private String identificacion;
  private String direccion;
  private String telefono;
}