package com.github.br.perelesoq.jam26.ecs.system.base.input;

import com.artemis.BaseSystem;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import de.golfgl.gdx.controllers.mapping.MappedController;

public abstract class AbstractInputSystem extends BaseSystem {

    private final MyControllerMapping myControllerMapping;
    private MappedController mappedController;
    private Controller lastActiveController;

    private final GameInputRegistry inputRegistry;

    //  ФЛАГ ПАУЗЫ ДЛЯ ИГРОВОГО ПРОЦЕССА
    private boolean isGameplayInputPaused = false;

    public AbstractInputSystem(GameInputRegistry gameInputRegistry) {
        this.inputRegistry = gameInputRegistry;
        myControllerMapping = new MyControllerMapping();

        // Пытаемся инициализировать контроллер, если он уже подключен
        checkAndRefreshController();
    }

    /**
     * Метод проверяет, изменился ли статус подключения геймпада.
     * Если геймпад отключили или подключили новый — он пересоздает MappedController.
     */
    private void checkAndRefreshController() {
        Controller currentController = Controllers.getCurrent();

        // Если физический контроллер изменился (отключили, или подключили вместо него другой)
        if (currentController != lastActiveController) {
            lastActiveController = currentController;
            if (currentController != null) {
                // Инициализируем MappedController с новым активным геймпадом
                mappedController = new MappedController(currentController, myControllerMapping);
            } else {
                mappedController = null;
            }
        }
    }

    public void setGameplayInputPaused(boolean paused) {
        this.isGameplayInputPaused = paused;
    }

    @Override
    protected void processSystem() {
        // Эти две строки теперь гарантированно выполняются КАЖДЫЙ кадр,
        // сбрасывая Just Pressed состояния геймпада вовремя!
        checkAndRefreshController();
        inputRegistry.tick(mappedController);

        // Логику выполняем только если нет паузы диалога
        if (!isGameplayInputPaused) {
            processGameAction(inputRegistry, mappedController);
        }
    }

    protected abstract void processGameAction(GameInputRegistry inputRegistry, MappedController mappedController);

    public boolean isActionJustPressed(GameAction gameAction) {
        return inputRegistry.isActionJustPressed(gameAction, mappedController);
    }

    public boolean isAnyActionJustPressed() {
        for (GameAction gameAction : GameAction.values()) {
            if (isActionJustPressed(gameAction)) {
                return true;
            }
        }

        return false;
    }

    public GameInputRegistry getInputRegistry() {
        return inputRegistry;
    }

    public MappedController getController() {
        return mappedController;
    }
}
