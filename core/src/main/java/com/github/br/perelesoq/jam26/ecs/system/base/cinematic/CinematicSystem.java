package com.github.br.perelesoq.jam26.ecs.system.base.cinematic;

import com.artemis.BaseSystem;
import com.github.br.perelesoq.jam26.ecs.component.singleton.cinematic.CinematicSingletonComponent;

public class CinematicSystem extends BaseSystem {

    private boolean isStepInitialized = false;

    @Override
    protected void processSystem() {
        CinematicSingletonComponent cinematic = CinematicSingletonComponent.INSTANCE;

        if (!cinematic.isActive) {
            isStepInitialized = false;
            return;
        }

        CinematicSingletonComponent.CinematicStep currentStep = cinematic.getCurrentStep();
        if (currentStep == null) {
            cinematic.isActive = false;
            return;
        }

        // Вызываем инициализацию шага один раз
        if (!isStepInitialized) {
            currentStep.onStart(world);
            isStepInitialized = true;
        }

        // Каждый кадр обновляем шаг. Если он рапортует, что завершен — переходим к следующему
        boolean isFinished = currentStep.onUpdate(world, world.getDelta());
        if (isFinished) {
            cinematic.nextStep();
            isStepInitialized = false; // Сбрасываем для следующего шага
        }
    }
}
