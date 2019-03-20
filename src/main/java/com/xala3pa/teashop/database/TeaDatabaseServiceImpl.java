package com.xala3pa.teashop.database;

import com.xala3pa.teashop.domain.Tea;
import com.xala3pa.teashop.domain.TeaType;
import com.xala3pa.teashop.domain.exceptions.TeaNotFoundException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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

  @Override
  public TeaDatabaseService findTeasByID(String teaID, Handler<AsyncResult<JsonObject>> resultHandler) {
    String sql = "SELECT * FROM TEA WHERE ID = ?";

    JsonArray data = new JsonArray().add(teaID);

    dbClient.queryWithParams(sql, data, res -> {
      if (res.succeeded()) {
        if (res.result().getResults().isEmpty()) {
          LOGGER.error("TeaDatabaseServiceImpl :: findTeasByID - No results", res.cause());
          resultHandler.handle(Future.failedFuture(new TeaNotFoundException("No Teas found by ID : " + teaID)));
        } else {
          LOGGER.info("TeaDatabaseServiceImpl :: findTeasByID - Getting Tea by ID");
          resultHandler.handle(Future.succeededFuture(res.result().getRows().get(0)));
        }
      } else {
        LOGGER.error("TeaDatabaseServiceImpl :: findTeasByID - Database Error: Getting Tea by ID", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
    return this;
  }

  @Override
  public TeaDatabaseService deleteTeaByID(String teaID, Handler<AsyncResult<Void>> resultHandler) {
    String sql = "DELETE FROM TEA WHERE ID = ?";

    JsonArray data = new JsonArray().add(teaID);

    dbClient.updateWithParams(sql, data, res -> {
      if (res.succeeded()) {
        LOGGER.info("TeaDatabaseServiceImpl :: deleteTeaByID - Deleting tea by ID: {}", teaID);
        resultHandler.handle(Future.succeededFuture());
      } else {
        LOGGER.error("TeaDatabaseServiceImpl :: deleteTeaByID - Database Error: Deleting Tea by ID", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
    return this;
  }

  @Override
  public TeaDatabaseService updateTea(Tea tea, Handler<AsyncResult<Tea>> resultHandler) {
    String updateSql = "UPDATE TEA SET NAME=?, TYPE=?, INFUSE_TIME=? WHERE ID = ?";
    String findSql = "SELECT * FROM TEA WHERE ID = ?";

    JsonArray findData = new JsonArray().add(tea.getID());

    dbClient.queryWithParams(findSql, findData, res -> {
      if (res.succeeded()) {
        if (res.result().getResults().isEmpty()) {
          LOGGER.error("TeaDatabaseServiceImpl :: updateTea - No results", res.cause());
          resultHandler.handle(Future.failedFuture(new TeaNotFoundException("No Teas found by ID : " + tea.getID())));
        } else {
          JsonArray updateData = new JsonArray()
            .add(tea.getName())
            .add(tea.getTeaType())
            .add(tea.getInfuse_time())
            .add(tea.getID());

          dbClient.updateWithParams(updateSql, updateData, result -> {
            if (result.succeeded()) {
              LOGGER.info("TeaDatabaseServiceImpl :: updateTea - Updating tea with ID: {}", tea.getID());
              resultHandler.handle(Future.succeededFuture(tea));
            } else {
              LOGGER.error("TeaDatabaseServiceImpl :: updateTea - Database Error: Updating Tea", result.cause());
              resultHandler.handle(Future.failedFuture(result.cause()));
            }
          });
        }
      } else {
        LOGGER.error("TeaDatabaseServiceImpl :: updateTea - Database Error: Getting Tea by ID", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
    return this;
  }
}
