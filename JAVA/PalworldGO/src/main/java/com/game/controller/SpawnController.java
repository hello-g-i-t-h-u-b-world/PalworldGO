package com.game.controller;

import com.game.model.monster.SpawnedMonster;
import com.game.service.SpawnService;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SpawnController implements HttpHandler {

    private final Gson gson = new Gson();

    private static final List<SpawnedMonster> monsters = new ArrayList<>();

    private static long lastSpawnTime = System.currentTimeMillis();

    private static final int MAX_MONSTERS = 5;

    private static final Random random = new Random();

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

            // 자동 스폰
            if(now - lastSpawnTime >= nextSpawnDelay) {

                if(monsters.size() < MAX_MONSTERS) {

                    SpawnedMonster spawnedMonster =
                            SpawnService.generateMonster(
                                    lat,
                                    lng
                            );

                    monsters.add(spawnedMonster);

                    System.out.println("몬스터 생성 : "
                                        + spawnedMonster
                                        .getMonster()
                                        .getName()
                    );
                }

                lastSpawnTime = now;

                nextSpawnDelay = randomDelay();
            }

            Map<String,Object> response = new HashMap<>();

            response.put("weather", SpawnService.currentWeather);

            List<Map<String,Object>> monsterList = new ArrayList<>();

            for(SpawnedMonster monster : monsters) {

                Map<String,Object> m = new HashMap<>();

                m.put("spawnId", monster.getSpawnId());

                m.put("id", monster.getMonster().getId());

                m.put("name", monster.getMonster().getName());

                m.put("type", monster.getMonster().getType());

                m.put("lat", monster.getLat());

                m.put("lng", monster.getLng());

                monsterList.add(m);
            }

            response.put("monsters", monsterList);

            String json = gson.toJson(response);

            exchange.getResponseHeaders().add(
                    "Access-Control-Allow-Origin",
                    "*"
            );

            exchange.getResponseHeaders().add(
                    "Content-Type",
                    "application/json"
            );

            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(
                    200,
                    bytes.length
            );

            try(OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static long randomDelay() {
        return 5000 + random.nextInt(5000);
    }

    public static void removeMonster(int spawnId) {
        monsters.removeIf(monster -> monster.getSpawnId() == spawnId);

        System.out.println("몬스터 제거 완료 : " + spawnId);
    }

    public static List<SpawnedMonster> getMonsters() {
        return monsters;
    }
}