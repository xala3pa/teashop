package com.xala3pa.teashop.http;

import com.xala3pa.teashop.config.VertxConfig;
import com.xala3pa.teashop.database.TeaDatabaseService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {
  private static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
  private static final String CONFIG_TEADB_QUEUE = "teadb.queue";

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  private TeaController teaController;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    String teaDbQueue = config().getString(CONFIG_TEADB_QUEUE, "teadb.queue");
    TeaDatabaseService teaDatabaseService = TeaDatabaseService.createTeaDatabaseProxy(vertx, teaDbQueue);
    VertxConfig vertxConfig = new VertxConfig(teaDatabaseService);
    teaController = vertxConfig.getTeaController();
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {


    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.get("/").handler(teaController::indexHandler);
    router.get("/teas").handler(teaController::findAllTeas);
    router.get("/teas/type/:type").handler(teaController::findTeasByType);
    router.get("/teas/:id").handler(teaController::findTeaById);
    router.put("/teas/:id").handler(teaController::updateTeaByID);
    router.post("/teas").handler(teaController::addTea);
    router.delete("/teas/:id").handler(teaController::deleteTea);

    router.route("/tea/*").failureHandler(ErrorHandler.create());
    router.route("/").handler(TimeoutHandler.create());

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
}
