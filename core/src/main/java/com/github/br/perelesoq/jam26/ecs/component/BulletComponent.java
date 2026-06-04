package com.github.br.perelesoq.jam26.ecs.component;

import com.artemis.PooledComponent;

public class BulletComponent extends PooledComponent {

    public float damage = 1.0f;
    public float lifeTime = 7.0f; // Пуля исчезнет через 2 секунды, чтобы не засорять память

    @Override
    protected void reset() {
        damage = 1.0f;
        lifeTime = 7.0f;
    }

}
