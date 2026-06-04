package com.game;

import com.game.controller.SpawnController;
import com.game.handler.CaptureGameHandler;
import com.game.handler.LoginHandler;
import com.game.handler.RegisterHandler;
import com.game.handler.PokedexHandler;
import com.game.handler.InventoryHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/spawn", new SpawnController());
        server.createContext("/capture-game", new CaptureGameHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/pokedex", new PokedexHandler());
        server.createContext("/inventory", new InventoryHandler());
        server.setExecutor(null);

        System.out.println("Server started on port 8080");
        server.start();
    }
}