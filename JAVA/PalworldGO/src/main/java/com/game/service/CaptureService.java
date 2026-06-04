package com.game.service;

import com.game.DatabaseManager;
import com.game.logic.CatchRateCalculator;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class CaptureService {

    /**
     * 클릭 횟수 기반으로 확률을 계산하여 포획을 수행하고, 성공 시에만 DB에 저장하는 유기적 메서드
     */
    public JsonObject calculateAndCapture(int userId, String monsterName, String type, int clickCount) {
        JsonObject result = new JsonObject();

        // 확률 계산
        CatchRateCalculator calculator = new CatchRateCalculator(clickCount);

        // 인터페이스 명세에 맞게 구현된 확률(최대 95%)을 가져옵니다.
        int successRate = calculator.calculateSuccessRate();

        // PercentProbability 인터페이스의 default 메서드인 rollDice를 호출하여 판정합니다.
        boolean isSuccess = calculator.rollDice(successRate);

        result.addProperty("success", isSuccess);
        result.addProperty("rate", successRate);
        // 인터페이스 규격 안에서 난수가 결정되므로, UI 반환이나 로그 추적용 성공/실패 여부를 확정 지어 전달합니다.

        // 2. 포획 성공(true) 시에만 데이터베이스 저장 실행!
        if (isSuccess) {
            try (Connection conn = DatabaseManager.connect()) {

                // [A] 인벤토리(inventory) 저장 전담
                String sqlInventory = "INSERT INTO inventory (user_id, monster_name, type, captured_at) VALUES (?, ?, ?, ?)";
                try (PreparedStatement inventory = conn.prepareStatement(sqlInventory)) {
                    inventory.setInt(1, userId);
                    inventory.setString(2, monsterName);
                    inventory.setString(3, type);
                    inventory.setString(4, LocalDateTime.now().toString());
                    inventory.executeUpdate();
                }

                // [B] 도감 중복 체크 및 저장 로직을 전담 클래스로 위임하여 호출
                PokedexService.checkAndRegisterPokedex(conn, userId, monsterName);

                System.out.println("[DB 반영] 포획 성공으로 인한 인벤토리 정상 저장 완료 유저ID: " + userId);

            } catch (Exception e) {
                System.err.println("CaptureService DB 연동 중 에러 발생:");
                e.printStackTrace();
                // DB 저장 오류 시 롤백 방어 코드로 실패 처리 유도
                result.addProperty("success", false);
            }
        } else {
            System.out.println("[포획 실패] CatchRateCalculator 판정 결과 실패로 판단하여 DB에 저장하지 않고 종료합니다. (확률: " + successRate + "%)");
        }

        return result;
    }
}