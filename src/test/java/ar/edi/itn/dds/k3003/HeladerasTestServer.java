package ar.edi.itn.dds.k3003;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoViandaEnum;
import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import ar.edu.utn.dds.k3003.facades.dtos.TrasladoDTO;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.json.JavalinJackson;

import java.time.LocalDateTime;
import java.util.List;

import static ar.edu.utn.dds.k3003.app.WebApp.configureObjectMapper;
public class HeladerasTestServer {

  public static void main(String[] args) throws Exception {

    var env = System.getenv();

    var port = Integer.parseInt(env.getOrDefault("PORT", "8081"));

    var app = Javalin.create(config -> {
      config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
        configureObjectMapper(mapper);
      }));
    }).start(port);

    app.get("/heladeras/{heladeraId}/temperaturas", HeladerasTestServer::obtenerTemperaturas);
  }


  private static void obtenerTemperaturas(Context context) {

    var heladeraId = context.pathParam("heladeraId");
    if (heladeraId.equals("0")) {
      var temperaturaDTO = new TemperaturaDTO(5,0,LocalDateTime.now());
      context.json(List.of(temperaturaDTO));
    } else {
      context.result("Heladera no encontrada: " + heladeraId);
      context.status(HttpStatus.NOT_FOUND);
    }
  }
}