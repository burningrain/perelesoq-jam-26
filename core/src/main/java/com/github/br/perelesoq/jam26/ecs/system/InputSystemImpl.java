package com.github.br.perelesoq.jam26.ecs.system;

import com.badlogic.gdx.Input;
import com.artemis.ComponentMapper;
import com.github.br.perelesoq.jam26.ecs.component.*;
import com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.trigger.InteractionIntentComponent;
import com.github.br.perelesoq.jam26.ecs.system.base.input.AbstractInputSystem;
import com.github.br.perelesoq.jam26.ecs.system.base.input.GameAction;
import com.github.br.perelesoq.jam26.ecs.system.base.input.GameInputRegistry;
import com.github.br.perelesoq.jam26.ecs.system.base.input.MyControllerMapping;
import de.golfgl.gdx.controllers.mapping.MappedController;

public class InputSystemImpl extends AbstractInputSystem {

    private ComponentMapper<VelocityComponent> mVelocity;
    private ComponentMapper<JumpControlComponent> mJumpControl;
    private ComponentMapper<CharacterStateComponent> mState;
    private ComponentMapper<TransformComponent> transMapper;

    public InputSystemImpl() {
        super(new GameInputRegistry() {
            @Override
            protected void setupDefaultMappings() {
                bindKey(GameAction.MOVE_LEFT, Input.Keys.LEFT, Input.Keys.A);
                bindKey(GameAction.MOVE_RIGHT, Input.Keys.RIGHT, Input.Keys.D);
                bindKey(GameAction.JUMP, Input.Keys.SPACE);
                bindKey(GameAction.FIRE, Input.Keys.SHIFT_RIGHT, Input.Keys.ENTER);
                bindKey(GameAction.EXECUTE, Input.Keys.E);

                bindControllerButton(GameAction.MOVE_LEFT, MyControllerMapping.DPAD_LEFT);
                bindControllerButton(GameAction.MOVE_RIGHT, MyControllerMapping.DPAD_RIGHT);
                bindControllerButton(GameAction.JUMP, MyControllerMapping.BUTTON_A);
                bindControllerButton(GameAction.FIRE, MyControllerMapping.BUTTON_B);
                bindControllerButton(GameAction.EXECUTE, MyControllerMapping.BUTTON_X);
            }
        });
    }

    @Override
    protected void processGameAction(GameInputRegistry inputRegistry, MappedController mappedController) {
        int entityId = HeroSingletonComponent.INSTANCE.playerId;

        // Предохранитель на случай, если игрок еще не заспавнился
        if (entityId == -1 || !mVelocity.has(entityId) || !transMapper.has(entityId)) return;

        VelocityComponent velocity = mVelocity.get(entityId);
        JumpControlComponent jumpCtrl = mJumpControl.get(entityId);
        CharacterStateComponent state = mState.get(entityId);
        TransformComponent transform = transMapper.get(entityId); // Достаем трансформ сразу в начале

        // --- 1. ПРАВИЛЬНОЕ ГОРИЗОНТАЛЬНОЕ ДВИЖЕНИЕ И РАЗВОРOT ---
        float moveX = 0;
        if (inputRegistry.isActionPressed(GameAction.MOVE_LEFT, mappedController)) {
            moveX = -1f;
            transform.flipX = true; // Разворачиваем спрайт влево при движении влево
        } else if (inputRegistry.isActionPressed(GameAction.MOVE_RIGHT, mappedController)) {
            moveX = 1f;
            transform.flipX = false; // Разворачиваем спрайт вправо при движении вправо
        } else {
            moveX = 0f;
        }
        velocity.x = moveX * HeroSingletonComponent.INSTANCE.RUN_SPEED;

        // --- 2. ОБРАБОТКА СТАРТА ПРЫЖКА (ПРИ УДЕРЖАНИИ ИЛИ КЛИКЕ) ---

        // Если мы физически на земле, возвращаем готовность к прыжку
        if (state.onGround) {
            jumpCtrl.isJumpReady = true;
        }

        // Проверяем обычное зажатие (Pressed), а не одиночный клик (JustPressed)
        if (inputRegistry.isActionPressed(GameAction.JUMP, mappedController)) {

            // Если кнопка зажата, мы на земле и прыжок готов — стартуем!
            if (state.onGround && jumpCtrl.isJumpReady) {
                jumpCtrl.isJumping = true;
                jumpCtrl.isJumpReady = false;     // Мгновенно блокируем повторный старт до приземления
                jumpCtrl.jumpTimeCounter = 0f;    // Сбрасываем таймер Марио-взлета
                velocity.y = jumpCtrl.INITIAL_JUMP_FORCE; // Даем стартовый толчок
                state.onGround = false;           // Отрываемся от земли
            }

        } else {
            // --- 3. ОТПУСКАНИЕ КНОПКИ ПРЫЖКА ---
            // Если игрок физически отпустил кнопку посреди полета — обрубаем Марио-взлет
            jumpCtrl.isJumping = false;
        }

        // Проверяем нажатие взаимодействия
        if (inputRegistry.isActionJustPressed(GameAction.EXECUTE, mappedController)) {
            // Взводим намерение взаимодействовать в этом кадре
            world.edit(entityId).create(InteractionIntentComponent.class);
        } else {
            // Если кнопка не нажата, обязательно убираем компонент, чтобы намерение не залипло
            world.edit(entityId).remove(InteractionIntentComponent.class);
        }

        // --- 4. МЕХАНИКА СТРЕЛЬБЫ ---
        // Проверяем, подобрал ли герой оружие по сюжету
        if (HeroSingletonComponent.INSTANCE.hasWeapon) {
            if (inputRegistry.isActionJustPressed(GameAction.FIRE, mappedController)) {
                // ВЗВОДИМ ТАЙМЕР АНИМАЦИИ АТАКЫ
                state.attackAnimTimer = state.ATTACK_ANIM_DURATION;
                // Направление выстрела берем СТРОГО из TransformComponent!
                float direction = transform.flipX ? -1f : 1f;

                // Создаем сущность-интент (приказ) на спавн пули
                int spawnBulletEntity = world.create();
                SpawnBulletIntentComponent intent = world.edit(spawnBulletEntity).create(SpawnBulletIntentComponent.class);

                // Позиция спавна — вычисляем на основе transform.flipX
                intent.startX = transform.x + (transform.flipX ? -8f : 32f);
                intent.startY = transform.y + 8f; // На уровне груди пиксельного героя
                intent.dirX = direction;
            }
        }
    }

}
