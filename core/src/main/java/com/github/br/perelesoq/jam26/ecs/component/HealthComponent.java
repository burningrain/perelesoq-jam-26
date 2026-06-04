package com.github.br.perelesoq.jam26.ecs.component;

import com.artemis.PooledComponent;

public class HealthComponent extends PooledComponent {

    public float hp = 4f;       // Текущее здоровье
    public float maxHp = 4f;    // Максимальное здоровье (для шкалы HP)
    public boolean isDead = false;

    @Override
    protected void reset() {
        hp = 4f;
        maxHp = 4f;
        isDead = false;
    }

}
