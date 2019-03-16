package com.xala3pa.teashop.database;

import com.xala3pa.teashop.domain.Tea;
import com.xala3pa.teashop.domain.TeaType;
import com.xala3pa.teashop.domain.exceptions.TeaNotFoundException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;


public class TeaDatabaseServiceImpl implements TeaDatabaseService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TeaDatabaseServiceImpl.class);
  private static final int TEA_NAME = 1;

  private final JDBCClient dbClient;

  TeaDatabaseServiceImpl(JDBCClient dbClient) {
    this.dbClient = dbClient;
  }

  @Override
  public TeaDatabaseService addTea(Tea tea, Handler<AsyncResult<Tea>> resultHandler) {
    String sql = "INSERT INTO TEA (id, name, type, infuse_time) VALUES ?, ?, ?, ?";

    JsonArray data = new JsonArray().
      add(tea.getID()).
      add(tea.getName()).
      add(tea.getTeaType()).
      add(tea.getInfuse_time());

    dbClient.updateWithParams(sql, data, res -> {
      if (res.succeeded()) {
        LOGGER.info("TeaDatabaseServiceImpl :: addTea - Adding new tea: {}", tea);
        resultHandler.handle(Future.succeededFuture(tea));
      } else {
        LOGGER.error("TeaDatabaseServiceImpl :: addTea - Database Error: Inserting new Tea", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
    return this;
  }

  @Override
  public TeaDatabaseService findAllTeas(Handler<AsyncResult<JsonArray>> resultHandler) {
    String sql = "SELECT * FROM TEA";

    dbClient.query(sql, res -> {
      if (res.succeeded()) {
        JsonArray teas = new JsonArray(res.result()
          .getResults()
          .stream()
          .map(json -> json.getString(TEA_NAME))
          .sorted()
          .collect(Collectors.toList()));
        LOGGER.info("TeaDatabaseServiceImpl :: findAllTeas - Getting list of all teas");
        resultHandler.handle(Future.succeededFuture(teas));
      } else {
        LOGGER.error("TeaDatabaseServiceImpl :: findAllTeas - Database Error: Getting all Teas", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
    return this;
  }

  @Override
  public TeaDatabaseService findTeasByType(TeaType teaType, Handler<AsyncResult<JsonArray>> resultHandler) {
    String sql = "SELECT * FROM TEA WHERE TYPE = ?";

    JsonArray data = new JsonArray().add(teaType);

    dbClient.queryWithParams(sql, data, res -> {
      if (res.succeeded()) {
        if (res.result().getResults().isEmpty()) {
          LOGGER.error("TeaDatabaseServiceImpl :: findTeasByType - No results", res.cause());
          resultHandler.handle(Future.failedFuture(new TeaNotFoundException("No Teas found by type : " + teaType)));
        } else {
          JsonArray teas = new JsonArray(res.result()
            .getResults()
            .stream()
            .map(json -> json.getString(TEA_NAME))
            .sorted()
            .collect(Collectors.toList()));
          LOGGER.info("TeaDatabaseServiceImpl :: findTeasByType - Getting list of all teas by Type");
          resultHandler.handle(Future.succeededFuture(teas));
        }
      } else {
        LOGGER.error("TeaDatabaseServiceImpl :: findTeasByType - Database Error: Getting teas by type", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
    return this;
  }
}
