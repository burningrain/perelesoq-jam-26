package com.github.br.perelesoq.jam26.screen;

import com.artemis.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.ashvard.gdx.simple.animation.SimpleAnimation;
import com.github.br.perelesoq.jam26.Constants;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.ecs.EntityFactory;
import com.github.br.perelesoq.jam26.ecs.component.AnimationComponent;
import com.github.br.perelesoq.jam26.ecs.component.RenderComponent;
import com.github.br.perelesoq.jam26.ecs.component.TransformComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.ViewPortSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.system.input.InputSystem;
import com.github.br.perelesoq.jam26.ecs.system.ui.AnimationSystem;
import com.github.br.perelesoq.jam26.ecs.system.ui.CameraSystem;
import com.github.br.perelesoq.jam26.ecs.system.ui.RenderSystem;
import com.github.br.perelesoq.jam26.render.ActorFactory;
import com.github.br.perelesoq.jam26.structure.screen.AbstractGameScreen;

public class Level1Screen extends AbstractGameScreen {

    private TiledMap tiledMap;
    private ActorFactory actorFactory;

    private World world;

    @Override
    public void show() {
        AssetManager assetManager = getGameManager().assetManager;
        tiledMap = assetManager.get(Resources.Tiled.LEVEL_1_ENTRANCE);

        Skin gameSkin = assetManager.get(Resources.SKIN);
        actorFactory = new ActorFactory(gameSkin, assetManager);

        ViewPortSingletonComponent.INSTANCE.camera = new OrthographicCamera();
        ViewPortSingletonComponent.INSTANCE.viewPort = new FitViewport(
            Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, ViewPortSingletonComponent.INSTANCE.camera
        );
        GameScreenUtils.centerCamera(ViewPortSingletonComponent.INSTANCE.camera);

        world = createEcsEngine(assetManager);

        EntityFactory entityFactory = world.getSystem(EntityFactory.class);
        entityFactory.createPlayer(50, 50);
    }

    private World createEcsEngine(AssetManager assetManager) {
        AnimationSystem animationSystem = new AnimationSystem();
        animationSystem.addAnimation(assetManager.<SimpleAnimation>get(Resources.Animations.HERO_ANIM_FSM));

        RenderSystem renderSystem = new RenderSystem(
            actorFactory,
            ViewPortSingletonComponent.INSTANCE.viewPort,
            tiledMap,
            1f
        );

        WorldConfiguration setup = new WorldConfigurationBuilder()
            .with(new InputSystem())

            .with(new CameraSystem(6.5f, 40f, 4f, 6f)) //TODO в значениях сильно не уверен
            .with(animationSystem)
            .with(renderSystem)

            .with(new EntityFactory())
            .build();

        return new World(setup);
    }

    @Override
    public void render(float delta) {
        world.process();
    }

    @Override
    public void resize(int width, int height) {
        ViewPortSingletonComponent.INSTANCE.viewPort.update(width, height);
        GameScreenUtils.centerCamera(ViewPortSingletonComponent.INSTANCE.camera);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

}
