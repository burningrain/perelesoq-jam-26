package com.github.br.perelesoq.jam26.ecs.component;

import com.artemis.PooledComponent;

public class CharacterStateComponent extends PooledComponent {

    public final float ATTACK_ANIM_DURATION = 1f / 12f * 3f; // Длительность анимации 3 кадра
    public boolean onGround = false;
    public float attackAnimTimer = 0f; // Таймер удержания анимации атаки

    @Override
    protected void reset() {
        onGround = false;
        attackAnimTimer = 0f;
    }

}
