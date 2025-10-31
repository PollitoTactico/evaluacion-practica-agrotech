package com.agrotech;

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.CamelContext;

public class MainApp {
    public static void main(String[] args) throws Exception {
        // ensure DB folder and table exist
        DBHelper.initDB();

        CamelContext context = new DefaultCamelContext();
        // add routes
        context.addRoutes(new Routes());

        // add shutdown hook to stop Camel cleanly
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutting down Camel...");
                context.stop();
            } catch (Exception e) {
                // ignore
            }
        }));

        System.out.println("Starting Camel (MainApp). Press Ctrl+C to stop.");
        context.start();

        // keep main thread alive
        Thread.currentThread().join();
    }
}
