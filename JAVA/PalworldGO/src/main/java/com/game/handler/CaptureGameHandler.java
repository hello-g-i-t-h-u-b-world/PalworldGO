package com.game.handler;

import com.game.controller.SpawnController;
import com.game.service.CaptureService;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CaptureGameHandler implements HttpHandler {

    private final Gson gson = new Gson();
    private final CaptureService captureService = new CaptureService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // CORS 설정 및 OPTIONS 브라우저 프리플라이트 대응 요청 처리
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        try {
            JsonObject body = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    JsonObject.class
            );

            if (body == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            // 디버깅 콘솔 기록 출력
            System.out.println("[서버 수신 데이터 원본] " + body.toString());

            int userId = body.get("userId").getAsInt();

            int spawnId = body.get("spawnId").getAsInt();

            String monsterName = body.get("monsterName").getAsString();

            String type = body.get("type").getAsString();

            int clickCount = body.get("clickCount").getAsInt();

            // 필수 식별 파라미터 유효성 기본 검증
            if (userId == 0 || monsterName.isEmpty()) {
                System.out.println("[검증 실패] 필수 요청 데이터 유실 - userId: " + userId + ", monsterName: " + monsterName);
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            // 독립된 비즈니스 서비스 레이어로직 위임 실행
            JsonObject captureResult = captureService.calculateAndCapture(userId, monsterName, type, clickCount);
            boolean success = captureResult.get("success").getAsBoolean();

            // 인스턴스 스폰 배열 매핑 동기화 해제 (필드 몬스터 마커 제거 유도)
            SpawnController.removeMonster(spawnId);

            // 프론트엔드가 요구하는 JSON 규격 가공 반환
            JsonObject result = new JsonObject();
            result.addProperty("success", success);
            result.addProperty("rate", captureResult.get("rate").getAsInt());

            String json = gson.toJson(result);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
                os.flush();
            }

            System.out.println(monsterName + " 포획 판정 최종 프로세스 완료. 결과: " + success);

        } catch (Exception e) {
            System.err.println("[오류 발생] CaptureGameHandler 서블릿 연산 실패:");
            e.printStackTrace();

            // 서버 내부 오류(500) 발생 시 프론트엔드가 정상 파싱할 수 있도록 JSON 문자열 포맷 구조로 응답 보장
            String errorJson = "{\"error\":\"Internal Server Error\",\"success\":false,\"rate\":0}";
            byte[] bytes = errorJson.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(500, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
                os.flush();
            }
        }
    }
}