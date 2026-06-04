package com.game.service;

import com.game.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PokedexService {

    /**
     * 유저의 도감 기록을 확인하고, 처음 포획한 신규 몬스터일 경우에만 도감 테이블에 추가합니다.
     */
    public static void checkAndRegisterPokedex(Connection conn, int userId, String monsterName) {
        try {
            // 1. 도감(pokedex) 중복 여부 체크
            String sqlCheck = "SELECT * FROM pokedex WHERE user_id=? AND monster_name=?";
            boolean alreadyDiscovered = false;

            try (PreparedStatement check = conn.prepareStatement(sqlCheck)) {
                check.setInt(1, userId);
                check.setString(2, monsterName);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        alreadyDiscovered = true;
                    }
                }
            }

            // 2. 발견 기록이 없을 때만 새롭게 도감 추가
            if (!alreadyDiscovered) {
                String sqlPokedex = "INSERT INTO pokedex (user_id, monster_name) VALUES (?, ?)";
                try (PreparedStatement pokedex = conn.prepareStatement(sqlPokedex)) {
                    pokedex.setInt(1, userId);
                    pokedex.setString(2, monsterName);
                    pokedex.executeUpdate();
                    System.out.println("[도감 등록] 신규 몬스터 발견! 도감 등록 완료: " + monsterName);
                }
            } else {
                System.out.println("[도감 유지] 이미 도감에 등록된 몬스터입니다: " + monsterName);
            }

        } catch (Exception e) {
            System.err.println("PokedexService 도감 처리 중 에러 발생:");
            e.printStackTrace();
        }
    }
}