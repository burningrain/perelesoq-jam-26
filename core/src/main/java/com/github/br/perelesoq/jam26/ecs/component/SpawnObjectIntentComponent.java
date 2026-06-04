package com.github.br.perelesoq.jam26.ecs.component;

import com.artemis.PooledComponent;

public class SpawnObjectIntentComponent extends PooledComponent {

    public String targetZoneName; // "spawn1", "spawn2" или "ANY"

    @Override
    protected void reset() {
        targetZoneName = null;
    }

}
