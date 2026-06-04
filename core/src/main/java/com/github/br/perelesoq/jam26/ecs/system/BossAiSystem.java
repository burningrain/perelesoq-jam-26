package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.utils.Array;
import com.github.ashvard.gdx.simple.animation.component.SimpleAnimatorUtils;
import com.github.ashvard.gdx.simple.animation.fsm.FsmContext;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.dialogs.DialogFactory;
import com.github.br.perelesoq.jam26.ecs.component.*;
import com.github.br.perelesoq.jam26.ecs.component.singleton.BossSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.DialogueSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.ui.AnimationComponent;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.RenderSystem;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;
import com.github.br.perelesoq.jam26.render.ui.CustomOrthogonalTiledMapRenderer;

public class BossAiSystem extends IteratingSystem {

    private ComponentMapper<AnimationComponent> mAnimation;
    private ComponentMapper<HealthComponent> mHealth;

    public BossAiSystem() {
        super(Aspect.all(HealthComponent.class, AnimationComponent.class).exclude(BulletComponent.class));
    }

    @Override
    protected void process(int bossEntityId) {
        HealthComponent bossHealth = mHealth.get(bossEntityId);

        AnimationComponent animComp = mAnimation.get(bossEntityId);
        if (animComp.simpleAnimationComponent == null) return;
        FsmContext fsm = animComp.simpleAnimationComponent.fsmContext;

        // Если босс умер, принудительно включаем анимацию смерти и выходим
        if (bossHealth.isDead) {
            fsm.insert("to_idle", false);
            fsm.insert("to_prepare", false);
            fsm.insert("to_attack", false);
            fsm.insert("to_dead", true); // Строго по вашему JSON переменных
            return;
        }

        BossSingletonComponent bossAi = BossSingletonComponent.INSTANCE;
        float delta = world.getDelta();
        bossAi.stateTimer += delta;

        switch (bossAi.currentState) {
            case IDLE:
                // Выставляем предикаты строго по вашему JSON конфигу
                fsm.insert("to_idle", true);
                fsm.insert("to_prepare", false);
                fsm.insert("to_attack", false);
                fsm.insert("to_dead", false);

                // === ЛОГИКА ВЫКЛЮЧЕНИЯ ВСПЫШКИ ПО ТАЙМЕРУ ===
                if (bossAi.flashTimer > 0f) {
                    bossAi.flashTimer -= delta;
                    if (bossAi.flashTimer <= 0f) {
                        // Время вышло — возвращаем всё назад, скрывая белый слой
                        CustomOrthogonalTiledMapRenderer renderer = getWorld().getSystem(RenderSystem.class).getRenderer();
                        renderer.getLayer(TiledUiConstants.Layers.BACKGROUND_WHITE).setVisible(false);
                    }
                }

                // Спавним патроны "if_tree", если их нет на арене и у героя в кармане 0
                IntBag ammoItems = world.getAspectSubscriptionManager().get(Aspect.all(AmmoItemComponent.class)).getEntities();
                if (ammoItems.isEmpty() && HeroSingletonComponent.INSTANCE.ammo == 0) {
                    spawnAmmoIntent("spawn1");
                    spawnAmmoIntent("spawn2");
                }

                // Переход на фазу подготовки по времени отдыха босса
                if (bossAi.stateTimer >= bossAi.IDLE_DURATION) {
                    bossAi.currentState = BossSingletonComponent.State.PREPARE;
                    bossAi.stateTimer = 0f;
                }
                break;

            case PREPARE:
                // Переключаем в анимацию "prepare" (в JSON она зациклена через LOOP_PINGPONG, так что таймер тут идеален)
                fsm.insert("to_idle", false);
                fsm.insert("to_prepare", true);
                fsm.insert("to_attack", false);
                fsm.insert("to_dead", false);

                // Держим фазу подготовки фиксированные 5 секунд
                if (bossAi.stateTimer >= bossAi.PREPARE_DURATION) {
                    bossAi.currentState = BossSingletonComponent.State.ATTACK;
                    bossAi.stateTimer = 0f;
                }
                break;

            case ATTACK:
                // Включаем предикат атаки
                fsm.insert("to_idle", false);
                fsm.insert("to_prepare", false);
                fsm.insert("to_attack", true);
                fsm.insert("to_dead", false);

                // В JSON анимация "attack" имеет режим NORMAL (проигрывается один раз с 8 по 14 кадр).
                // Проверяем, завершилась ли она физически на экране.
                boolean isAttackAnimFinished = SimpleAnimatorUtils.isAnimationFinished(animComp.simpleAnimationComponent.animatorDynamicPart);

                if (isAttackAnimFinished) {
                    // Анимация удара завершилась — проверяем, спрятался ли наш герой в коробке
                    if (!HeroSingletonComponent.INSTANCE.isHiddenInBox) {
                        triggerGameOver();
                    }

                    // === ЛОГИКА ВКЛЮЧЕНИЯ ВСПЫШКИ И СМЕНЫ ФАЗЫ ===
                    // 1. Включаем белый слой экрана
                    CustomOrthogonalTiledMapRenderer renderer = getWorld().getSystem(RenderSystem.class).getRenderer();
                    renderer.getLayer(TiledUiConstants.Layers.BACKGROUND_WHITE).setVisible(true);

                    // 2. Заводим таймер удержания вспышки
                    bossAi.flashTimer = bossAi.FLASH_DURATION;

                    // Сбрасываем цикл босса обратно в покой
                    bossAi.currentState = BossSingletonComponent.State.IDLE;
                    bossAi.stateTimer = 0f;
                }
                break;
        }
    }

    private void spawnAmmoIntent(String zoneName) {
        int intentId = world.create();
        world.edit(intentId).create(SpawnObjectIntentComponent.class).targetZoneName = zoneName;
    }

    private void triggerGameOver() {
        Array<DialogueSingletonComponent.Phrase> phrases = DialogFactory.bossWin();
        phrases.add(new DialogueSingletonComponent.Phrase(
            Resources.Atlases.Objects.AVATAR_HERO,
            "ЭТУ ФРАЗУ ВИДНО НЕ БУДЕТ. FIXME!",
            () -> {
                int intentEntity = world.create();
                ChangeLevelIntentComponent intent = world.edit(intentEntity).create(ChangeLevelIntentComponent.class);
                intent.nextLevelKey = "LEVEL_1";
            }
        ));
        DialogueSingletonComponent.INSTANCE.start(phrases);
    }
}
