package com.github.br.perelesoq.jam26.ecs.component;

import com.artemis.PooledComponent;

public class OpenDoorIntentComponent extends PooledComponent {

    public int doorId = -1;

    public OpenDoorIntentComponent(){}

    public OpenDoorIntentComponent(int doorId) {
        this.doorId = doorId;
    }

    @Override
    protected void reset() {
        doorId = -1;
    }
}
