package com.xala3pa.teashop.http;

import com.xala3pa.teashop.database.TeaDatabaseService;
import com.xala3pa.teashop.domain.Tea;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {
  private static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
  private static final int CREATED = 201;
  private static final int BAD_REQUEST = 400;
  private static final String CONFIG_TEADB_QUEUE = "teadb.queue";

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  private TeaDatabaseService teaDatabaseService;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    InitializeTeaDBService();

    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.get("/").handler(this::indexHandler);
    router.post().handler(BodyHandler.create());
    router.post("/tea").handler(this::addTeaHandler);

    int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);
    server
      .requestHandler(router)
      .listen(portNumber, asyncResult -> {
        if (asyncResult.succeeded()) {
          LOGGER.info("HttpServerVerticle :: HTTP server running on port {}", portNumber);
          startFuture.complete();
        } else {
          LOGGER.error("HttpServerVerticle :: Could not start a HTTP server", asyncResult.cause());
          startFuture.fail(asyncResult.cause());
        }
      });
  }

  private void addTeaHandler(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();

    Handler<AsyncResult<Tea>> handler = reply -> {
      if (reply.succeeded()) {
          Tea tea = reply.result();
          String responseBody = Json.encodePrettily(tea.toJson());
          LOGGER.info("HttpServerVerticle :: New Tea added");
          sendSuccess(responseBody, response, CREATED);
      } else {
        LOGGER.error("HttpServerVerticle :: Error in the replay", reply.cause());
        routingContext.fail(reply.cause());
      }
    };

    try {
      final Tea tea = Json.decodeValue(routingContext.getBodyAsString(), Tea.class);
      teaDatabaseService.addTea(tea, handler);
    } catch (DecodeException ex) {
      LOGGER.error("HttpServerVerticle :: Error adding a new tea - ex: {}", ex.getStackTrace());
      sendError(BAD_REQUEST, response);
    }

  }

  private void indexHandler(RoutingContext routingContext) {
    LOGGER.info("HttpServerVerticle :: Calling index router");

    HttpServerResponse response = routingContext.response();

    response.putHeader("content-type", "text/html")
      .putHeader("charset", "UTF-8")
      .end("Simple Teashop made with &#x2764 and Vert.x");
  }

  private void InitializeTeaDBService() {
    String teaDbQueue = config().getString(CONFIG_TEADB_QUEUE, "teadb.queue");
    teaDatabaseService = TeaDatabaseService.createTeaDatabaseProxy(vertx, teaDbQueue);
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
