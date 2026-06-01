package com.github.br.perelesoq.jam26.render.ui;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface ObjectLayerObjectRenderInterceptor {

    boolean isIgnoreLayer(String name);

    void draw(String layerName, Batch batch);

}
