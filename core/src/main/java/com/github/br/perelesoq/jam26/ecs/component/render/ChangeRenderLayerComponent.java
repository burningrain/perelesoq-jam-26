package com.github.br.perelesoq.jam26.ecs.component.render;

import com.artemis.PooledComponent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class ChangeRenderLayerComponent extends PooledComponent {

    private static final Color DEFAULT_TINT_COLOR = Color.WHITE;

    public boolean isDirty = true; // ФЛАГ: сигнализирует, что данные обновились

    public String layerName;

    public boolean isVisible = true;
    public float opacity = 1f;
    public Color tintColor = DEFAULT_TINT_COLOR;

    public float offsetX = 0;
    public float offsetY = 0;

    public final Vector2 parallaxFactor = new Vector2(1f, 1f);

    @Override
    protected void reset() {
        isDirty = true;

        layerName = null;
        isVisible = true;
        opacity = 1f;
        offsetX = 0;
        offsetY = 0;
        parallaxFactor.set(1f, 1f);
        tintColor = DEFAULT_TINT_COLOR;
    }

}
