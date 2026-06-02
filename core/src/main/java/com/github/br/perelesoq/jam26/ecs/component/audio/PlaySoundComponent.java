package com.github.br.perelesoq.jam26.ecs.component.audio;

import com.artemis.PooledComponent;

public class PlaySoundComponent extends PooledComponent {

    public String soundName;
    public float volume = 1.0f;
    public float pitch = 1.0f;

    @Override
    protected void reset() {
        soundName = null;
        volume = 1.0f;
        pitch = 1.0f;
    }

}
