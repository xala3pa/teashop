package com.xala3pa.teashop.config;

import com.xala3pa.teashop.database.TeaDatabaseService;
import com.xala3pa.teashop.http.TeaController;

public class VertxConfig {
  private TeaDatabaseService teaDatabaseService;

  public VertxConfig(TeaDatabaseService teaDatabaseService) {
    this.teaDatabaseService = teaDatabaseService;
  }

  public  TeaController getTeaController() {
    return  new TeaController(teaDatabaseService);
  }
}
