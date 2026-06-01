package com.github.br.perelesoq.jam26.ecs.system.ui;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.github.br.perelesoq.jam26.ecs.component.TransformComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.ViewPortSingletonComponent;

public class CameraSystem extends BaseSystem {

    protected ComponentMapper<TransformComponent> transformMapper;

    private int focusEntityId = -1;
    private final float xMin, xMax, yMin, yMax;

    private final Vector3 mVector3 = new Vector3();
    private float newZoom = 1f;

    public CameraSystem(float xMin, float xMax, float yMin, float yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    public void changeZoomTo(float newZoom) {
        this.newZoom = newZoom;
    }

    public void setFocusEntityId(int focusEntityId) {
        this.focusEntityId = focusEntityId;
    }

    @Override
    protected void processSystem() {
        ViewPortSingletonComponent viewPortComponent = ViewPortSingletonComponent.INSTANCE;
        OrthographicCamera camera = viewPortComponent.camera;

        if (camera.zoom != newZoom) {
            camera.zoom = MathUtils.lerp(camera.zoom, newZoom, 0.01f);
        }

        if (focusEntityId != -1) {
            TransformComponent transformComponent = transformMapper.get(focusEntityId);

            if (transformComponent != null) {
                float x = Math.max(xMin, Math.min(xMax, transformComponent.x));
                float y = Math.max(yMin, Math.min(yMax, transformComponent.y + 2));

                mVector3.set(x, y, 0);
                camera.position.lerp(mVector3, 0.1f);
            }
        }

        camera.update();
    }

}
