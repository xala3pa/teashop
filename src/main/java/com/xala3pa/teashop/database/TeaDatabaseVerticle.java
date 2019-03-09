package com.xala3pa.teashop.database;

import com.xala3pa.teashop.domain.Tea;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeaDatabaseVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(TeaDatabaseVerticle.class);

  private static final String CONFIG_TEADB_JDBC_URL = "teadb.jdbc.url";
  private static final String CONFIG_TEADB_JDBC_DRIVER_CLASS = "teadb.jdbc.driver_class";
  private static final String CONFIG_TEADB_JDBC_MAX_POOL_SIZE = "teadb.jdbc.max_pool_size";
  private static final String SQL_CREATE_PAGES_TABLE = "CREATE TABLE IF NOT EXISTS TEA (ID INTEGER, NAME VARCHAR(25), TYPE VARCHAR(25),INFUSE_TIME INTEGER)";
  private static final String CONFIG_TEADB_QUEUE = "teadb.queue";

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    JDBCClient dbClient = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", config().getString(CONFIG_TEADB_JDBC_URL, "jdbc:hsqldb:file:db/teashop"))
      .put("driver_class", config().getString(CONFIG_TEADB_JDBC_DRIVER_CLASS, "org.hsqldb.jdbcDriver"))
      .put("max_pool_size", config().getInteger(CONFIG_TEADB_JDBC_MAX_POOL_SIZE, 30)));

    TeaDatabaseService teaDatabaseService = TeaDatabaseService.createTeaDatabaseService(dbClient);
    ServiceBinder binder = new ServiceBinder(vertx);
    binder.setAddress(CONFIG_TEADB_QUEUE).register(TeaDatabaseService.class, teaDatabaseService);

    dbClient.getConnection(ar -> {
      if (ar.failed()) {
        LOGGER.error("TeaDatabaseVerticle :: Could not open a database connection", ar.cause());
        startFuture.fail(ar.cause());
      } else {
        SQLConnection connection = ar.result();
        connection.execute(SQL_CREATE_PAGES_TABLE, create -> {
          connection.close();
          if (create.failed()) {
            LOGGER.error("TeaDatabaseVerticle :: Database preparation error", create.cause());
            startFuture.fail(create.cause());
          } else {
            LOGGER.info("TeaDatabaseVerticle :: 2Initialization Database done");
            startFuture.complete();
          }
        });
      }
    });
  }
}
