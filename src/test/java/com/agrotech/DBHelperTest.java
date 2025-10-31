package com.agrotech;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class DBHelperTest {

    @BeforeAll
    public static void setup() throws Exception {
        DBHelper.initDB();
        // clean table
        try (var conn = java.sql.DriverManager.getConnection("jdbc:sqlite:database/lecturas.db");
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM lecturas;");
        }
    }

    @Test
    public void testInsertAndGetUltimo() throws Exception {
        DBHelper.insertLectura("T001","2025-10-01",10.5,20.0);
        DBHelper.insertLectura("T001","2025-10-02",11.5,21.0);
        String res = DBHelper.getUltimoPorSensor("T001");
        assertNotNull(res);
        assertTrue(res.contains("\"id\":\"T001\""));
        assertTrue(res.contains("2025-10-02"));
    }

    @Test
    public void testServicioAnalitica() throws Exception {
        // If sensor not found, service should return JSON with id and error or id
        ServicioAnalitica s = new ServicioAnalitica();
        String resp = s.getUltimoValor("NONEXISTENT");
        assertNotNull(resp);
        assertTrue(resp.contains("\"id\":\"NONEXISTENT\""));
    }
}
