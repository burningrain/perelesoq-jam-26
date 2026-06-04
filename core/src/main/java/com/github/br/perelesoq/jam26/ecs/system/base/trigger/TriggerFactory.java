package com.github.br.perelesoq.jam26.ecs.system.base.trigger;

import com.artemis.BaseSystem;
import com.artemis.EntityEdit;
import com.artemis.World;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.perelesoq.jam26.dialogs.CinematicFactory;
import com.github.br.perelesoq.jam26.dialogs.DialogFactory;
import com.github.br.perelesoq.jam26.ecs.EntityFactory;
import com.github.br.perelesoq.jam26.ecs.component.*;
import com.github.br.perelesoq.jam26.ecs.component.door.OpenDoorIntentComponent;
import com.github.br.perelesoq.jam26.ecs.component.physics.Hitbox;
import com.github.br.perelesoq.jam26.ecs.component.physics.PhysicsComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.Controller1SingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.DialogueSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.cinematic.CinematicSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.trigger.TriggerComponent;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;
import com.github.br.perelesoq.jam26.render.ui.AnimatedImage;
import com.github.br.perelesoq.jam26.render.ui.CustomOrthogonalTiledMapRenderer;

public class TriggerFactory extends BaseSystem {

    private final CustomOrthogonalTiledMapRenderer renderer;
    private final DialogFactory dialogFactory;
    private final CinematicFactory cinematicFactory;

    public TriggerFactory(
        CustomOrthogonalTiledMapRenderer renderer,
        DialogFactory dialogFactory,
        CinematicFactory cinematicFactory
    ) {
        this.renderer = renderer;
        this.dialogFactory = dialogFactory;
        this.cinematicFactory = cinematicFactory;
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
                    createTerminalTrigger(edit, properties);
                    break;
                case "controller_trigger":
                    createControllerTrigger(edit, properties);
                    break;
                case "level_exit_trigger":
                    createLevelExitTrigger(edit, properties);
                    break;
            }
        }
    }

    public void createLevelExitTrigger(EntityEdit edit, MapProperties properties) {
        String nextLevelKey = properties.get("nextLevelState", String.class);
        String endCinematic = properties.get("endCinematic", String.class);

        TriggerComponent trigger = edit.create(TriggerComponent.class);
        trigger.requiresExecution = false;

        trigger.action = new TriggerAction() {
            @Override
            public void onEnter(int playerEntityId, int triggerEntityId) {}

            @Override
            public void onExit(int playerEntityId, int triggerEntityId) {}

            @Override
            public void onExecute(int playerEntityId, int triggerEntityId) {
               if (endCinematic == null) {
                    createShangeLevelIntentComponent(nextLevelKey, trigger);
                    return;
                }

                Array<CinematicSingletonComponent.CinematicStep> cinematicScript =
                    cinematicFactory.getCinematicScript(endCinematic);
                if (cinematicScript != null) {
                    cinematicScript.add(new CinematicSingletonComponent.CinematicStep() {
                        @Override
                        public void onStart(World world) {
                        }
                        @Override
                        public boolean onUpdate(World world, float delta) {
                            createShangeLevelIntentComponent(nextLevelKey, trigger);
                            return true;
                        }
                    });
                    cinematicFactory.start(cinematicScript);
                } else {
                    createShangeLevelIntentComponent(nextLevelKey, trigger);
                }
            }
        };
    }

    private void createShangeLevelIntentComponent(String nextLevelKey, TriggerComponent trigger) {
        // Создаем сущность-интент для смены уровня
        int intentEntity = world.create();
        ChangeLevelIntentComponent intent = world.edit(intentEntity).create(ChangeLevelIntentComponent.class);
        intent.nextLevelKey = nextLevelKey;
        // Выключаем триггер, чтобы не нажать дважды во время анимации
        trigger.isNotReused = true;
    }

    public void createControllerTrigger(EntityEdit edit, MapProperties properties) {
        Integer doorId = EntityFactory.getDoorId(properties);
        String dialogId = EntityFactory.getDialogId(properties);

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
                boolean isActive = Controller1SingletonComponent.INSTANCE.isActive;
                if (isActive) {
                    int entityId = world.create();
                    EntityEdit edit = world.edit(entityId);
                    OpenDoorIntentComponent openDoorIntentComponent = edit.create(OpenDoorIntentComponent.class);
                    openDoorIntentComponent.doorId = doorId;
                    trigger.isNotReused = true;
                    Controller1SingletonComponent.INSTANCE.isActive = false; // сбрасываем флаг
                } else {
                    if (dialogId != null) {
                        DialogueSingletonComponent.INSTANCE.start(dialogFactory.getDialog(dialogId));
                    }
                }
            }
        };
    }

    public void createTerminalTrigger(EntityEdit edit, MapProperties properties) {
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
                String dialog = properties.get("dialog", String.class);
                if (dialog == null) {
                    throw new GdxRuntimeException("terminal property 'dialog' is not found");
                }
                DialogueSingletonComponent.INSTANCE.start(dialogFactory.getDialog(dialog));
                trigger.isNotReused = true;
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
