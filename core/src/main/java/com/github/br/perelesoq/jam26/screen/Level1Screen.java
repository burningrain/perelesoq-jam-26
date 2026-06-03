package com.github.br.perelesoq.jam26.screen;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.ashvard.gdx.simple.animation.SimpleAnimation;
import com.github.br.perelesoq.jam26.Constants;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.dialogs.DialogFactory;
import com.github.br.perelesoq.jam26.ecs.EntityFactory;
import com.github.br.perelesoq.jam26.ecs.component.singleton.DialogueSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.SirenSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.ViewPortSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.system.DoorSystem;
import com.github.br.perelesoq.jam26.ecs.system.dialog.DialogViewAvatarFactory;
import com.github.br.perelesoq.jam26.ecs.system.dialog.DialogueSystem;
import com.github.br.perelesoq.jam26.ecs.system.HeroAnimationStateSystem;
import com.github.br.perelesoq.jam26.ecs.system.SirenSystem;
import com.github.br.perelesoq.jam26.ecs.system.base.audio.AudioSystem;
import com.github.br.perelesoq.jam26.ecs.system.base.physics.PhysicsSystem;
import com.github.br.perelesoq.jam26.ecs.system.base.trigger.TriggerFactory;
import com.github.br.perelesoq.jam26.ecs.system.base.trigger.TriggerSystem;
import com.github.br.perelesoq.jam26.ecs.system.InputSystemImpl;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.AnimationSystem;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.CameraSystem;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.RenderSystem;
import com.github.br.perelesoq.jam26.render.ActorFactory;
import com.github.br.perelesoq.jam26.structure.screen.AbstractGameScreen;

public class Level1Screen extends AbstractGameScreen {

    private TiledMap tiledMap;
    private ActorFactory actorFactory;
    private DialogFactory dialogFactory;

    private World world;

    @Override
    public void show() {
        dialogFactory = new DialogFactory();

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
        entityFactory.createGameObjects(tiledMap);

        TriggerFactory system = world.getSystem(TriggerFactory.class);
        system.createGameObjects(tiledMap);
    }

    private World createEcsEngine(AssetManager assetManager) {
        AnimationSystem animationSystem = new AnimationSystem();
        animationSystem.addAnimation(assetManager.<SimpleAnimation>get(Resources.Animations.HERO_ANIM_FSM));
        animationSystem.addAnimation(assetManager.<SimpleAnimation>get(Resources.Animations.DOOR_ANIM_FSM));

        RenderSystem renderSystem = new RenderSystem(
            actorFactory,
            ViewPortSingletonComponent.INSTANCE.viewPort,
            tiledMap,
            1f
        );

        InputSystemImpl inputSystem = new InputSystemImpl();
        /*
        Правильное и каноничное разделение фаз игрового кадра (Frame Lifecycle) в геймдеве выглядит так:
            1) Фаза Ввода (Сбор нажатий клавиш).Фаза Логики / Состояния (Сирена, Состояния анимаций персонажа, ИИ врагов).
            2) Фаза Физики (Движение хитбоксов, гравитация, расчет новых координат TransformComponent).
            3) Фаза Триггеров (Анализ столкновений, которые только что произошли на фазе физики).
            4) Фаза Пост-логики / Камеры (Камера центрируется по уже измененным и проверенным координатам трансформ-компонента).
            5) Фаза Отрисовки (Анимация, Аудио и финальный Рендеринг).
         */
        WorldConfiguration setup = new WorldConfigurationBuilder()
            // --- 1. ФАЗА ВВОДА ---
            .with(inputSystem)
            .with(new DialogueSystem(inputSystem, renderSystem, new DialogViewAvatarFactory(getGameManager().assetManager)))

            // --- 2. ФАЗА ИГРОВОЙ ЛОГИКИ И СОСТОЯНИЙ ---
            .with(new SirenSystem())                  // Считает альфу и звук до симуляции физики и рендера
            .with(new HeroAnimationStateSystem())     // Определяет, бежит персонаж или прыгает, выставляя флаги флипа
            .with(new DoorSystem())                   // Читает намерения, запускает FSM дверей и вовремя удаляет их физику

            // --- 3. ФАЗА ФИЗИКИ И ПЕРЕМЕЩЕНИЯ ---
            .with(new PhysicsSystem())                // Рассчитывает движение, двигает хитбокс, обновляет TransformComponent

            // --- 4. ФАЗА ТРИГГЕРОВ И КОЛЛИЗИЙ ---
            .with(new TriggerSystem())                // Срабатывает сразу после физического шага на актуальных координатах

            // --- 5. ФАЗА СЛЕДОВАНИЯ КАМЕРЫ (ПОСТ-ФИЗИКА) ---
            .with(new CameraSystem(6.5f, 40f, 4f, 6f)) // Камера плавно летит строго к новым координатам трансформ-компонента

            // --- 6. ФАЗА ОТРИСОВКИ И ВЫВОДА ЭФФЕКТОВ ---
            .with(animationSystem)                    // Обновляет кадры анимации
            .with(renderSystem)                       // Очищает экран, применяет грязные флаги слоев Сирены, рисует карту и сущности
            .with(new AudioSystem(getGameManager().assetManager)) // Проигрывает накопленные за кадр звуки сирены и эффекты

            // --- НЕАКТИВНЫЕ ФАБРИКИ (Порядок не важен, они выключены) ---
            .with(new EntityFactory())
            .with(new TriggerFactory(renderSystem.getRenderer(), dialogFactory))
            .build();

        return new World(setup);
    }

    @Override
    public void render(float delta) {
        boolean isDialogueActive = DialogueSingletonComponent.INSTANCE.isActive;
        // ЗАМОРОЗКА/РАЗМОРОЗКА СИСТЕМ ПРИ АКТИВАЦИИ ОКНА ДИАЛОГА
        // Физика, триггеры и ввод игрока НЕ должны работать во время диалога
        world.getSystem(PhysicsSystem.class).setEnabled(!isDialogueActive);
        world.getSystem(TriggerSystem.class).setEnabled(!isDialogueActive);
        // Если у вас в InputSystemImpl зашито перемещение героя, её тоже выключаем:
        world.getSystem(InputSystemImpl.class).setGameplayInputPaused(isDialogueActive);
        // Сирена, анимации и рендеринг ДОЛЖНЫ работать всегда (чтобы окно диалога плавно появлялось)
        //world.getSystem(SirenSystem.class).setEnabled(!isDialogueActive);

        // Запускаем тик ECS-мира
        world.setDelta(delta);
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
