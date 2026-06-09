package com.game.logic;

import java.util.Random;

public interface PercentProbability {
    // 성공 확률(0~100)을 계산해서 반환하는 메서드 규칙
    int calculateSuccessRate();

    default boolean rollDice(int rate) {
        Random random = new Random();
        return rate > random.nextInt(100);
    }
}