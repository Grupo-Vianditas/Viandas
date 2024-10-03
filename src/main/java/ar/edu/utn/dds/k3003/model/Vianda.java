package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.facades.dtos.EstadoViandaEnum;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "viandas")
public class Vianda {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "qr",nullable = false,unique = true)
  private String qr;
  @Column(name = "colaboradorId",nullable = false)
  private Long colaboradorId;
  @Column(name = "heladeraId",nullable = false)
  private Integer heladeraId;
  @Enumerated(EnumType.STRING)
  @Column(name = "estado",nullable = false)
  private EstadoViandaEnum estado;
  @Column(name = "fechaElaboracion",nullable = false)
  private LocalDateTime fechaElaboracion;

  protected Vianda() {
    super();
  }

  public Vianda(
      String qr,
      Long colaboradorId,
      Integer heladeraId,
      EstadoViandaEnum estado,
      LocalDateTime fechaElaboracion
  ) {
    this.qr = qr;
    this.colaboradorId = colaboradorId;
    this.heladeraId = heladeraId;
    this.estado = estado;
    this.fechaElaboracion = fechaElaboracion;
  }
}
