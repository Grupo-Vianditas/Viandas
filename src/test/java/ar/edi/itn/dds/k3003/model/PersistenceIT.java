package ar.edi.itn.dds.k3003.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ar.edu.utn.dds.k3003.facades.dtos.EstadoViandaEnum;
import ar.edu.utn.dds.k3003.model.Vianda;
import java.time.LocalDateTime;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PersistenceIT {
  private static final Long COLABORADOR_ID = 1L;
  private static final Integer HELADERA_ID = 1;
  private static final String QR = "unQr";
  static EntityManagerFactory entityManagerFactory;
  final LocalDateTime now = LocalDateTime.now();
  EntityManager entityManager;

  @BeforeAll
  public static void setUpClass() throws Exception {
    entityManagerFactory = Persistence.createEntityManagerFactory("copiamedb");
  }

  @BeforeEach
  public void setup() throws Exception {
    entityManager = entityManagerFactory.createEntityManager();
  }

  @Test
  public void testConectar() {
// vacío, para ver que levante el ORM
  }

  @Test
  public void testGuardarYRecuperarDoc() throws Exception {
    Vianda vianda = new Vianda(QR, COLABORADOR_ID, HELADERA_ID, EstadoViandaEnum.PREPARADA, now);
    entityManager.getTransaction()
        .begin();
    entityManager.persist(vianda);
    entityManager.getTransaction()
        .commit();
    entityManager.close();

    entityManager = entityManagerFactory.createEntityManager();
    Vianda otraVianda = entityManager.find(Vianda.class, 1L);

    assertEquals(vianda.getId(), otraVianda.getId()); // también puede redefinir el equals
  }

}
