package com.github.br.perelesoq.jam26.ecs.component.singleton;

public class HeroSingletonComponent {

    public static final HeroSingletonComponent INSTANCE = new HeroSingletonComponent();

    public int playerId;

    public boolean hasWeapon = false;

    public float RUN_SPEED = 90f; // 40f слишком медленно для экрана 320, персонаж будет ползти

}
