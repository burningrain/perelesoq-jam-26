package com.github.br.perelesoq.jam26.ecs;

import com.artemis.*;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.perelesoq.jam26.Constants;
import com.github.br.perelesoq.jam26.animation.AnimationFactory;
import com.github.br.perelesoq.jam26.ecs.component.*;
import com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent;

public class EntityFactory extends BaseSystem {

    private Archetype playerArchetype;

    protected ComponentMapper<TransformComponent> transformMapper;
    protected ComponentMapper<RenderComponent> renderMapper;
    protected ComponentMapper<AnimationComponent> animationMapper;
    protected ComponentMapper<PhysicsComponent> physicsMapper;

    @Override
    protected void initialize() {
        // Отключаем ежекадровое обновление, так как этот класс — просто фабрика
        setEnabled(false);

        playerArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(VelocityComponent.class)
            .add(JumpControlComponent.class)
            .add(CharacterStateComponent.class)
            .add(AnimationComponent.class)
            .add(RenderComponent.class)
            .build(world);
    }

    public int createPlayer(int x, int y) {
        int id = world.create(playerArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = physicsMapper.get(id);
        physicsComponent.width = 32f;
        physicsComponent.height = 32f;
        physicsComponent.useGravity = true;

        AnimationComponent animationComponent = animationMapper.get(id);
        animationComponent.simpleAnimationComponent = AnimationFactory.createHero();

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.textureRegion = animationComponent.simpleAnimationComponent.animatorDynamicPart.currentFrame;
        renderComponent.layer = Constants.GAME_OBJECTS_LAYER;

        HeroSingletonComponent.INSTANCE.playerId = id; //TODO кривота

        return id;
    }

    public int createStaticObstacle(float x, float y, float width, float height) {
        int entityId = world.create();
        EntityEdit edit = world.edit(entityId);

        TransformComponent transform = edit.create(TransformComponent.class);
        transform.x = x;
        transform.y = y;

        PhysicsComponent physics = edit.create(PhysicsComponent.class);
        physics.width = width;
        physics.height = height;
        physics.useGravity = false; // СТЕНЫ НЕ ПАДАЮТ

        edit.create(VelocityComponent.class);
        edit.create(CharacterStateComponent.class);

        return entityId;
    }

    @Override
    protected void processSystem() {
        // Метод пустой, так как система passive (setEnabled(false))
    }

    public void createGameObjects(TiledMap tiledMap) {
        MapLayer gameObjects = tiledMap.getLayers().get(Constants.GAME_OBJECTS_LAYER);
        if (gameObjects == null) {
            throw new GdxRuntimeException("entity layer [" + Constants.GAME_OBJECTS_LAYER +
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

            switch (name) {
                case "player":
                    createPlayer((int) x, (int) y);
                    break;
                case "wall":
                    createStaticObstacle(x, y, width, height);
                    break;
            }
        }
    }

}
