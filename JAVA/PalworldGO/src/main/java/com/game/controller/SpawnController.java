package com.game.controller;

import com.game.model.Monster;
import com.game.service.SpawnService;

import com.google.gson.Gson;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SpawnController implements HttpHandler {
    private final Gson gson = new Gson();

    // 현재 맵 몬스터
    private static final List<Monster> monsters = new ArrayList<>();

    // 마지막 스폰 시간
    private static long lastSpawnTime = System.currentTimeMillis();

    // 최대 몬스터 수
    private static final int MAX_MONSTERS = 5;

    private static final Random random = new Random();

    // 다음 스폰 시간
    private static long nextSpawnDelay = randomDelay();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();

            double lat = 37.5;
            double lng = 126.9;

            if(query != null) {
                String[] params = query.split("&");

                lat = Double.parseDouble(params[0].split("=")[1]);

                lng = Double.parseDouble(params[1].split("=")[1]);
            }

            long now = System.currentTimeMillis();

            // 5~10초마다 스폰
            if(now - lastSpawnTime >= nextSpawnDelay) {

                // 최대 5마리 제한
                if(monsters.size() < MAX_MONSTERS) {
                    Monster monster = SpawnService.generateMonster(lat, lng);

                    monsters.add(monster);

                    System.out.println("몬스터 생성: " + monster.name);
                }

                lastSpawnTime = now;

                nextSpawnDelay = randomDelay();
            }

            // 응답 데이터
            Map<String, Object> response = new HashMap<>();

            response.put("weather", SpawnService.currentWeather);

            response.put("monsters", monsters);

            String json = gson.toJson(response);

            exchange
                    .getResponseHeaders()
                    .add(
                            "Access-Control-Allow-Origin",
                            "*"
                    );

            exchange
                    .getResponseHeaders()
                    .add(
                            "Content-Type",
                            "application/json"
                    );

            exchange.sendResponseHeaders(
                    200,
                    json.getBytes().length
            );

            OutputStream os =
                    exchange.getResponseBody();

            os.write(json.getBytes());

            os.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // 랜덤 스폰 시간
    private static long randomDelay() {
        return 5000 + random.nextInt(5000);
    }

    // spawnId 기반 제거
    public static void removeMonster(int spawnId) {
        monsters.removeIf(
                monster ->
                        monster.spawnId == spawnId
        );

        System.out.println("몬스터 제거 완료: " + spawnId);
    }
}