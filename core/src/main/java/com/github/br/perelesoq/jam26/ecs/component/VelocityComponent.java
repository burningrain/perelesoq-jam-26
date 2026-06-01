package com.github.br.perelesoq.jam26.ecs.component;

import com.artemis.PooledComponent;

public class VelocityComponent extends PooledComponent {

    public float x = 0;
    public float y = 0;

    @Override
    protected void reset() {
        x = 0;
        y = 0;
    }

}
