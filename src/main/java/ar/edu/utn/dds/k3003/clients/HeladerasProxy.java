package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.facades.FachadaHeladeras;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import ar.edu.utn.dds.k3003.facades.dtos.RetiroDTO;
import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.HttpStatus;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class HeladerasProxy implements FachadaHeladeras {

  private final String endpoint;
  private final HeladerasRetrofitClient service;

  public HeladerasProxy(ObjectMapper objectMapper) {

    var env = System.getenv();
    this.endpoint = env.getOrDefault("URL_HELADERAS", "http://localhost:8081/");

    var retrofit = new Retrofit.Builder().baseUrl(this.endpoint)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build();

    this.service = retrofit.create(HeladerasRetrofitClient.class);
  }

  @Override
  public HeladeraDTO agregar(HeladeraDTO heladeraDTO) {
    return null;
  }

  @Override
  public void depositar(
      Integer integer,
      String s
  ) throws NoSuchElementException {

  }

  @Override
  public Integer cantidadViandas(Integer integer) throws NoSuchElementException {
    return null;
  }

  @Override
  public void retirar(RetiroDTO retiroDTO) throws NoSuchElementException {

  }

  @Override
  public void temperatura(TemperaturaDTO temperaturaDTO) {

  }

  @Override
  public List<TemperaturaDTO> obtenerTemperaturas(Integer integer) {
    try {
      Response<List<TemperaturaDTO>> execute = service.get(integer)
          .execute();

      if (execute.isSuccessful()) {
        return execute.body();
      }
      if (execute.code() == HttpStatus.NOT_FOUND.getCode()) {
        throw new NoSuchElementException("no se encontro la heladera " + integer);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error conectandose con el componente heladeras");
    }
    throw new RuntimeException("Error conectandose con el componente heladeras");
  }

  @Override
  public void setViandasProxy(FachadaViandas fachadaViandas) {

  }
}