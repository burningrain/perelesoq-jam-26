package com.github.br.perelesoq.jam26.ecs.component.singleton.cinematic;

import com.artemis.World;

public class DelayStep implements CinematicSingletonComponent.CinematicStep {

    private float delay;

    public DelayStep(float delay) {
        this.delay = delay;
    }

    @Override
    public void onStart(World world) {
    }

    @Override
    public boolean onUpdate(World world, float delta) {
        delay -= delta;
        return delay <= 0;
    }

}
