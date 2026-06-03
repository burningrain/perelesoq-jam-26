package com.github.br.perelesoq.jam26.ecs.component;

public class DoorComponent extends com.artemis.PooledComponent {

    public int doorId = -1;
    public boolean isOpeningTriggered = false; // Флаг, что мы уже запустили процесс открытия

    public DoorComponent(){}

    public DoorComponent(int doorId) {
        this.doorId = doorId;
    }

    @Override
    protected void reset() {
        doorId = -1;
    }
}
