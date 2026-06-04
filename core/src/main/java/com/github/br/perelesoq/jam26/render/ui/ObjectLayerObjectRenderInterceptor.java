package com.github.br.perelesoq.jam26.render.ui;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface ObjectLayerObjectRenderInterceptor {

    ObjectLayerObjectRenderInterceptor DEFAULT = new ObjectLayerObjectRenderInterceptor() {
        @Override
        public void clearBuffer() {
        }

        @Override
        public boolean isIgnoreLayer(String name) {
            return false;
        }

        @Override
        public void draw(String layerName, Batch batch) {
        }
    };

    void clearBuffer();

    boolean isIgnoreLayer(String name);

    void draw(String layerName, Batch batch);

}
