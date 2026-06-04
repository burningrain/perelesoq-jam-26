package com.github.br.perelesoq.jam26.ecs.component;

import com.artemis.PooledComponent;

public class SpawnBulletIntentComponent extends PooledComponent {

    public float startX;
    public float startY;
    public float dirX;

    @Override
    protected void reset() {
        startX = 0;
        startY = 0;
        dirX = 0;
    }

}
