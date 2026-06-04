package com.github.br.perelesoq.jam26.ecs;

import com.artemis.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.animation.AnimationFactory;
import com.github.br.perelesoq.jam26.ecs.component.*;
import com.github.br.perelesoq.jam26.ecs.component.door.DoorComponent;
import com.github.br.perelesoq.jam26.ecs.component.physics.Hitbox;
import com.github.br.perelesoq.jam26.ecs.component.physics.PhysicsComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.trigger.TriggerComponent;
import com.github.br.perelesoq.jam26.ecs.component.ui.AnimationComponent;
import com.github.br.perelesoq.jam26.ecs.component.ui.render.RenderComponent;
import com.github.br.perelesoq.jam26.ecs.system.base.trigger.TriggerAction;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;

public class EntityFactory extends BaseSystem {

    private final AssetManager assetManager;

    private Archetype playerArchetype;

    protected ComponentMapper<TransformComponent> transformMapper;
    protected ComponentMapper<RenderComponent> renderMapper;
    protected ComponentMapper<AnimationComponent> animationMapper;
    protected ComponentMapper<PhysicsComponent> physicsMapper;

    public EntityFactory(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

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

    private void createBoss(MapProperties properties, float x, float y, float width, float height) {
        int entityId = world.create();
        EntityEdit edit = world.edit(entityId);

        TransformComponent transformComponent = edit.create(TransformComponent.class);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = edit.create(PhysicsComponent.class);
        physicsComponent.hitbox = new Hitbox(width, height, 43f, 43f, 36f, 0f);
        physicsComponent.useGravity = false;

        edit.create(VelocityComponent.class);
        edit.create(CharacterStateComponent.class);

        AnimationComponent animationComponent = edit.create(AnimationComponent.class);
        animationComponent.simpleAnimationComponent = AnimationFactory.createBoss();

        RenderComponent renderComponent = edit.create(RenderComponent.class);
        renderComponent.textureRegion = animationComponent.simpleAnimationComponent.animatorDynamicPart.currentFrame;
        renderComponent.layer = TiledUiConstants.Layers.GAME_OBJECTS_LAYER;

        HealthComponent health = edit.create(HealthComponent.class);
        float bossHp = 4f;
        health.maxHp = bossHp;
        health.hp = bossHp;
    }

    public int createBullet(float x, float y, float dirX) {
        int id = world.create();
        EntityEdit edit = world.edit(id);

        // 1. Координаты
        TransformComponent transform = edit.create(TransformComponent.class);
        transform.x = x;
        transform.y = y;
        transform.flipX = (dirX < 0); // Поворачиваем пулю графически

        // 2. Физика пули (Размер хитбокса пули маленький, например 4x4 пикселя)
        PhysicsComponent physics = edit.create(PhysicsComponent.class);
        physics.hitbox = new Hitbox(12f, 12f, 2f, 1f, 3f, 3f);
        physics.useGravity = false; // Пули не падают на землю
        physics.isTrigger = true;   // Пуля — это сквозной триггер, она не должна толкать босса или стены!

        // 3. Скорость (Задаем вектор движения)
        VelocityComponent velocity = edit.create(VelocityComponent.class);
        velocity.x = dirX * 300f; // 300f — скорость полета снаряда

        edit.create(CharacterStateComponent.class);
        edit.create(BulletComponent.class);

        // 4. Текстура (Возьмите любой маленький пиксельный регион из атласа, например "bullet")
        RenderComponent render = edit.create(RenderComponent.class);
        TextureAtlas textureAtlas = assetManager.get(Resources.Atlases.GAME_OBJECTS, TextureAtlas.class);
        TextureAtlas.AtlasRegion region = textureAtlas.findRegion("if-bullet");
        render.textureRegion = region;
        render.layer = TiledUiConstants.Layers.GAME_OBJECTS_LAYER;

        return id;
    }

    private void createSpawnZone(String name, MapProperties properties, float x, float y, float width, float height) {
        int entityId = world.create();
        EntityEdit edit = world.edit(entityId);

        // Считываем границы области из Tiled
        TransformComponent transform = edit.create(TransformComponent.class);
        transform.x = x;
        transform.y = y;

        // Сохраняем размеры области прямо в кастомный компонент зоны спавна,
        // чтобы знать, в каких границах генерировать случайные координаты патронов
        SpawnZoneComponent zone = edit.create(SpawnZoneComponent.class);
        zone.width = width;
        zone.height = height;
        zone.spawnZoneName = name; // "spawn1" или "spawn2"
    }

    public void spawnIfTreeInZone(SpawnZoneComponent zone, TransformComponent zoneTrans) {
        int id = world.create();
        EntityEdit edit = world.edit(id);

        // Генерируем случайную точку внутри прямоугольника зоны спавна
        TransformComponent transform = edit.create(TransformComponent.class);
        transform.x = zoneTrans.x;
        transform.y = zoneTrans.y;

        // Делаем физический хитбокс патрона сквозным триггером
        PhysicsComponent physics = edit.create(PhysicsComponent.class);
        physics.hitbox = new Hitbox(16f, 16f, 0f, 0f, 0f, 0f);
        physics.useGravity = false; // Патроны парят в воздухе или лежат на платформах зоны
        physics.isTrigger = true;   // Герой пройдет сквозь него

        // Навешиваем обязательные маркеры для PhysicsSystem, чтобы хитбокс добавился в JBump
        edit.create(VelocityComponent.class);
        edit.create(CharacterStateComponent.class);

        // Навешиваем маркер, что это патрон, собираемый игроком
        edit.create(AmmoItemComponent.class);

        // Превращаем саму сущность патрона в триггер для работы TriggerSystem!
        TriggerComponent trigger = edit.create(TriggerComponent.class);
        trigger.requiresExecution = false; // Срабатывает автоматически при наступании (onEnter)
        trigger.action = new TriggerAction() {
            @Override
            public void onEnter(int playerEntityId, int triggerEntityId) {
                // Игрок подобрал патрон! Наращиваем счетчик пуль
                // (Для этого мы сейчас добавим поле ammo в HeroSingletonComponent)
                HeroSingletonComponent.INSTANCE.ammo++;
                HeroSingletonComponent.INSTANCE.hasWeapon = true; // На случай если патронов было 0

                // Звук подбора (опционально)

                // Уничтожаем патрон из мира
                world.delete(triggerEntityId);
            }
            @Override public void onExit(int pId, int tId) {}
            @Override public void onExecute(int pId, int tId) {}
        };

        // Навешиваем графику и анимацию "if_tree" (FSM подгрузится из вашего PreScreenAssetLoader)
        AnimationComponent animation = edit.create(AnimationComponent.class);
        animation.simpleAnimationComponent = AnimationFactory.createIfTree();

        RenderComponent render = edit.create(RenderComponent.class);
        render.textureRegion = animation.simpleAnimationComponent.animatorDynamicPart.currentFrame;
        render.layer = TiledUiConstants.Layers.GAME_OBJECTS_LAYER;
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
                case "boss":
                    createBoss(properties, x, y, width, height);
                    break;
                case "bullet":
                    float dir = Float.parseFloat(properties.get("dir", String.class));
                    createBullet(x, y, dir);
                    break;
                case "spawn1":
                case "spawn2":
                    createSpawnZone(name, properties, x, y, width, height);
                    break;
                default:
                    throw new GdxRuntimeException("gameObject [" + name + "] is not found");
            }
        }
    }

}
