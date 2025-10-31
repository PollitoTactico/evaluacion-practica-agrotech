package com.agrotech;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class Routes extends RouteBuilder {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void configure() throws Exception {
        // File Transfer: read sensores.csv from project root, move to processed, convert to JSON and forward
        from("file:./?fileName=sensores.csv&move=processed/${file:name}")
            .routeId("file-transfer")
            .log("[FILE] Found sensores.csv, processing...")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                if (body == null) return;
                String[] lines = body.split("\\r?\\n");
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (line.isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length < 4) continue;
                    Map<String, Object> m = new HashMap<>();
                    m.put("id_sensor", parts[0]);
                    m.put("fecha", parts[1]);
                    m.put("humedad", Double.parseDouble(parts[2]));
                    m.put("temperatura", Double.parseDouble(parts[3]));
                    String json = mapper.writeValueAsString(m);
                    // send to AgroAnalyzer route
                    exchange.getContext().createProducerTemplate().sendBody("direct:agroAnalyzerIn", json);
                    // also log the JSON
                    exchange.getContext().createProducerTemplate().sendBody("log:file-json", "[FILE->JSON] " + json);
                }
            })
            .log("[FILE] sensores.csv processed and moved to processed/");

        // AgroAnalyzer: receive JSON, parse and insert into DB
        from("direct:agroAnalyzerIn")
            .routeId("agro-analyzer")
            .log("[AGRO] Received: ${body}")
            .process(exchange -> {
                String json = exchange.getIn().getBody(String.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> m = mapper.readValue(json, Map.class);
                String id = (String) m.get("id_sensor");
                String fecha = (String) m.get("fecha");
                double humedad = Double.parseDouble(m.get("humedad").toString());
                double temperatura = Double.parseDouble(m.get("temperatura").toString());
                DBHelper.insertLectura(id, fecha, humedad, temperatura);
            })
            .log("[AGRO] Inserted lectura into DB");

        // RPC server: respond to getUltimoValor
        // HTTP endpoint to request last reading via HTTP -> direct:solicitarLectura
        from("undertow:http://0.0.0.0:8080/rpc/ultimo/{id}")
            .routeId("http-rpc")
            .log("[HTTP] Request for sensor ${header.id}")
            .setBody(simple("${header.id}"))
            .to("direct:solicitarLectura")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"));

        
        from("direct:rpc.obtenerUltimo")
            .routeId("rpc-servidor")
            .log("[SERVIDOR] Solicitud recibida para sensor ${header.id_sensor}")
            .bean(ServicioAnalitica.class, "getUltimoValor");

        // RPC client: request last value
        from("direct:solicitarLectura")
            .routeId("rpc-cliente")
            .log("[CLIENTE] Solicitando lectura del sensor ${body}")
            .process(exchange -> {
                String id = exchange.getIn().getBody(String.class);
                // synchronous call to the server route (direct)
                String resp = exchange.getContext()
                    .createProducerTemplate()
                    .requestBodyAndHeader("direct:rpc.obtenerUltimo", null, "id_sensor", id, String.class);
                exchange.getIn().setBody(resp);
            })
            .log("[CLIENTE] Respuesta recibida: ${body}");
    }
}
