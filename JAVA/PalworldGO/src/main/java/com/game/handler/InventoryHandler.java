package com.game.handler;

import com.game.DatabaseManager;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryHandler implements HttpHandler {

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) {

        try {

            String query = exchange.getRequestURI().getQuery();

            if(query == null || !query.contains("=")) {

                sendErrorResponse(
                        exchange,
                        400,
                        "userId가 없습니다."
                );

                return;
            }

            int userId = Integer.parseInt(query.split("=")[1]);

            List<Map<String,Object>> list = new ArrayList<>();

            try(
                    Connection conn = DatabaseManager.connect();

                    PreparedStatement ps = conn.prepareStatement(
                            "SELECT * FROM inventory WHERE user_id=? ORDER BY captured_at DESC"
                    )
            ) {

                ps.setInt(1, userId);

                ResultSet rs = ps.executeQuery();

                while(rs.next()) {

                    Map<String,Object> item = new HashMap<>();

                    item.put("name", rs.getString("monster_name"));

                    item.put("type", rs.getString("type"));

                    item.put("time", rs.getString("captured_at"));

                    list.add(item);
                }

                rs.close();
            }

            String json = gson.toJson(list);

            exchange.getResponseHeaders().add(
                    "Access-Control-Allow-Origin",
                    "*"
            );

            exchange.getResponseHeaders().add(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(
                    200,
                    bytes.length
            );

            try(
                    OutputStream os = exchange.getResponseBody()
            ) {
                os.write(bytes);
            }

        } catch(Exception e) {

            e.printStackTrace();

            try {

                sendErrorResponse(
                        exchange,
                        500,
                        "서버 내부 오류"
                );

            } catch(Exception ignored) {}
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws Exception {

        Map<String,String> error = new HashMap<>();

        error.put("error", message);

        String json = gson.toJson(error);

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().add(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(statusCode, bytes.length);

        try(
                OutputStream os = exchange.getResponseBody()
        ) {
            os.write(bytes);
        }
    }
}