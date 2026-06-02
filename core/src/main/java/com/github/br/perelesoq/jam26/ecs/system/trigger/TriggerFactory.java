package com.github.br.perelesoq.jam26.ecs.system.trigger;

import com.artemis.BaseSystem;
import com.artemis.EntityEdit;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.perelesoq.jam26.ecs.component.CharacterStateComponent;
import com.github.br.perelesoq.jam26.ecs.component.physics.Hitbox;
import com.github.br.perelesoq.jam26.ecs.component.physics.PhysicsComponent;
import com.github.br.perelesoq.jam26.ecs.component.TransformComponent;
import com.github.br.perelesoq.jam26.ecs.component.VelocityComponent;
import com.github.br.perelesoq.jam26.ecs.component.trigger.TriggerComponent;
import com.github.br.perelesoq.jam26.render.CustomOrthogonalTiledMapRenderer;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;
import com.github.br.perelesoq.jam26.render.ui.AnimatedImage;

public class TriggerFactory extends BaseSystem {

    private final CustomOrthogonalTiledMapRenderer renderer;

    public TriggerFactory(CustomOrthogonalTiledMapRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    protected void initialize() {
        // Отключаем ежекадровое обновление, так как этот класс — просто фабрика
        setEnabled(false);
    }

    @Override
    protected void processSystem() {
    }

    public void createGameObjects(TiledMap tiledMap) {
        MapLayer gameObjects = tiledMap.getLayers().get(TiledUiConstants.Layers.TRIGGERS_LAYER);
        if (gameObjects == null) {
            throw new GdxRuntimeException("entity layer [" + TiledUiConstants.Layers.TRIGGERS_LAYER +
                "] is not found. Create layer in Tiled Map");
        }

        MapObjects objects = gameObjects.getObjects();
        for (MapObject object : objects) {
            String name = object.getName();

            MapProperties properties = object.getProperties();
            float x = properties.get("x", float.class);
            float y = properties.get("y", float.class);
            float width = properties.get("width", Float.class);
            float height = properties.get("height", Float.class);

            int entityId = world.create();
            EntityEdit edit = world.edit(entityId);
            createPhysicsForTrigger(edit, x, y, width, height);
            switch (name) {
                case "terminal_trigger":
                    createTerminalTrigger(edit);
                    break;
                case "controller_trigger":
                    createControllerTrigger(edit);
                    break;
            }
        }
    }

    public void createControllerTrigger(EntityEdit edit) {
        TriggerComponent trigger = edit.create(TriggerComponent.class);
        trigger.requiresExecution = true;
        trigger.action = new TriggerAction() {
            @Override
            public void onEnter(int playerEntityId, int triggerEntityId) {
                AnimatedImage actor = renderer.getActor(
                    TiledUiConstants.Layers.ACTORS_LAYER, TiledUiConstants.Actors.CONTROLLER, AnimatedImage.class
                );
                actor.setFrameAndPause(1);
            }

            @Override
            public void onExit(int playerEntityId, int triggerEntityId) {
                AnimatedImage actor = renderer.getActor(
                    TiledUiConstants.Layers.ACTORS_LAYER, TiledUiConstants.Actors.CONTROLLER, AnimatedImage.class
                );
                actor.setFrameAndPause(0);
            }

            @Override
            public void onExecute(int playerEntityId, int triggerEntityId) {

            }
        };
    }

    public void createTerminalTrigger(EntityEdit edit) {
        TriggerComponent trigger = edit.create(TriggerComponent.class);
        trigger.requiresExecution = true;
        trigger.action = new TriggerAction() {
            @Override
            public void onEnter(int playerEntityId, int triggerEntityId) {
                AnimatedImage actor = renderer.getActor(
                    TiledUiConstants.Layers.ACTORS_LAYER, TiledUiConstants.Actors.TERMINAL, AnimatedImage.class
                );
                actor.setFrameAndPause(1);
            }

            @Override
            public void onExit(int playerEntityId, int triggerEntityId) {
                AnimatedImage actor = renderer.getActor(
                    TiledUiConstants.Layers.ACTORS_LAYER, TiledUiConstants.Actors.TERMINAL, AnimatedImage.class
                );
                actor.setFrameAndPause(0);
            }

            @Override
            public void onExecute(int playerEntityId, int triggerEntityId) {

            }
        };
    }

    private void createPhysicsForTrigger(EntityEdit edit, float x, float y, float width, float height) {
        TransformComponent transform = edit.create(TransformComponent.class);
        transform.x = x;
        transform.y = y;

        PhysicsComponent physics = edit.create(PhysicsComponent.class);
        physics.hitbox = new Hitbox(width, height, 0f, 0f, 0f, 0f);
        physics.useGravity = false;

        // для работы физической системы
        edit.create(VelocityComponent.class);
        edit.create(CharacterStateComponent.class);
    }

}
