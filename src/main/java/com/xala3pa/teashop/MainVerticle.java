package com.xala3pa.teashop;

import com.xala3pa.teashop.database.TeaDatabaseVerticle;
import com.xala3pa.teashop.http.HttpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    Future<String> teaDBVerticleDeployment = Future.future();
    vertx.deployVerticle(new TeaDatabaseVerticle(), teaDBVerticleDeployment.completer());

    teaDBVerticleDeployment.compose(id -> {
      Future<String> httpVerticleDeployment = Future.future();
      vertx.deployVerticle(new HttpServerVerticle(), httpVerticleDeployment.completer());
      return httpVerticleDeployment;
    }).setHandler(ar -> {
      if (ar.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(ar.cause());
      }
    });
  }

}
