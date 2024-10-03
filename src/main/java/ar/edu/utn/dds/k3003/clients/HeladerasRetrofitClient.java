package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface HeladerasRetrofitClient {

  @GET("heladeras/{heladeraId}/temperaturas")
  Call<List<TemperaturaDTO>> get(@Path("heladeraId") Integer heladeraId);
}