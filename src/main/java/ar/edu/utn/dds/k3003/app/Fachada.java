package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaHeladeras;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoViandaEnum;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import ar.edu.utn.dds.k3003.model.Vianda;
import ar.edu.utn.dds.k3003.repositories.ViandaMapper;
import ar.edu.utn.dds.k3003.repositories.ViandaRepository;
import ar.edu.utn.dds.k3003.repositories.utils.PersistenceUtils;
import java.util.List;
import java.util.NoSuchElementException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Fachada implements FachadaViandas {
  private final ViandaMapper viandaMapper;
  private final ViandaRepository viandaRepository;
  private FachadaHeladeras fachadaHeladeras;
  private EntityManagerFactory entityManagerFactory;
  private EntityManager entityManager;

  public Fachada() {
    this.entityManagerFactory = PersistenceUtils.createEntityManagerFactory();
    this.entityManager = entityManagerFactory.createEntityManager();
    this.viandaMapper = new ViandaMapper();
    this.viandaRepository = new ViandaRepository(entityManager);
  }

  @Override
  public ViandaDTO agregar(ViandaDTO viandaDTO) {
    Vianda vianda =
        new Vianda(
            viandaDTO.getCodigoQR(),
            viandaDTO.getColaboradorId(),
            viandaDTO.getHeladeraId(),
            viandaDTO.getEstado(),
            viandaDTO.getFechaElaboracion());
    vianda = this.viandaRepository.save(vianda);
    return viandaMapper.map(vianda);
  }

  @Override
  public ViandaDTO modificarEstado(String qr, EstadoViandaEnum estadoViandaEnum)
      throws NoSuchElementException {
    Vianda viandaEncontrada = viandaRepository.findByQr(qr);
    viandaEncontrada.setEstado(estadoViandaEnum);
    viandaEncontrada = viandaRepository.save(viandaEncontrada);
    return viandaMapper.map(viandaEncontrada);
  }

  @Override
  public List<ViandaDTO> viandasDeColaborador(Long colaboradorId, Integer mes, Integer anio)
      throws NoSuchElementException {
    return viandaRepository.findByCollaboratorIdAndYearAndMonth(colaboradorId, mes, anio).stream()
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
    return fachadaHeladeras.obtenerTemperaturas(viandaEncontrada.getHeladeraId()).stream()
        .anyMatch(temperaturaDTO -> temperaturaDTO.getTemperatura() >= 5);
  }

  @Override
  public ViandaDTO modificarHeladera(String qr, int nuevaHeladera) {
    Vianda viandaEncontrada = viandaRepository.findByQr(qr);
    viandaEncontrada.setHeladeraId(nuevaHeladera);
    viandaEncontrada = viandaRepository.save(viandaEncontrada);
    return viandaMapper.map(viandaEncontrada);
  }

  public void clearDB(){
    viandaRepository.clearDB();
  }

}
