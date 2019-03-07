package com.xala3pa.teashop.database;

import com.xala3pa.teashop.domain.Tea;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TeaDatabaseServiceImpl implements TeaDatabaseService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TeaDatabaseServiceImpl.class);

  private final JDBCClient dbClient;

  TeaDatabaseServiceImpl(JDBCClient dbClient, Handler<AsyncResult<TeaDatabaseService>> readyHandler) {
    this.dbClient = dbClient;
  }

  @Override
  public TeaDatabaseService addTea(Tea tea, Handler<AsyncResult<Void>> resultHandler) {
    String sql = "INSERT INTO TEA (id, name, type, infusion_time) VALUES ?, ?, ?, ?";

    dbClient.updateWithParams(sql, new JsonArray().add(tea.toJson()), res -> {
      if (res.succeeded()) {
        resultHandler.handle(Future.succeededFuture());
      } else {
        LOGGER.error("Database Error: Inserting new Tea", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
    return this;
  }
}
