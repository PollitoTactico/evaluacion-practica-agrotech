package com.agrotech;

import org.apache.camel.Header;

public class ServicioAnalitica {
    public String getUltimoValor(@Header("id_sensor") String id) {
        try {
            return DBHelper.getUltimoPorSensor(id);
        } catch (Exception e) {
            return String.format("{\"id\":\"%s\",\"error\":\"%s\"}", id, e.getMessage());
        }
    }
}
