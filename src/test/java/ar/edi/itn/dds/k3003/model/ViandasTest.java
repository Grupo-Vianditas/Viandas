package ar.edi.itn.dds.k3003.model;

import ar.edu.utn.dds.k3003.ClassFinder;
import ar.edu.utn.dds.k3003.facades.FachadaHeladeras;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoViandaEnum;
import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
public class ViandasTest {
  private static final Long COLABORADOR_ID = 1L;
  private static final Integer HELADERA_ID = 1;
  private static final String QR = "unQr";
  final LocalDateTime now = LocalDateTime.now();
  FachadaViandas fachadaViandas;
  ViandaDTO vianda;
  @Mock FachadaHeladeras fachadaHeladeras;

  public ViandasTest() {}

  @BeforeEach
  void setUp() throws Throwable {
    try {
      fachadaViandas =
          ClassFinder.findAndInstantiateClassImplementingInterface(
              "ar.edu.utn.dds.k3003.app.Fachada", FachadaViandas.class);
      vianda = new ViandaDTO(QR, now, EstadoViandaEnum.PREPARADA, COLABORADOR_ID, HELADERA_ID);
      fachadaViandas.setHeladerasProxy(fachadaHeladeras);
    } catch (Throwable var2) {
      Throwable $ex = var2;
      throw $ex;
    }
  }

  @AfterEach
  void tearUp() {}

  @Test
  @DisplayName("Cuando se agrega una vianda a un colaborador, se le asigna un id secuencial")
  void testAgregarVianda() {
    ViandaDTO viandaAgregada = fachadaViandas.agregar(vianda);
    ViandaDTO otraViandaAgregada = fachadaViandas.agregar(vianda);
    Assertions.assertEquals(viandaAgregada.getId() + 1, otraViandaAgregada.getId());
  }

  @Test
  @DisplayName("Cuando se modifica el estado de una vianda, este es guardado en el repositorio ")
  void testModificarEstado() {
    fachadaViandas.agregar(vianda);
    ViandaDTO viandaModificada = fachadaViandas.modificarEstado(QR, EstadoViandaEnum.DEPOSITADA);
    Assertions.assertEquals(EstadoViandaEnum.DEPOSITADA, viandaModificada.getEstado());
    ViandaDTO viandaEncontrada = fachadaViandas.buscarXQR(QR);
    Assertions.assertEquals(viandaModificada.getId(), viandaEncontrada.getId());
    Assertions.assertEquals(viandaModificada.getEstado(), viandaEncontrada.getEstado());
  }

  @Test
  @DisplayName(
      "Cuando se busca las viandas de un colaborador, solo las viandas de este son " + "obtenidas")
  void testViandasDeColaborador() {
    ViandaDTO viandaDeOtroColaborador =
        new ViandaDTO(QR, now, EstadoViandaEnum.PREPARADA, 2L, HELADERA_ID);
    fachadaViandas.agregar(vianda);
    fachadaViandas.agregar(vianda);
    fachadaViandas.agregar(viandaDeOtroColaborador);

    List<ViandaDTO> viandasEncontradas =
        fachadaViandas.viandasDeColaborador(COLABORADOR_ID, now.getMonthValue(), now.getYear());
    Assertions.assertEquals(2, viandasEncontradas.size());
    Assertions.assertEquals(
        COLABORADOR_ID,
        viandasEncontradas.stream().distinct().toList().stream()
            .findFirst()
            .get()
            .getColaboradorId());
  }

  @Test
  @DisplayName("Cuando se busca por QR, se obtiene la vianda apropiada")
  void testBuscarXQR() {
    ViandaDTO viandaConOtroQR =
        new ViandaDTO("otroQR", now, EstadoViandaEnum.PREPARADA, COLABORADOR_ID, HELADERA_ID);
    fachadaViandas.agregar(vianda);
    fachadaViandas.agregar(viandaConOtroQR);

    ViandaDTO viandaEncontrada = fachadaViandas.buscarXQR(QR);
    Assertions.assertEquals(QR, viandaEncontrada.getCodigoQR());
  }

  @Test
  @DisplayName(
      "Cuando hay una temperatura mayor a 5 en la heladera, se considera a la vianda vencida")
  void evaluarVencimiento() {
    fachadaViandas.agregar(vianda);
    Mockito.when(fachadaHeladeras.obtenerTemperaturas(HELADERA_ID))
        .thenReturn(
            List.of(
                new TemperaturaDTO(6, HELADERA_ID, now), new TemperaturaDTO(2, HELADERA_ID, now)));

    Assertions.assertTrue(
        fachadaViandas.evaluarVencimiento(QR),
        "Una de las temperaturas es " + "mayor a 5, por lo que la vianda esta vencida.");
  }

  @Test
  @DisplayName(
      "Cuando ninguna de las temperaturas de la heladera da mayor a 5, se considera a la vianda "
          + "no vencida")
  void evaluarVencimientoDeHeladeraNoVencida() {
    fachadaViandas.agregar(vianda);
    Mockito.when(fachadaHeladeras.obtenerTemperaturas(HELADERA_ID))
        .thenReturn(
            List.of(
                new TemperaturaDTO(3, HELADERA_ID, now), new TemperaturaDTO(2, HELADERA_ID, now)));

    Assertions.assertFalse(
        fachadaViandas.evaluarVencimiento(QR),
        "Ninguna de las temperaturas es " + "mayor a 5, por lo que la vianda no esta vencida.");
  }

  @Test
  @DisplayName("La heladera una vianda puede ser cambiada.")
  void evaluarModificarHeladera() {
    fachadaViandas.agregar(vianda);
    fachadaViandas.modificarHeladera(QR, 2);

    ViandaDTO viandaEncontrada = fachadaViandas.buscarXQR(QR);
    Assertions.assertEquals(2, viandaEncontrada.getHeladeraId());
  }

}
