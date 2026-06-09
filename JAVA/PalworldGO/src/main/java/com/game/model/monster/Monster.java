package com.game.model.monster;

public abstract class Monster {
    protected int id;
    protected String name;
    protected String type;

    public Monster(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    // 기본 스폰 가중치
    public abstract int getBaseSpawnWeight();
}