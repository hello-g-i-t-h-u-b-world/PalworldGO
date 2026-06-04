package com.game.logic;

import java.util.Random;

public interface PercentProbability {
    // 성공 확률(0~100)을 계산해서 반환하는 메서드 규칙
    int calculateSuccessRate();

    // 주사위를 굴려 최종 성공(true/false)을 판정하는 기본(default) 메서드 공통화
    default boolean rollDice(int rate) {
        Random random = new Random();
        return rate > random.nextInt(100);
    }
}