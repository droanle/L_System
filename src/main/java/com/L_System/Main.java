package com.L_System;

import java.io.IOException;

import com.L_System.Controllers.Teste;
import com.L_System.L_API.Server;

/**
 * Hello world!
 *
 */
public class Main {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        Server App = new Server(PORT);

        App.addControllers(Teste.class);

        App.start();

        // App.stopAfterDelay(30000); // 30 segundos
    }
}
