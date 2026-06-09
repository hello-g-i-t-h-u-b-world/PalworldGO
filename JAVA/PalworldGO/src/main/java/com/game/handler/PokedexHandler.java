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

            PreparedStatement ps = conn.prepareStatement(
                            """
                            SELECT
                                p.monster_name,
                                p.discovered_at,
                                m.type
                            FROM pokedex p
                            JOIN monsters m
                                ON p.monster_name = m.name
                            WHERE p.user_id = ?
                            ORDER BY p.discovered_at ASC
                            """
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            List<Map<String,Object>> list = new ArrayList<>();

            while(rs.next()) {

                Map<String,Object> item = new HashMap<>();

                item.put("name", rs.getString("monster_name"));

                item.put("type", rs.getString("type"));

                item.put("discoveredAt", rs.getTimestamp("discovered_at").toString());

                list.add(item);
            }

            rs.close();
            ps.close();
            conn.close();

            String json = gson.toJson(list);

            exchange.getResponseHeaders().add(
                    "Access-Control-Allow-Origin",
                    "*"
            );

            exchange.getResponseHeaders().add(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            exchange.sendResponseHeaders(
                    200,
                    json.getBytes("UTF-8").length
            );

            try(OutputStream os = exchange.getResponseBody()) {

                os.write(
                        json.getBytes("UTF-8")
                );
            }

        } catch(Exception e) {

            e.printStackTrace();
        }
    }
}