-- SQL script to create table 'lecturas'
CREATE TABLE IF NOT EXISTS lecturas (
  id_sensor VARCHAR(10),
  fecha TEXT,
  humedad DOUBLE,
  temperatura DOUBLE
);
