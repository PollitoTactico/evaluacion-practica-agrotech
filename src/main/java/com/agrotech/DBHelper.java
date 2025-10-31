package com.agrotech;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.File;

public class DBHelper {
    private static final String DB_DIR = "database";
    private static final String DB_FILE = "database/lecturas.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;

    public static void initDB() throws Exception {
        File dir = new File(DB_DIR);
        if (!dir.exists()) dir.mkdirs();
        // create the DB and the table if not exists
        try (Connection conn = DriverManager.getConnection(URL)) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS lecturas (id_sensor VARCHAR(10), fecha TEXT, humedad DOUBLE, temperatura DOUBLE);");
            }
        }
    }

    public static void insertLectura(String id_sensor, String fecha, double humedad, double temperatura) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL)) {
            String sql = "INSERT INTO lecturas(id_sensor, fecha, humedad, temperatura) VALUES (?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id_sensor);
                ps.setString(2, fecha);
                ps.setDouble(3, humedad);
                ps.setDouble(4, temperatura);
                ps.executeUpdate();
            }
        }
    }

    public static String getUltimoPorSensor(String id_sensor) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL)) {
            String sql = "SELECT id_sensor, fecha, humedad, temperatura FROM lecturas WHERE id_sensor = ? ORDER BY fecha DESC LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id_sensor);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String id = rs.getString("id_sensor");
                        String fecha = rs.getString("fecha");
                        double humedad = rs.getDouble("humedad");
                        double temperatura = rs.getDouble("temperatura");
                        return String.format("{\"id\":\"%s\",\"humedad\":%s,\"temperatura\":%s,\"fecha\":\"%s\"}", id, humedad, temperatura, fecha);
                    } else {
                        return String.format("{\"id\":\"%s\",\"error\":\"not found\"}", id_sensor);
                    }
                }
            }
        }
    }
}
