package com.game.service;

import com.game.DatabaseManager;
import com.game.model.monster.Monster;
import com.game.model.monster.MonsterFactory;
import com.game.model.monster.SpawnedMonster;
import com.game.model.weather.WeatherBoost;
import com.game.model.weather.WeatherFactory;
import com.game.util.LocationUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnService {

    private static final Random random = new Random();

    public static String currentWeather = "";

    private static int nextSpawnId = 1;

    public static SpawnedMonster generateMonster(double userLat, double userLng) {

        String weather = WeatherService.getWeather(userLat, userLng);

        currentWeather = weather;

        WeatherBoost weatherBoost = WeatherFactory.createWeather(weather);

        List<Monster> monsters = new ArrayList<>();

        try(Connection conn = DatabaseManager.connect()) {
            // db에서 몬스터들 불러오기
            PreparedStatement ps =
                    conn.prepareStatement(
                            "SELECT * FROM monsters");

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                Monster monster =
                        MonsterFactory.createMonster(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("type")
                        );

                monsters.add(monster);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        if(monsters.isEmpty()) {
            return null;
        }

        // 전체 가중치 계산
        int totalWeight = 0;

        for(Monster monster : monsters) {

            int weight = monster.getBaseSpawnWeight() + weatherBoost.getSpawnBonus(monster);

            totalWeight += weight;
        }

        // 랜덤 추첨
        int randomValue = random.nextInt(totalWeight);

        Monster selectedMonster = null;

        int accumulatedWeight = 0;

        for(Monster monster : monsters) {

            accumulatedWeight += monster.getBaseSpawnWeight() + weatherBoost.getSpawnBonus(monster);

            if(randomValue < accumulatedWeight) {

                selectedMonster = monster;
                break;
            }
        }

        double[] pos = LocationUtil.getRandomPosition(userLat, userLng, 100);

        SpawnedMonster spawnedMonster = new SpawnedMonster(nextSpawnId++, selectedMonster, pos[0], pos[1]);

        // 테스트 코드
        System.out.println("현재 날씨 : " + weather);
        System.out.println("스폰 몬스터 : " + selectedMonster.getName());
        System.out.println("기본 가중치 : " + selectedMonster.getBaseSpawnWeight());
        System.out.println("날씨 보너스 : " + weatherBoost.getSpawnBonus(selectedMonster));
        System.out.println("최종 가중치 : " + (selectedMonster.getBaseSpawnWeight() + weatherBoost.getSpawnBonus(selectedMonster)));
        System.out.println(selectedMonster.getClass().getSimpleName());
        System.out.println("WEATHER RAW = " + weather);
        System.out.println("BOOST CLASS = " + weatherBoost.getClass().getSimpleName());

        return spawnedMonster;
    }
}