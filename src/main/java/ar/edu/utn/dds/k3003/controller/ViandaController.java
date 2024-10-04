package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.List;
import java.util.NoSuchElementException;

public class ViandaController {

  private final Fachada fachada;

  public ViandaController(Fachada fachada) {
    this.fachada = fachada;
  }

  public void agregarVianda(Context context) {
    var viandaDTO = context.bodyAsClass(ViandaDTO.class);
    try {
      var viandaDTORta = this.fachada.agregar(viandaDTO);

      context.json(viandaDTORta);
      context.status(HttpStatus.CREATED);
    } catch (RuntimeException ex) {
      context.result(ex.getLocalizedMessage());
      context.status(HttpStatus.BAD_REQUEST);
    }

  }

  public void preloadDB(Context context) {
    try {
      var viandasRespuesta = this.fachada.preloadDB();

      context.json(viandasRespuesta);
      context.status(HttpStatus.CREATED);
    } catch (RuntimeException ex) {
      context.result(ex.getLocalizedMessage());
      context.status(HttpStatus.BAD_REQUEST);
    }

  }

  public void findByColaboradorIdAndAnioAndMes(Context context) {
    var colaboradorId = context.queryParamAsClass("colaboradorId", Long.class)
        .get();
    var anio = context.queryParamAsClass("anio", Integer.class)
        .get();
    var mes = context.queryParamAsClass("mes", Integer.class)
        .get();
    try {
      var viandaDTOS = this.fachada.viandasDeColaborador(colaboradorId, mes, anio);
      context.json(viandaDTOS);
    } catch (NoSuchElementException ex) {
      context.result(ex.getLocalizedMessage());
      context.status(HttpStatus.NOT_FOUND);
    }
  }

  public void buscarPorQR(Context context) {
    var qr = context.pathParamAsClass("qr", String.class)
        .get();

    try {
      var viandaDTO = this.fachada.buscarXQR(qr);
      context.json(viandaDTO);
    } catch (NoSuchElementException ex) {
      context.result(ex.getLocalizedMessage());
      context.status(HttpStatus.NOT_FOUND);
    }
  }

  public void evaluarVencimiento(Context context) {
    var qr = context.pathParamAsClass("qr", String.class)
        .get();

    try {
      var viandaDTO = this.fachada.evaluarVencimiento(qr);
      context.json(viandaDTO);
    } catch (NoSuchElementException ex) {
      context.result(ex.getLocalizedMessage());
      context.status(HttpStatus.NOT_FOUND);
    }
  }

  public void modificarHeladera(Context context) {
    var qr = context.pathParamAsClass("qrVianda", String.class)
        .get();
    var heladeraId = context.queryParamAsClass("heladeraId", Integer.class)
        .get();

    var viandaDTO = this.fachada.modificarHeladera(qr, heladeraId);
    context.json(viandaDTO);
  }

  public void limpiarDB(Context context) {
    fachada.clearDB();
    context.status(HttpStatus.NO_CONTENT);
  }
}
