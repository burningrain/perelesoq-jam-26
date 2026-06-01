package com.github.br.perelesoq.jam26.ecs.component;

import com.artemis.PooledComponent;

public class TransformComponent extends PooledComponent {

    public float x;
    public float y;
    public float scaleX = 1f;
    public float scaleY = 1f;
    public float rotation;
    public float originX = Float.NaN;
    public float originY = Float.NaN;
    public boolean flipX = false;
    public boolean flipY = false;

    @Override
    protected void reset() {
        x = 0;
        y = 0;
        scaleX = 1f;
        scaleY = 1f;
        rotation = 0;
        originX = Float.NaN;
        originY = Float.NaN;
        flipX = false;
        flipY = false;
    }

}
