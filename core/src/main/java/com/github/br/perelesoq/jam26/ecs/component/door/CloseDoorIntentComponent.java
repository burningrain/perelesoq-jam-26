package com.github.br.perelesoq.jam26.ecs.component.door;

import com.artemis.PooledComponent;

public class CloseDoorIntentComponent extends PooledComponent {
    public int doorId = -1;

    @Override
    protected void reset() {
        doorId = -1;
    }
}
