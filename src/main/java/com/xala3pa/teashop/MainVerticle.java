package com.xala3pa.teashop;

import com.xala3pa.teashop.http.HttpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    Future<String> httpVerticleDeployment = Future.future();
    vertx.deployVerticle(new HttpServerVerticle(), httpVerticleDeployment.completer());

    httpVerticleDeployment.setHandler(ar -> {
      if (ar.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(ar.cause());
      }
    });
  }

}
