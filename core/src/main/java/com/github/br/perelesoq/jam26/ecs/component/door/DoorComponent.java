package com.github.br.perelesoq.jam26.ecs.component.door;

public class DoorComponent extends com.artemis.PooledComponent {

    public int doorId = -1;
    public boolean isOpeningTriggered = false; // Флаг, что запустили процесс открытия
    public boolean isClosingTriggered = false; // Флаг, что запустили процесс закрытия

    public DoorComponent(){}

    public DoorComponent(int doorId) {
        this.doorId = doorId;
    }

    @Override
    protected void reset() {
        doorId = -1;
        isOpeningTriggered = false;
        isClosingTriggered = false;
    }
}
