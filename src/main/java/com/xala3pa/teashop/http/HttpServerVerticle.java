package com.xala3pa.teashop.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {
  private static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.get("/").handler(this::indexHandler);

    int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);
    server
      .requestHandler(router)
      .listen(portNumber, asyncResult -> {
        if (asyncResult.succeeded()) {
          LOGGER.info("HTTP server running on port {}", portNumber);
          startFuture.complete();
        } else {
          LOGGER.error("Could not start a HTTP server", asyncResult.cause());
          startFuture.fail(asyncResult.cause());
        }
      });
  }

  private void indexHandler(RoutingContext routingContext) {
    LOGGER.info("Calling index router");

    HttpServerResponse response = routingContext.response();

    response.putHeader("content-type", "text/html")
      .putHeader("charset", "UTF-8")
      .end("Simple Teashop made with &#x2764 and Vert.x");
  }
}
