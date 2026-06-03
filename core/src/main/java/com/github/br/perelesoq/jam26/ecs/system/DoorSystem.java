package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.github.ashvard.gdx.simple.animation.component.SimpleAnimatorUtils;
import com.github.ashvard.gdx.simple.animation.fsm.FsmContext;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.animation.DoorAnimationType;
import com.github.br.perelesoq.jam26.ecs.component.DoorComponent;
import com.github.br.perelesoq.jam26.ecs.component.OpenDoorIntentComponent;
import com.github.br.perelesoq.jam26.ecs.component.audio.PlaySoundComponent;
import com.github.br.perelesoq.jam26.ecs.component.physics.RemoveFromPhysicsWorldComponent;
import com.github.br.perelesoq.jam26.ecs.component.ui.AnimationComponent;
import com.github.br.perelesoq.jam26.ecs.component.physics.PhysicsComponent;

public class DoorSystem extends BaseSystem {

    private EntitySubscription intentSubscription;
    private EntitySubscription doorSubscription;

    private ComponentMapper<OpenDoorIntentComponent> mIntent;
    private ComponentMapper<DoorComponent> mDoor;
    private ComponentMapper<AnimationComponent> mAnimation;
    private ComponentMapper<PhysicsComponent> mPhysics;
    private ComponentMapper<RemoveFromPhysicsWorldComponent> mRemovePhysics;

    protected ComponentMapper<PlaySoundComponent> mPlaySound;

    @Override
    protected void initialize() {
        // Подписка на запросы открытия дверей
        intentSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(OpenDoorIntentComponent.class));

        // Подписка на все двери в мире
        doorSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(DoorComponent.class, AnimationComponent.class));
    }

    @Override
    protected void processSystem() {
        IntBag intents = intentSubscription.getEntities();
        IntBag doors = doorSubscription.getEntities();

        // --- 1. ОБРАБОТКА СИГНАЛОВ НАЖАТИЯ НА КНОПКУ (НАМЕРЕНИЙ) ---
        for (int i = 0; i < intents.size(); i++) {
            int intentEntityId = intents.get(i);
            OpenDoorIntentComponent intent = mIntent.get(intentEntityId);

            // Перебираем все двери и ищем ту, у которой совпадает ID
            for (int j = 0; j < doors.size(); j++) {
                int doorEntityId = doors.get(j);
                DoorComponent door = mDoor.get(doorEntityId);

                if (door.doorId == intent.doorId && !door.isOpeningTriggered) {
                    door.isOpeningTriggered = true;

                    // Дергаем FSM анимации двери!
                    AnimationComponent animComp = mAnimation.get(doorEntityId);
                    if (animComp != null && animComp.simpleAnimationComponent != null) {
                        FsmContext fsm = animComp.simpleAnimationComponent.fsmContext;

                        // Переключаем предикаты стейт-машины двери на открытие
                        fsm.insert(DoorAnimationType.TransitionPredicate.IS_CLOSING, false);
                        fsm.insert(DoorAnimationType.TransitionPredicate.IS_OPENING, true);
                    }

                    // звук открытия двери
                    createPlaySound();
                }
            }

            // Намерение обработано, удаляем его из мира
            world.delete(intentEntityId);
        }

        // --- 2. КОНТРОЛЬ ЗАВЕРШЕНИЯ АНИМАЦИИ ОТКРЫТИЯ ---
        for (int j = 0; j < doors.size(); j++) {
            int doorEntityId = doors.get(j);
            DoorComponent door = mDoor.get(doorEntityId);

            // Если процесс открытия для этой двери еще не был запущен, пропускаем кадр
            if (!door.isOpeningTriggered) continue;

            AnimationComponent animComp = mAnimation.get(doorEntityId);
            if (animComp == null || animComp.simpleAnimationComponent == null) continue;

            FsmContext fsm = animComp.simpleAnimationComponent.fsmContext;
            String currentState = fsm.getCurrentState();

            // ПРЕДОХРАНИТЕЛЬ: Если стейт еще null (на самом первом кадре, пока AnimationSystem не дошла),
            // значит анимация даже не переключилась. Твердое тело оставляем и ждем следующего кадра.
            if (currentState == null) continue;

            // СТРОГАЯ И ТОЧНАЯ ПРОВЕРКА ПО ВАШЕМУ JSON:
            // Физика двери удаляется только тогда, когда FSM вышла из состояния "is_opening"
            // и переключилась в финальное статичное состояние "opened"!
            boolean isOpenedStateActive = DoorAnimationType.State.IS_OPENING.equals(currentState) &&
                SimpleAnimatorUtils.isAnimationFinished(animComp.simpleAnimationComponent.animatorDynamicPart);

            if (isOpenedStateActive && mPhysics.has(doorEntityId)) {
                PhysicsComponent physics = mPhysics.get(doorEntityId);

                if (physics != null && physics.item != null) {
                    // СОЗДАЕМ КОМАНДУ НА УДАЛЕНИЕ ИЗ JBUMP
                    int removeRequest = world.create();
                    RemoveFromPhysicsWorldComponent req = mRemovePhysics.create(removeRequest);
                    req.itemToRemove = physics.item; // Передаем ссылку на физическое тело
                }

                // Теперь спокойно удаляем компонент физики из двери в ECS
                mPhysics.remove(doorEntityId);

                // Сбрасываем триггер, задача выполнена
                door.isOpeningTriggered = false;
            }
        }
    }

    private void createPlaySound() {
        int soundRequestEntity = world.create();
        PlaySoundComponent playSound = mPlaySound.create(soundRequestEntity);
        playSound.soundName = Resources.Sound.BIG_DOOR;
        playSound.volume = 0.7f; // [10]
        playSound.pitch = 1.0f;
    }

}
