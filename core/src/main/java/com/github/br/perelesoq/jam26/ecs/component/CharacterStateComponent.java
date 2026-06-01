package com.github.br.perelesoq.jam26.ecs.component;

import com.artemis.PooledComponent;

public class CharacterStateComponent extends PooledComponent {

    public boolean onGround = false;

    @Override
    protected void reset() {
        onGround = false;
    }

}
