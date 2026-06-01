package com.github.br.perelesoq.jam26.ecs.system.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import de.golfgl.gdx.controllers.mapping.MappedController;

public abstract class GameInputRegistry {

    private final IntMap<IntArray> keyboardMappings = new IntMap<>();
    private final IntMap<IntArray> controllerMappings = new IntMap<>();

    // Храним состояния кнопок геймпада для определения "Just Pressed"
    private final IntMap<Boolean> lastFrameControllerStates = new IntMap<>();
    private final IntMap<Boolean> currentFrameControllerStates = new IntMap<>();

    public GameInputRegistry() {
        setupDefaultMappings();
    }

    protected abstract void setupDefaultMappings();

    public void bindKey(GameAction action, int... keys) {
        if (!keyboardMappings.containsKey(action.ordinal())) {
            keyboardMappings.put(action.ordinal(), new IntArray());
        }
        keyboardMappings.get(action.ordinal()).addAll(keys);
    }

    public void bindControllerButton(GameAction action, int... buttons) {
        if (!controllerMappings.containsKey(action.ordinal())) {
            controllerMappings.put(action.ordinal(), new IntArray());
        }
        controllerMappings.get(action.ordinal()).addAll(buttons);
    }

    /**
     * Вызывать в САМОМ НАЧАЛЕ кадра (например, в начале processSystem у InputSystem)
     */
    public void tick(MappedController mappedController) {
        lastFrameControllerStates.clear();
        for (IntMap.Entry<Boolean> entry : currentFrameControllerStates.entries()) {
            lastFrameControllerStates.put(entry.key, entry.value);
        }
        currentFrameControllerStates.clear();

        if (mappedController != null) {
            for (IntMap.Entry<IntArray> entry : controllerMappings.entries()) {
                IntArray buttons = entry.value;
                for (int i = 0; i < buttons.size; i++) {
                    int btn = buttons.get(i);
                    currentFrameControllerStates.put(btn, mappedController.isButtonPressed(btn));
                }
            }
        }
    }

    /**
     * Проверка: удерживается ли кнопка прямо сейчас (для прыжка в процессе)
     */
    public boolean isActionPressed(GameAction action, MappedController mappedController) {
        // 1. Проверяем клавиатуру
        if (keyboardMappings.containsKey(action.ordinal())) {
            IntArray keys = keyboardMappings.get(action.ordinal());
            for (int i = 0; i < keys.size; i++) {
                if (Gdx.input.isKeyPressed(keys.get(i))) return true;
            }
        }
        // 2. Проверяем геймпад
        if (mappedController != null && controllerMappings.containsKey(action.ordinal())) {
            IntArray buttons = controllerMappings.get(action.ordinal());
            for (int i = 0; i < buttons.size; i++) {
                if (currentFrameControllerStates.get(buttons.get(i), false)) return true;
            }
        }
        return false;
    }

    /**
     * Проверка: была ли кнопка нажата СТРОГО в этом кадре (для старта прыжка)
     */
    public boolean isActionJustPressed(GameAction action, MappedController mappedController) {
        // 1. Клавиатура
        if (keyboardMappings.containsKey(action.ordinal())) {
            IntArray keys = keyboardMappings.get(action.ordinal());
            for (int i = 0; i < keys.size; i++) {
                if (Gdx.input.isKeyJustPressed(keys.get(i))) return true;
            }
        }
        // 2. Геймпад
        if (mappedController != null && controllerMappings.containsKey(action.ordinal())) {
            IntArray buttons = controllerMappings.get(action.ordinal());
            for (int i = 0; i < buttons.size; i++) {
                int btn = buttons.get(i);
                boolean now = currentFrameControllerStates.get(btn, false);
                boolean before = lastFrameControllerStates.get(btn, false);
                if (now && !before) return true; // Нажали только что
            }
        }
        return false;
    }

}
