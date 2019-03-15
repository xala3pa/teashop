package com.xala3pa.teashop.http;

import com.xala3pa.teashop.database.TeaDatabaseService;
import com.xala3pa.teashop.domain.Tea;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeaController {
  private static final int CREATED = 201;
  private static final int BAD_REQUEST = 400;
  private static final int OK = 200;

  private TeaDatabaseService teaDatabaseService;

  public TeaController(TeaDatabaseService teaDatabaseService) {
    this.teaDatabaseService = teaDatabaseService;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(TeaController.class);

  void indexHandler(RoutingContext routingContext) {
    LOGGER.info("HttpServerVerticle :: indexHandler - Calling index router");

    HttpServerResponse response = routingContext.response();

    response.putHeader("content-type", "text/html")
      .putHeader("charset", "UTF-8")
      .end("Simple Teashop made with &#x2764 and Vert.x");
  }

  void findAllTeas(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();

    Handler<AsyncResult<JsonArray>> handler = reply -> {
      if (reply.succeeded()) {
        String responseBody = Json.encodePrettily(reply.result());
        sendSuccess(responseBody, response, OK);
      } else {
        LOGGER.error("HttpServerVerticle :: findAllTeas - Error in the replay", reply.cause());
        routingContext.fail(reply.cause());
      }
    };

    teaDatabaseService.findAllTeas(handler);
  }

  void addTea(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();

    Handler<AsyncResult<Tea>> handler = reply -> {
      if (reply.succeeded()) {
        Tea tea = reply.result();
        String responseBody = Json.encodePrettily(tea.toJson());
        LOGGER.info("HttpServerVerticle :: addTea - New Tea added");
        sendSuccess(responseBody, response, CREATED);
      } else {
        LOGGER.error("HttpServerVerticle :: AddTea - Error in the replay", reply.cause());
        routingContext.fail(reply.cause());
      }
    };

    try {
      final Tea tea = Json.decodeValue(routingContext.getBodyAsString(), Tea.class);
      teaDatabaseService.addTea(tea, handler);
    } catch (DecodeException ex) {
      LOGGER.error("HttpServerVerticle :: addTea - Error adding a new tea - ex: {}", ex.getCause());
      sendError(BAD_REQUEST, response);
    }
  }

  void findTeasByType(RoutingContext routingContext) {
  }

  void findTeaById(RoutingContext routingContext) {
  }

  void updateTeaByID(RoutingContext routingContext) {
  }

  void deleteTea(RoutingContext routingContext) {
  }

  private void sendError(int statusCode, HttpServerResponse response) {
    response
      .putHeader("content-type", "application/json; charset=utf-8")
      .setStatusCode(statusCode)
      .end();
  }

  private void sendSuccess(String responseBody, HttpServerResponse response, int statusCode) {
    response
      .putHeader("content-type", "application/json; charset=utf-8")
      .setStatusCode(statusCode)
      .end(responseBody);
  }
}
