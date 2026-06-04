package com.game.handler;

import com.game.DatabaseManager;
import com.game.model.User;
import com.game.util.UserSessionManager;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.sql.*;
import java.util.*;

public class InventoryHandler implements HttpHandler {

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.contains("=")) {
                sendErrorResponse(exchange, 400, "잘못된 요청 양식입니다.");
                return;
            }

            // 클라이언트가 보낸 유저 고유 ID 식별
            int userId = Integer.parseInt(query.split("=")[1]);

            // 💡 [변경 핵심] DB에 매번 연결하는 대신, 로그인 시 저장된 User 캐시 객체를 즉시 가져옴
            User currentUser = UserSessionManager.getUserSession(userId);

            if (currentUser == null) {
                // 로그인 세션이 끊겼거나 비정상적인 접근일 경우 예외 처리
                System.out.println("[경고] 세션에 존재하지 않는 유저 ID 접근 시도: " + userId);
                sendErrorResponse(exchange, 401, "로그인 정보가 없거나 만료되었습니다. 다시 로그인해주세요.");
                return;
            }

            // 몬스터 보유 인벤토리 리스트는 가변적이므로 기존대로 매번 최신화하여 가져옴
            Connection conn = DatabaseManager.connect();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM inventory WHERE user_id=?"
            );
            ps.setInt(1, currentUser.id); // 캐싱된 User 객체의 안전한 고유 ID 사용

            ResultSet rs = ps.executeQuery();
            List<Map<String, Object>> list = new ArrayList<>();

            while(rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", rs.getString("monster_name"));
                item.put("type", rs.getString("type"));
                list.add(item);
            }

            rs.close();
            ps.close();
            conn.close(); // DB 리소스 자원 반환

            // 응답 데이터에 인벤토리 리스트뿐만 아니라, 캐싱된 유저 정보(레벨, 골드 등)를 유기적으로 결합 가능
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userInfo", currentUser); // 로그인 시 저장해 둔 레벨, 골드 정보 포함
            responseData.put("inventory", list);

            String json = gson.toJson(responseData);

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, json.getBytes("UTF-8").length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(json.getBytes("UTF-8"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            try { sendErrorResponse(exchange, 500, "서버 내부 오류가 발생했습니다."); } catch(Exception ignored) {}
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws Exception {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", message);
        String errorJson = gson.toJson(errorMap);

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, errorJson.getBytes("UTF-8").length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(errorJson.getBytes("UTF-8"));
        }
    }
}

//package com.game.handler;
//
//import com.game.DatabaseManager;
//
//import com.google.gson.Gson;
//
//import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpHandler;
//
//import java.io.OutputStream;
//import java.sql.*;
//import java.util.*;
//
//public class InventoryHandler implements HttpHandler {
//
//    private final Gson gson = new Gson();
//
//    @Override
//    public void handle(HttpExchange exchange) {
//
//        try {
//
//            String query = exchange.getRequestURI().getQuery();
//            // 유저 id값 빼오는 문자열 파싱 공식
//            int userId = Integer.parseInt(query.split("=")[1]);
//
//            Connection conn = DatabaseManager.connect();
//
//            PreparedStatement ps =
//                    conn.prepareStatement(
//                            "SELECT * FROM inventory WHERE user_id=?"
//                    );
//
//            ps.setInt(1, userId);
//
//            ResultSet rs =
//                    ps.executeQuery();
//
//            List<Map<String, Object>> list =
//                    new ArrayList<>();
//
//            while(rs.next()) {
//
//                Map<String, Object> item =
//                        new HashMap<>();
//
//                item.put(
//                        "name",
//                        rs.getString("monster_name")
//                );
//
//                item.put(
//                        "type",
//                        rs.getString("monster_type")
//                );
//
//                list.add(item);
//            }
//
//            String json =
//                    gson.toJson(list);
//
//            exchange.getResponseHeaders().add(
//                    "Access-Control-Allow-Origin",
//                    "*"
//            );
//
//            exchange.sendResponseHeaders(
//                    200,
//                    json.getBytes().length
//            );
//
//            OutputStream os =
//                    exchange.getResponseBody();
//
//            os.write(json.getBytes());
//            os.close();
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//        }
//    }
//}
