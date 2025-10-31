package com.agrotech;

import org.apache.camel.main.Main;

public class MainApp {
    public static void main(String[] args) throws Exception {
        // ensure DB folder and table exist
        DBHelper.initDB();

        Main main = new Main();
        main.addRouteBuilder(new Routes());
        System.out.println("Starting Camel (MainApp). Press Ctrl+C to stop.");
        main.run();
    }
}
