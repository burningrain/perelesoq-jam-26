package com.github.br.perelesoq.jam26.ecs;

import com.artemis.*;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.perelesoq.jam26.animation.AnimationFactory;
import com.github.br.perelesoq.jam26.ecs.component.*;
import com.github.br.perelesoq.jam26.ecs.component.door.DoorComponent;
import com.github.br.perelesoq.jam26.ecs.component.physics.Hitbox;
import com.github.br.perelesoq.jam26.ecs.component.physics.PhysicsComponent;
import com.github.br.perelesoq.jam26.ecs.component.ui.render.RenderComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.ui.AnimationComponent;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;

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
        physicsComponent.hitbox = new Hitbox(32f, 32f, 9f, 10f, 4f, 2f);
        physicsComponent.useGravity = true;

        AnimationComponent animationComponent = animationMapper.get(id);
        animationComponent.simpleAnimationComponent = AnimationFactory.createHero();

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.textureRegion = animationComponent.simpleAnimationComponent.animatorDynamicPart.currentFrame;
        renderComponent.layer = TiledUiConstants.Layers.GAME_OBJECTS_LAYER;

        HeroSingletonComponent.INSTANCE.playerId = id; //TODO кривота

        return id;
    }

    public EntityEdit createStaticObstacle(float x, float y, float width, float height) {
        int entityId = world.create();
        EntityEdit edit = world.edit(entityId);

        TransformComponent transform = edit.create(TransformComponent.class);
        transform.x = x;
        transform.y = y;

        PhysicsComponent physics = edit.create(PhysicsComponent.class);
        physics.hitbox = new Hitbox(width, height, 0f, 0f, 0f, 0f);
        physics.useGravity = false; // СТЕНЫ НЕ ПАДАЮТ

        edit.create(VelocityComponent.class);
        edit.create(CharacterStateComponent.class);

        return edit;
    }

    public void createDoor(MapProperties properties, float x, float y, float width, float height) {
        EntityEdit doorEntity = createStaticObstacle(x, y, width, height);

        DoorComponent doorComponent = doorEntity.create(DoorComponent.class);
        doorComponent.doorId = getDoorId(properties);

        AnimationComponent animationComponent = doorEntity.create(AnimationComponent.class);
        animationComponent.simpleAnimationComponent = AnimationFactory.createDoor();

        RenderComponent renderComponent = doorEntity.create(RenderComponent.class);
        renderComponent.textureRegion = animationComponent.simpleAnimationComponent.animatorDynamicPart.currentFrame;
        renderComponent.layer = TiledUiConstants.Layers.GAME_OBJECTS_LAYER;
    }

    public static Integer getDoorId(MapProperties properties) {
        String doorId = properties.get("doorId", String.class);
        if (doorId == null) {
            throw new GdxRuntimeException("parameter 'doorId' is not found");
        }
        return Integer.parseInt(doorId);
    }

    public static String getDialogId(MapProperties properties) {
        return properties.get("dialogId", String.class);
    }

    @Override
    protected void processSystem() {
        // Метод пустой, так как система passive (setEnabled(false))
    }

    public void createGameObjects(TiledMap tiledMap) {
        MapLayer gameObjects = tiledMap.getLayers().get(TiledUiConstants.Layers.GAME_OBJECTS_LAYER);
        if (gameObjects == null) {
            throw new GdxRuntimeException("entity layer [" + TiledUiConstants.Layers.GAME_OBJECTS_LAYER +
                "] is not found. Create layer in Tiled Map");
        }

        MapObjects objects = gameObjects.getObjects();
        for (MapObject object : objects) {
            String name = object.getName();

            MapProperties properties = object.getProperties();
            float x = properties.get("x", float.class);
            float y = properties.get("y", float.class);
            float width = properties.get("width", float.class);
            float height = properties.get("height", float.class);

            switch (name) {
                case "player":
                    createPlayer((int) x, (int) y);
                    break;
                case "wall":
                    createStaticObstacle(x, y, width, height);
                    break;
                case "door":
                    createDoor(properties, x, y, width, height);
                    break;
            }
        }
    }

}
