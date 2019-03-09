package com.xala3pa.teashop.database;

import com.xala3pa.teashop.domain.Tea;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.jdbc.JDBCClient;

@ProxyGen
public interface TeaDatabaseService {

  @GenIgnore
  static TeaDatabaseService createTeaDatabaseService(JDBCClient dbClient) {
    return new TeaDatabaseServiceImpl(dbClient);
  }

  @GenIgnore
  static TeaDatabaseService createTeaDatabaseProxy(Vertx vertx, String address) {
    return new TeaDatabaseServiceVertxEBProxy(vertx, address);
  }

  @Fluent
  TeaDatabaseService addTea(Tea tea, Handler<AsyncResult<Tea>> resultHandler);
}
