package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.github.ashvard.gdx.simple.animation.fsm.FsmContext;
import com.github.br.perelesoq.jam26.animation.AnimationFactory;
import com.github.br.perelesoq.jam26.animation.HeroAnimationType;
import com.github.br.perelesoq.jam26.ecs.component.ui.AnimationComponent;
import com.github.br.perelesoq.jam26.ecs.component.CharacterStateComponent;
import com.github.br.perelesoq.jam26.ecs.component.TransformComponent;
import com.github.br.perelesoq.jam26.ecs.component.VelocityComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent;

public class HeroAnimationStateSystem extends IteratingSystem {

    private ComponentMapper<TransformComponent> transformMapper;
    private ComponentMapper<AnimationComponent> mAnimation;
    private ComponentMapper<VelocityComponent> mVelocity;
    private ComponentMapper<CharacterStateComponent> mState;

    public HeroAnimationStateSystem() {
        super(Aspect.all(TransformComponent.class, AnimationComponent.class, VelocityComponent.class, CharacterStateComponent.class));
    }

    @Override
    protected void process(int entityId) {
        boolean hasWeapon = HeroSingletonComponent.INSTANCE.hasWeapon;
        AnimationComponent animComp = mAnimation.get(entityId);

        if (animComp.simpleAnimationComponent == null) return;

        VelocityComponent velocity = mVelocity.get(entityId);
        CharacterStateComponent state = mState.get(entityId);
        FsmContext fsmContext = animComp.simpleAnimationComponent.fsmContext;

        // 1. Полностью очищаем все предикаты текущего кадра
        AnimationFactory.resetHeroAnimationContext(fsmContext);

        // Обновляем таймер атаки
        if (state.attackAnimTimer > 0f) {
            state.attackAnimTimer -= world.getDelta();
        }

        // Меняем направление ТОЛЬКО если скорость отлична от нуля
        TransformComponent transformComponent = transformMapper.get(entityId);
        if (velocity.x < -0.1f) {
            transformComponent.flipX = true;
        } else if (velocity.x > 0f) {
            transformComponent.flipX = false;
        }

        // 2. РАСЧЕТ ТЕКУЩЕГО СОСТОЯНИЯ НА ОСНОВЕ ФИЗИКИ И ТАЙМЕРОВ

        // ПРИОР ИТЕТ АТАК И: Если герой стреляет прямо сейчас, включаем анимацию атаки
        if (hasWeapon && state.attackAnimTimer > 0f) {
            // Переключаем FSM на анимацию стрельбы перед собой
            fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_ATTACK_DOWN, true);
        }
        // ИНАЧЕ включаем стандартные перемещения
        else if (!state.onGround) {
            // --- ПЕРСОНАЖ В ВОЗДУХЕ (ПРЫЖОК / ПАДЕНИЕ) ---
            if (hasWeapon) {
                fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_WEAPON_JUMP, true);
            } else {
                fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_JUMP, true);
            }
        } else {
            // --- ПЕРСОНАЖ НА ЗЕМЛЕ ---
            if (Math.abs(velocity.x) > 0.1f) {
                // Герой бежит/идет
                if (hasWeapon) {
                    fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_WEAPON_WALKING, true);
                } else {
                    fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_WALKING, true);
                }
            } else {
                // Герой стоит на месте
                if (hasWeapon) {
                    fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_WEAPON_IDLE, true);
                } else {
                    fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_IDLE, true);
                }
            }
        }
    }
}
