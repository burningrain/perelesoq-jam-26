package com.github.br.perelesoq.jam26;

import com.artemis.Aspect;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.ashvard.gdx.simple.animation.SimpleAnimation;
import com.github.br.perelesoq.jam26.dialogs.CinematicFactory;
import com.github.br.perelesoq.jam26.dialogs.DialogFactory;
import com.github.br.perelesoq.jam26.ecs.EntityFactory;
import com.github.br.perelesoq.jam26.ecs.component.singleton.*;
import com.github.br.perelesoq.jam26.ecs.component.singleton.cinematic.CinematicSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.system.*;
import com.github.br.perelesoq.jam26.ecs.system.base.audio.AudioSystem;
import com.github.br.perelesoq.jam26.ecs.system.base.cinematic.CinematicSystem;
import com.github.br.perelesoq.jam26.ecs.system.base.physics.PhysicsSystem;
import com.github.br.perelesoq.jam26.ecs.system.base.trigger.TriggerFactory;
import com.github.br.perelesoq.jam26.ecs.system.base.trigger.TriggerSystem;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.AnimationSystem;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.CameraSystem;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.RenderSystem;
import com.github.br.perelesoq.jam26.ecs.system.dialog.DialogViewAvatarFactory;
import com.github.br.perelesoq.jam26.ecs.system.dialog.DialogueSystem;
import com.github.br.perelesoq.jam26.render.ActorFactory;
import com.github.br.perelesoq.jam26.render.ui.CustomOrthogonalTiledMapRenderer;
import com.github.br.perelesoq.jam26.structure.GameManager;
import com.github.br.perelesoq.jam26.structure.UserFactory;

public class UserFactoryImpl implements UserFactory {

    public GameManager<UserFactoryImpl> gameManager;

    public ActorFactory actorFactory;
    public DialogFactory dialogFactory;
    public CinematicFactory cinematicFactory;

    public World ecsWorld;

    @Override
    public void init(GameManager gameManager) {
        this.gameManager = gameManager;

        dialogFactory = new DialogFactory();
        cinematicFactory = new CinematicFactory();

        AssetManager assetManager = getGameManager().assetManager;
        Skin gameSkin = assetManager.get(Resources.SKIN);
        actorFactory = new ActorFactory(gameSkin, assetManager);
    }

    /**
     * Создает ECS-мир ОДИН раз при запуске игры. Все системы кэшируются здесь.
     */
    private World createGlobalEcsEngine(AssetManager assetManager, TiledMap tiledMap) {
        AnimationSystem animationSystem = new AnimationSystem();
        animationSystem.addAnimation(assetManager.<SimpleAnimation>get(Resources.Animations.HERO_ANIM_FSM));
        animationSystem.addAnimation(assetManager.<SimpleAnimation>get(Resources.Animations.DOOR_ANIM_FSM));

        animationSystem.addAnimation(assetManager.<SimpleAnimation>get(Resources.Animations.IF_TREE_ANIM_FSM));
        animationSystem.addAnimation(assetManager.<SimpleAnimation>get(Resources.Animations.BOSS_ANIM_FSM));

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
            .with(new CinematicSystem())
            .with(new SirenSystem())                  // Считает альфу и звук до симуляции физики и рендера
            .with(new BossDeathSystem())
            .with(new ObjectSpawnerSystem())
            .with(new BulletSpawnerSystem())          // ОБРАБАТЫВАЕТ СИГНАЛ ВЫСТРЕЛА СРАЗУ ПОСЛЕ ИНПУТА
            .with(new BulletSystem())

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
            .with(new LevelTransitionSystem(getGameManager()))

            // --- НЕАКТИВНЫЕ ФАБРИКИ (Порядок не важен, они выключены) ---
            .with(new EntityFactory(assetManager))
            .with(new TriggerFactory(renderSystem, dialogFactory, cinematicFactory))
            .build();

        return new World(setup);
    }

    private GameManager getGameManager() {
        return gameManager;
    }

    /**
     * Централизованный метод полной очистки текущего уровня перед загрузкой следующего.
     * Вызывается из экранов уровней.
     */
    public void resetCurrentLevel(TiledMap tiledMap) {
        if (ecsWorld == null) {
            this.ecsWorld = createGlobalEcsEngine(gameManager.assetManager, tiledMap);
        } else {
            RenderSystem renderSystem = ecsWorld.getSystem(RenderSystem.class);
            renderSystem.setNewTileMap(tiledMap);
        }

        // 1. Стираем старые сущности
        IntBag entities = ecsWorld.getAspectSubscriptionManager()
            .get(Aspect.all())
            .getEntities();

        for (int i = entities.size() - 1; i >= 0; i--) {
            ecsWorld.delete(entities.get(i));
        }

        // 2. Жестко сбрасываем синглтоны блокировок геймплея
        HeroSingletonComponent.INSTANCE.playerId = -1;
        HeroSingletonComponent.INSTANCE.hasWeapon = false;

        // Чистим диалоги и кат-сцены, чтобы shouldPauseGameplay гарантированно стал false!
        DialogueSingletonComponent.INSTANCE.isActive = false;
        CinematicSingletonComponent.INSTANCE.steps.clear();
        CinematicSingletonComponent.INSTANCE.isActive = false;

        SirenSingletonComponent.INSTANCE.isActive = false;
        Controller1SingletonComponent.INSTANCE.isActivated = false;

        // 3. Чистим ядро Artemis без запуска систем
        ecsWorld.getSystem(com.artemis.EntityManager.class).reset();

        // 4. Наполняем фабриками новый уровень
        EntityFactory entityFactory = ecsWorld.getSystem(EntityFactory.class);
        entityFactory.createGameObjects(tiledMap);

        TriggerFactory triggerFactory = ecsWorld.getSystem(TriggerFactory.class);
        triggerFactory.createGameObjects(tiledMap);

        // 5. Синхронизируем мапперы компонентов для нового героя
        ecsWorld.getAspectSubscriptionManager().process();
    }

    public void render(float delta) {
        boolean isDialogueActive = DialogueSingletonComponent.INSTANCE.isActive;
        boolean isCinematicActive = CinematicSingletonComponent.INSTANCE.isActive;
        // Игрок теряет управление, если идет диалог ИЛИ кат-сцена
        boolean shouldPauseGameplay = isDialogueActive || isCinematicActive;

        // ЗАМОРОЗКА/РАЗМОРОЗКА СИСТЕМ ПРИ АКТИВАЦИИ ОКНА ДИАЛОГА
        // Физика, триггеры и ввод игрока НЕ должны работать во время диалога
        ecsWorld.getSystem(PhysicsSystem.class).setEnabled(!shouldPauseGameplay);
        ecsWorld.getSystem(TriggerSystem.class).setEnabled(!shouldPauseGameplay);
        ecsWorld.getSystem(InputSystemImpl.class).setGameplayInputPaused(shouldPauseGameplay);

        // Запускаем тик ECS-мира
        ecsWorld.setDelta(delta);
        ecsWorld.process();
    }

}
