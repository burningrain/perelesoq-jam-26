package com.github.br.perelesoq.jam26.ecs.component.singleton;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ViewPortSingletonComponent {

    public static final ViewPortSingletonComponent INSTANCE = new ViewPortSingletonComponent();

    public OrthographicCamera camera;
    public Viewport viewPort;

}
