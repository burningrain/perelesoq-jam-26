package com.github.br.perelesoq.jam26.ecs.component;

import com.artemis.PooledComponent;

public class JumpControlComponent extends PooledComponent {

    public final float INITIAL_JUMP_FORCE = 100f; // Короткий прыжок не меняется
    public final float HOLD_JUMP_FORCE = 650f;    // Умеренная сила
    public final float MAX_HOLD_TIME = 0.29f;     // Персонаж разгоняется вверх на 3 кадрa дольше


    public boolean isJumping = false;
    public float jumpTimeCounter = 0f;

    // Разрешен ли старт прыжка (чтобы предотвратить спам импульса в воздухе)
    public boolean isJumpReady = true;

    @Override
    protected void reset() {
        isJumping = false;
        jumpTimeCounter = 0;
        isJumpReady = false;
    }

}
