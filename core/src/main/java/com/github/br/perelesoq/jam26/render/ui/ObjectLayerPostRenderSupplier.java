package com.github.br.perelesoq.jam26.render.ui;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface ObjectLayerPostRenderSupplier {
    void draw(String layerName, Batch batch);
}
