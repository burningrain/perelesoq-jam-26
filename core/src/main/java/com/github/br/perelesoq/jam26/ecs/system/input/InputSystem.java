package com.github.br.perelesoq.jam26.ecs.system.input;

import com.artemis.BaseSystem;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import de.golfgl.gdx.controllers.mapping.MappedController;

public class InputSystem extends BaseSystem {

    private MyControllerMapping myControllerMapping;
    private MappedController mappedController;
    private Controller lastActiveController;

    @Override
    protected void initialize() {
        // Создаем маппинг один раз при старте системы
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

    @Override
    protected void processSystem() {
        // КРИТИЧЕСКИЙ ШАГ: Каждый кадр проверяем, на месте ли геймпад
        checkAndRefreshController();

        // Если геймпадов нет, система просто ничего не делает в этом кадре
        if (mappedController == null) {
            return;
        }

        // 1. Опрашиваем оси по новым константам Xbox
        float moveX = mappedController.getConfiguredAxisValue(MyControllerMapping.AXIS_LEFT_X);
        float moveY = mappedController.getConfiguredAxisValue(MyControllerMapping.AXIS_LEFT_Y);

        // Пример мертвой зоны для стиков
        if (moveX != 0) {
            System.out.println("moveX: " + moveX);
        }
        if (moveY != 0) {
            System.out.println("moveY: " + moveY);
        }

        // 2. Опрашиваем кнопки (Прыжок на A)
        if (mappedController.isButtonPressed(MyControllerMapping.BUTTON_A)) {
            System.out.println("BUTTON_A");
        }

        // Атака на X
        if (mappedController.isButtonPressed(MyControllerMapping.BUTTON_X)) {
            System.out.println("BUTTON_X");
        }

        // Атака на X
        if (mappedController.isButtonPressed(MyControllerMapping.BUTTON_B)) {
            System.out.println("BUTTON_B");
        }

        // Атака на X
        if (mappedController.isButtonPressed(MyControllerMapping.BUTTON_Y)) {
            System.out.println("BUTTON_Y");
        }

        // Атака на X
        if (mappedController.isButtonPressed(MyControllerMapping.DPAD_LEFT)) {
            System.out.println("DPAD_LEFT");
        }

        // Атака на X
        if (mappedController.isButtonPressed(MyControllerMapping.DPAD_RIGHT)) {
            System.out.println("DPAD_RIGHT");
        }

        // Атака на X
        if (mappedController.isButtonPressed(MyControllerMapping.DPAD_DOWN)) {
            System.out.println("DPAD_DOWN");
        }

        // Атака на X
        if (mappedController.isButtonPressed(MyControllerMapping.DPAD_UP)) {
            System.out.println("DPAD_UP");
        }

    }
}
