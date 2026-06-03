package com.github.br.perelesoq.jam26.ecs.component.ui.render;

import com.artemis.PooledComponent;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RenderComponent extends PooledComponent {

    public boolean isVisible = true;
    public String layer;
    public transient TextureRegion textureRegion;

    @Override
    protected void reset() {
        isVisible = true;
        layer = null;
        textureRegion = null;
    }

}
