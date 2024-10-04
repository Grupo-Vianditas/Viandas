package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.clients.HeladerasProxy;
import ar.edu.utn.dds.k3003.controller.ViandaController;
import ar.edu.utn.dds.k3003.facades.dtos.Constants;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.micrometer.MicrometerPlugin;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmHeapPressureMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebApp {

  private static final Logger log = LoggerFactory.getLogger(WebApp.class);

  public static void main(String[] args) {

    var env = System.getenv();
    String TOKEN = env.get("TOKEN");

    final var registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    registry.config()
        .commonTags("app", "metrics-sample");

    try (var jvmGcMetrics = new JvmGcMetrics();
         var jvmHeapPressureMetrics = new JvmHeapPressureMetrics()) {
      jvmGcMetrics.bindTo(registry);
      jvmHeapPressureMetrics.bindTo(registry);
    }
    new JvmMemoryMetrics().bindTo(registry);
    new ProcessorMetrics().bindTo(registry);
    new FileDescriptorMetrics().bindTo(registry);


    var objectMapper = createObjectMapper();
    var fachada = new Fachada(registry);
    fachada.setHeladerasProxy(new HeladerasProxy(objectMapper));

    var port = Integer.parseInt(env.getOrDefault("PORT", "8080"));
    var viandasController = new ViandaController(fachada);
    final var micrometerPlugin = new MicrometerPlugin(config -> config.registry = registry);

    var app = Javalin.create(config -> {
          config.registerPlugin(micrometerPlugin);
        })
        .get("/metrics", ctx -> {
          var auth = ctx.header("Authorization");
          log.info("auth:" + auth);
          log.info("TOKEN:" + TOKEN);
          if (Objects.equals(auth, "Bearer " + TOKEN)) {
            log.info("Getting metrics");
            ctx.contentType("text/plain; version=0.0.4")
                .result(registry.scrape());
          }
          else {
            log.info("unauthorized");
            ctx.status(401)
                .json("unauthorized access");
          }
        })
        .start(port);

    app.post("/viandas", viandasController::agregarVianda);
    app.delete("/viandas", viandasController::limpiarDB);
    app.get(
        "/viandas/search/findByColaboradorIdAndAnioAndMes",
        viandasController::findByColaboradorIdAndAnioAndMes
    );
    app.get("/viandas/{qr}", viandasController::buscarPorQR);
    app.get("/viandas/{qr}/vencida", viandasController::evaluarVencimiento);
    app.patch("/viandas/{qrVianda}", viandasController::modificarHeladera);

  }

  public static ObjectMapper createObjectMapper() {
    var objectMapper = new ObjectMapper();
    configureObjectMapper(objectMapper);
    return objectMapper;
  }

  public static void configureObjectMapper(ObjectMapper objectMapper) {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    var sdf = new SimpleDateFormat(Constants.DEFAULT_SERIALIZATION_FORMAT, Locale.getDefault());
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    objectMapper.setDateFormat(sdf);
  }
}