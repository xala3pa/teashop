package com.xala3pa.teashop.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DataObject(generateConverter = true)
public class Tea {

  private Integer ID;
  private String name;
  private TeaType teaType;
  private Integer infuse_time;

  // Mandatory for data objects
  public Tea(JsonObject jsonObject) {
    TeaConverter.fromJson(jsonObject, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    TeaConverter.toJson(this, json);
    return json;
  }
}
