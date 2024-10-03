package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaHeladeras;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoViandaEnum;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import ar.edu.utn.dds.k3003.model.Vianda;
import ar.edu.utn.dds.k3003.repositories.ViandaMapper;
import ar.edu.utn.dds.k3003.repositories.ViandaRepository;
import ar.edu.utn.dds.k3003.repositories.utils.PersistenceUtils;
import io.javalin.http.BadRequestResponse;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class Fachada implements FachadaViandas {
  private final ViandaMapper viandaMapper;
  private final ViandaRepository viandaRepository;
  private FachadaHeladeras fachadaHeladeras;
  private EntityManagerFactory entityManagerFactory;
  private EntityManager entityManager;
  private AtomicInteger viandasPreparadasData;
  private AtomicInteger viandasVencidasData;
  private AtomicInteger viandasEnTrasladoData;

  public Fachada(PrometheusMeterRegistry registry) {
    this.entityManagerFactory = PersistenceUtils.createEntityManagerFactory();
    this.entityManager = entityManagerFactory.createEntityManager();
    this.viandaMapper = new ViandaMapper();
    this.viandaRepository = new ViandaRepository(entityManager);

    this.viandasPreparadasData = new AtomicInteger(0);
    this.viandasVencidasData = new AtomicInteger(0);
    this.viandasEnTrasladoData = new AtomicInteger(0);

    Gauge.builder("viandas_preparadas", viandasPreparadasData, AtomicInteger::get)
        .register(registry);
    Gauge.builder("viandas_vencidas", viandasVencidasData, AtomicInteger::get)
        .register(registry);
    Gauge.builder("viandas_enTraslado", viandasEnTrasladoData, AtomicInteger::get)
        .register(registry);
  }

  @Override
  public ViandaDTO agregar(ViandaDTO viandaDTO) {
    Vianda vianda =
        new Vianda(viandaDTO.getCodigoQR(), viandaDTO.getColaboradorId(), viandaDTO.getHeladeraId(),
            viandaDTO.getEstado(), viandaDTO.getFechaElaboracion()
        );
    vianda.setEstado(EstadoViandaEnum.PREPARADA);
    vianda = this.viandaRepository.save(vianda);
    viandasPreparadasData.incrementAndGet();
    return viandaMapper.map(vianda);
  }

  @Override
  public ViandaDTO modificarEstado(
      String qr,
      EstadoViandaEnum estadoViandaEnum
  ) throws NoSuchElementException {
    Vianda viandaEncontrada = viandaRepository.findByQr(qr);
    if (estadoViandaEnum.equals(viandaEncontrada.getEstado())) {
      return viandaMapper.map(viandaEncontrada);
    }
    if (estadoViandaEnum.equals(EstadoViandaEnum.PREPARADA)) {
      viandasPreparadasData.incrementAndGet();
    }
    if (viandaEncontrada.getEstado()
        .equals(EstadoViandaEnum.PREPARADA)) {
      viandasPreparadasData.decrementAndGet();
    }
    if (estadoViandaEnum.equals(EstadoViandaEnum.EN_TRASLADO)) {
      viandasPreparadasData.incrementAndGet();
    }
    if (viandaEncontrada.getEstado()
        .equals(EstadoViandaEnum.EN_TRASLADO)) {
      viandasEnTrasladoData.decrementAndGet();
    }
    if (viandaEncontrada.getEstado()
        .equals(EstadoViandaEnum.VENCIDA)) {
      throw new BadRequestResponse("La vianda esta vencida, no se puede modificar el estado.");
    }
    if (estadoViandaEnum.equals(EstadoViandaEnum.VENCIDA) && !evaluarVencimiento(qr)) {
      throw new BadRequestResponse(
          "La vianda no esta vencida, no se puede modificar al estado vencida.");
    }
    viandaEncontrada.setEstado(estadoViandaEnum);
    viandaEncontrada = viandaRepository.save(viandaEncontrada);
    return viandaMapper.map(viandaEncontrada);
  }

  @Override
  public List<ViandaDTO> viandasDeColaborador(
      Long colaboradorId,
      Integer mes,
      Integer anio
  ) throws NoSuchElementException {
    return viandaRepository.findByCollaboratorIdAndYearAndMonth(colaboradorId, mes, anio)
        .stream()
        .map(viandaMapper::map)
        .toList();
  }

  @Override
  public ViandaDTO buscarXQR(String qr) throws NoSuchElementException {
    Vianda viandaEncontrada = viandaRepository.findByQr(qr);
    return viandaMapper.map(viandaEncontrada);
  }

  @Override
  public void setHeladerasProxy(FachadaHeladeras fachadaHeladeras) {
    this.fachadaHeladeras = fachadaHeladeras;
  }

  @Override
  public boolean evaluarVencimiento(String qr) throws NoSuchElementException {
    Vianda viandaEncontrada = viandaRepository.findByQr(qr);
    if (fachadaHeladeras.obtenerTemperaturas(viandaEncontrada.getHeladeraId())
        .stream()
        .anyMatch(temperaturaDTO -> temperaturaDTO.getTemperatura() >= 5)) {
      viandaEncontrada.setEstado(EstadoViandaEnum.VENCIDA);
      viandaRepository.save(viandaEncontrada);
      viandasVencidasData.incrementAndGet();
      return true;
    }
    return false;
  }

  @Override
  public ViandaDTO modificarHeladera(
      String qr,
      int nuevaHeladera
  ) {
    Vianda viandaEncontrada = viandaRepository.findByQr(qr);
    viandaEncontrada.setHeladeraId(nuevaHeladera);
    viandaEncontrada = viandaRepository.save(viandaEncontrada);
    return viandaMapper.map(viandaEncontrada);
  }

  public void clearDB() {
    viandaRepository.clearDB();
  }

}
