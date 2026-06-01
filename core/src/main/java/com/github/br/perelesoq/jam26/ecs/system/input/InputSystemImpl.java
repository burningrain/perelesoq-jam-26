package com.github.br.perelesoq.jam26.ecs.system.input;

import com.badlogic.gdx.Input;
import com.artemis.ComponentMapper;
import com.github.br.perelesoq.jam26.ecs.component.CharacterStateComponent;
import com.github.br.perelesoq.jam26.ecs.component.JumpControlComponent;
import com.github.br.perelesoq.jam26.ecs.component.VelocityComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent;
import de.golfgl.gdx.controllers.mapping.MappedController;

public class InputSystemImpl extends AbstractInputSystem {

    private static final float RUN_SPEED = 90f; // 40f слишком медленно для экрана 320, персонаж будет ползти
    private ComponentMapper<VelocityComponent> mVelocity;
    private ComponentMapper<JumpControlComponent> mJumpControl;
    private ComponentMapper<CharacterStateComponent> mState;

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
        if (entityId == 0 || !mVelocity.has(entityId)) return;

        VelocityComponent velocity = mVelocity.get(entityId);
        JumpControlComponent jumpCtrl = mJumpControl.get(entityId);
        CharacterStateComponent state = mState.get(entityId);

        // --- 1. ПРАВИЛЬНОЕ ГОРИЗОНТАЛЬНОЕ ДВИЖЕНИЕ ---
        float moveX = 0;
        if (inputRegistry.isActionPressed(GameAction.MOVE_LEFT, mappedController)) {
            moveX = -1f;
        } else if (inputRegistry.isActionPressed(GameAction.MOVE_RIGHT, mappedController)) {
            moveX = 1f;
        } else {
            moveX = 0f;
        }
        velocity.x = moveX * RUN_SPEED;

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
                jumpCtrl.jumpTimeCounter = 0f;  // Сбрасываем таймер Марио-взлета
                velocity.y = jumpCtrl.INITIAL_JUMP_FORCE; // Даем стартовый толчок
                state.onGround = false;         // Отрываемся от земли
            }

        } else {
            // --- 3. ОТПУСКАНИЕ КНОПКИ ПРЫЖКА ---
            // Если игрок физически отпустил кнопку посреди полета — обрубаем Марио-взлет
            jumpCtrl.isJumping = false;
        }
    }
}
