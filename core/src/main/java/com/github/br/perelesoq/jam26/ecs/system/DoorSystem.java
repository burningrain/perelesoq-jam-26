package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.github.ashvard.gdx.simple.animation.component.SimpleAnimatorUtils;
import com.github.ashvard.gdx.simple.animation.fsm.FsmContext;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.ecs.component.door.CloseDoorIntentComponent;
import com.github.br.perelesoq.jam26.ecs.component.door.DoorComponent;
import com.github.br.perelesoq.jam26.ecs.component.door.OpenDoorIntentComponent;
import com.github.br.perelesoq.jam26.ecs.component.audio.PlaySoundComponent;
import com.github.br.perelesoq.jam26.ecs.component.physics.RemoveFromPhysicsWorldComponent;
import com.github.br.perelesoq.jam26.ecs.component.ui.AnimationComponent;
import com.github.br.perelesoq.jam26.ecs.component.physics.PhysicsComponent;
import com.github.br.perelesoq.jam26.ecs.component.physics.Hitbox; // Для восстановления физики

public class DoorSystem extends BaseSystem {

    private EntitySubscription openIntentSubscription;
    private EntitySubscription closeIntentSubscription; // Новая подписка
    private EntitySubscription doorSubscription;

    private ComponentMapper<OpenDoorIntentComponent> mOpenIntent;
    private ComponentMapper<CloseDoorIntentComponent> mCloseIntent; // Новый маппер
    private ComponentMapper<DoorComponent> mDoor;
    private ComponentMapper<AnimationComponent> mAnimation;
    private ComponentMapper<PhysicsComponent> mPhysics;
    private ComponentMapper<RemoveFromPhysicsWorldComponent> mRemovePhysics;

    protected ComponentMapper<PlaySoundComponent> mPlaySound;

    @Override
    protected void initialize() {
        openIntentSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(OpenDoorIntentComponent.class));

        closeIntentSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(CloseDoorIntentComponent.class));

        doorSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(DoorComponent.class, AnimationComponent.class));
    }

    @Override
    protected void processSystem() {
        IntBag openIntents = openIntentSubscription.getEntities();
        IntBag closeIntents = closeIntentSubscription.getEntities();
        IntBag doors = doorSubscription.getEntities();

        // --- 1. ОБРАБОТКА СИГНАЛОВ ОТКРЫТИЯ ---
        for (int i = 0; i < openIntents.size(); i++) {
            int intentEntityId = openIntents.get(i);
            OpenDoorIntentComponent intent = mOpenIntent.get(intentEntityId);

            for (int j = 0; j < doors.size(); j++) {
                int doorEntityId = doors.get(j);
                DoorComponent door = mDoor.get(doorEntityId);

                if (door.doorId == intent.doorId && !door.isOpeningTriggered) {
                    door.isOpeningTriggered = true;
                    door.isClosingTriggered = false; // Прерываем закрытие, если оно шло

                    AnimationComponent animComp = mAnimation.get(doorEntityId);
                    if (animComp != null && animComp.simpleAnimationComponent != null) {
                        FsmContext fsm = animComp.simpleAnimationComponent.fsmContext;
                        // Имена переменных строго по вашему JSON (camelCase)
                        fsm.insert("isClosing", false);
                        fsm.insert("isOpening", true);
                    }
                    createPlaySound();
                }
            }
            world.delete(intentEntityId);
        }

        // --- 2. ОБРАБОТКА СИГНАЛОВ ЗАКРЫТИЯ (НОВАЯ ЛОГИКА) ---
        for (int i = 0; i < closeIntents.size(); i++) {
            int intentEntityId = closeIntents.get(i);
            CloseDoorIntentComponent intent = mCloseIntent.get(intentEntityId);

            for (int j = 0; j < doors.size(); j++) {
                int doorEntityId = doors.get(j);
                DoorComponent door = mDoor.get(doorEntityId);

                if (door.doorId == intent.doorId && !door.isClosingTriggered) {
                    door.isClosingTriggered = true;
                    door.isOpeningTriggered = false; // Прерываем открытие, если шло

                    AnimationComponent animComp = mAnimation.get(doorEntityId);
                    if (animComp != null && animComp.simpleAnimationComponent != null) {
                        FsmContext fsm = animComp.simpleAnimationComponent.fsmContext;
                        // Имена переменных строго по вашему JSON (camelCase)
                        fsm.insert("isOpening", false);
                        fsm.insert("isClosing", true);
                    }
                    createPlaySound();
                }
            }
            world.delete(intentEntityId);
        }

        // --- 3. КОНТРОЛЬ СТЕЙТОВ И ФИЗИКИ ---
        for (int j = 0; j < doors.size(); j++) {
            int doorEntityId = doors.get(j);
            DoorComponent door = mDoor.get(doorEntityId);

            AnimationComponent animComp = mAnimation.get(doorEntityId);
            if (animComp == null || animComp.simpleAnimationComponent == null) continue;

            FsmContext fsm = animComp.simpleAnimationComponent.fsmContext;
            String currentState = fsm.getCurrentState();
            if (currentState == null) continue;

            boolean isAnimFinished = SimpleAnimatorUtils.isAnimationFinished(animComp.simpleAnimationComponent.animatorDynamicPart);

            // А: Логика завершения ОТКРЫТИЯ (уже была у вас)
            if (door.isOpeningTriggered && "is_opening".equals(currentState) && isAnimFinished) {
                if (mPhysics.has(doorEntityId)) {
                    PhysicsComponent physics = mPhysics.get(doorEntityId);
                    if (physics != null && physics.item != null) {
                        int removeRequest = world.create();
                        mRemovePhysics.create(removeRequest).itemToRemove = physics.item;
                    }
                    mPhysics.remove(doorEntityId);
                }
                door.isOpeningTriggered = false;
            }

            // Б: Логика завершения ЗАКРЫТИЯ (Новая логика)
            // Когда анимация "is_closing" полностью проигралась, стейт-машина перейдет обратно в "closed"
            if (door.isClosingTriggered && "is_closing".equals(currentState) && isAnimFinished) {

                if (!mPhysics.has(doorEntityId)) {
                    // Возвращаем PhysicsComponent двери в ECS мир!
                    PhysicsComponent physics = mPhysics.create(doorEntityId);

                    physics.hitbox = new Hitbox(15f, 43f, 0f, 0f, 0f, 0f);
                    physics.useGravity = false;
                }

                door.isClosingTriggered = false;
            }
        }
    }

    private void createPlaySound() {
        int soundRequestEntity = world.create();
        PlaySoundComponent playSound = mPlaySound.create(soundRequestEntity);
        playSound.soundName = Resources.Sound.BIG_DOOR;
        playSound.volume = 0.7f;
        playSound.pitch = 1.0f;
    }
}
