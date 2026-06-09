package com.game.logic;

public class CatchRateCalculator implements PercentProbability {
    private int clickCount;

    public CatchRateCalculator(int clickCount) {
        this.clickCount = clickCount;
    }

    @Override
    public int calculateSuccessRate() {
        int rate = 30 + (this.clickCount * 4);

        return Math.min(rate, 95); // 최대 95% 제한
    }
}
