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

  TeaDatabaseServiceImpl(JDBCClient dbClient) {
    this.dbClient = dbClient;
  }

  @Override
  public TeaDatabaseService addTea(Tea tea, Handler<AsyncResult<Void>> resultHandler) {
    String sql = "INSERT INTO TEA (id, name, type, infuse_time) VALUES ?, ?, ?, ?";

    JsonArray data = new JsonArray().
      add(tea.getID()).
      add(tea.getName()).
      add(tea.getTeaType()).
      add(tea.getInfuse_time());

    dbClient.updateWithParams(sql, data, res -> {
      if (res.succeeded()) {
        LOGGER.info("TeaDatabaseServiceImpl :: addTea - Adding new tea: {}",tea);
        resultHandler.handle(Future.succeededFuture());
      } else {
        LOGGER.error("TeaDatabaseServiceImpl :: addTea - Database Error: Inserting new Tea", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
    return this;
  }
}
