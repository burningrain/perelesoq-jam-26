package com.github.br.perelesoq.jam26.ecs.component.singleton;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.perelesoq.jam26.Constants;

public class ViewPortSingletonComponent {

    public static final ViewPortSingletonComponent INSTANCE = new ViewPortSingletonComponent();

    public OrthographicCamera camera = new OrthographicCamera();
    public Viewport viewPort = new FitViewport(
        Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera
    );

}
