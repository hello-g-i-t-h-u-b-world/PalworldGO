package com.game.service;

import com.game.DatabaseManager;
import com.game.model.Monster;
import com.game.util.LocationUtil;
import com.game.util.BoostedTypeManager; // 💡 새로 만든 독립 클래스 임포트

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnService {
    private static final Random random = new Random();

    // 현재 날씨 저장
    public static String currentWeather = "";

    // 스폰 ID
    private static int nextSpawnId = 1;

    public static Monster generateMonster(double userLat, double userLng) {
        // 현재 날씨 가져오기
        String weather = WeatherService.getWeather(userLat, userLng);

        // 현재 날씨 저장
        currentWeather = weather;

        // 날씨 부스트 타입 호출
        String boostedType = BoostedTypeManager.getBoostedType(weather);

        List<Monster> boostedMonsters = new ArrayList<>();
        List<Monster> normalMonsters = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM monsters");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                Monster monster = new Monster();
                monster.id = rs.getInt("id");
                monster.name = rs.getString("name");
                monster.type = rs.getString("type");

                // 날씨 강화 타입 분리
                if(monster.type.equals(boostedType)) {
                    boostedMonsters.add(monster);
                } else {
                    normalMonsters.add(monster);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Monster selectedMonster;
        int chance = random.nextInt(100);

        // 50% 확률 강화 타입 선택
        if(chance < 50 && !boostedMonsters.isEmpty()) {
            selectedMonster = boostedMonsters.get(random.nextInt(boostedMonsters.size()));
        } else {
            selectedMonster = normalMonsters.get(random.nextInt(normalMonsters.size()));
        }

        // =========================
        // 스폰 개체 고유 ID 부여
        // =========================
        selectedMonster.spawnId = nextSpawnId++;

        // 랜덤 위치 생성
        double[] pos = LocationUtil.getRandomPosition(userLat, userLng, 100);
        selectedMonster.lat = pos[0];
        selectedMonster.lng = pos[1];

        System.out.println("현재 날씨: " + weather);
        System.out.println("확률 증가 타입: " + boostedType);
        System.out.println("스폰 몬스터: " + selectedMonster.name);
        System.out.println("spawnId: " + selectedMonster.spawnId);

        return selectedMonster;
    }
}