package com.game.handler;

import com.game.DatabaseManager;
import com.game.model.User;
import com.game.util.UserSessionManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginHandler implements HttpHandler {

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // CORS 허용
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

        // OPTIONS 처리
        if(exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
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

            if(body == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            String username = body.get("username").getAsString();
            String password = body.get("password").getAsString();

            Connection conn = DatabaseManager.connect();

            // 💡 데이터베이스 테이블명이 프로젝트 구조나 관례에 따라 다를 수 있으니 확인이 필요합니다.
            // (기존 코드의 "SELECT * FROM users" 쿼리를 유지하되, 전체 필드를 가져옵니다.)
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM users WHERE username=? AND password=?"
            );

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            JsonObject result = new JsonObject();

            if(rs.next()) {
                // 1. 데이터베이스에서 인증된 유저 정보를 가져와 User 인스턴스에 채워 넣습니다.
                User loggedInUser = new User();
                loggedInUser.id = rs.getInt("id");
                loggedInUser.username = rs.getString("username");
                loggedInUser.password = rs.getString("password");

                // 2. [핵심 요구사항] 로그인 성공 시 생성된 User 객체를 세션 스토리지에 캐싱합니다.
                UserSessionManager.registerSession(loggedInUser);

                // 3. 기존 프론트엔드 연동에 문제가 없도록 기존 응답 규격도 충실히 채워줍니다.
                result.addProperty("success", true);
                result.addProperty("userId", loggedInUser.id);
                result.addProperty("username", loggedInUser.username);
            }
            else {
                result.addProperty("success", false);
            }

            // DB 리소스 해제
            rs.close();
            ps.close();
            conn.close();

            String json = gson.toJson(result);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }

        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        }
    }
}