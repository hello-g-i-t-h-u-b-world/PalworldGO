package com.game.handler;

import com.game.DatabaseManager;

import com.google.gson.Gson;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.sql.*;
import java.util.*;

public class PokedexHandler implements HttpHandler {

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) {
        try {

            String query = exchange.getRequestURI().getQuery();

            int userId = Integer.parseInt(query.split("=")[1]);

            Connection conn = DatabaseManager.connect();

            PreparedStatement ps =
                    conn.prepareStatement(
                            "SELECT * FROM pokedex WHERE user_id=?"
                    );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();

            while(rs.next()) {

                Map<String, Object> item = new HashMap<>();

                item.put("name", rs.getString("monster_name"));

                list.add(item);
            }

            String json = gson.toJson(list);

            exchange.getResponseHeaders().add(
                    "Access-Control-Allow-Origin",
                    "*"
            );

            exchange.sendResponseHeaders(200, json.getBytes().length);

            OutputStream os = exchange.getResponseBody();

            os.write(json.getBytes());
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
