package com.game.handler;

import com.game.DatabaseManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterHandler implements HttpHandler {

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Methods",
                "GET, POST, OPTIONS"
        );

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Headers",
                "Content-Type"
        );

        if(exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(200, -1);

            return;
        }

        try {

            JsonObject body = gson.fromJson(
                    new InputStreamReader(
                            exchange.getRequestBody(),
                            StandardCharsets.UTF_8
                    ),
                    JsonObject.class
            );

            String username = body.get("username").getAsString();

            String password = body.get("password").getAsString();

            Connection conn = DatabaseManager.connect();

            PreparedStatement ps =
                    conn.prepareStatement(
                            "INSERT INTO users(username, password) VALUES (?, ?)"
                    );

            ps.setString(1, username);
            ps.setString(2, password);

            ps.executeUpdate();

            JsonObject result = new JsonObject();

            result.addProperty("success", true);

            byte[] bytes =
                    gson.toJson(result)
                            .getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();

            os.write(bytes);
            os.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}