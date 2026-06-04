package com.github.br.perelesoq.jam26.ecs.component;

import com.artemis.PooledComponent;

public class SpawnZoneComponent extends PooledComponent {

    public float width;
    public float height;
    public String spawnZoneName;

    @Override
    protected void reset() {
        width = 0; height = 0; spawnZoneName = null;
    }

}
