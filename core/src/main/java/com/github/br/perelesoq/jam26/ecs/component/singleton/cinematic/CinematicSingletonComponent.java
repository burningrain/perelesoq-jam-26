package com.github.br.perelesoq.jam26.ecs.component.singleton.cinematic;

import com.artemis.Component;
import com.badlogic.gdx.utils.Array;

public class CinematicSingletonComponent extends Component {
    public static final CinematicSingletonComponent INSTANCE = new CinematicSingletonComponent();

    public boolean isActive = false; // Включен ли "режим кино" прямо сейчас

    // Список шагов (команд) кат-сцены
    public final Array<CinematicStep> steps = new Array<>();
    private int currentStepIndex = 0;

    /**
     * Интерфейс для одного шага кат-сцены
     */
    public interface CinematicStep {
        /**
         * Вызывается один раз при старте шага
         */
        void onStart(com.artemis.World world);

        /**
         * Вызывается каждый кадр. Возвращает true, когда шаг полностью завершен
         */
        boolean onUpdate(com.artemis.World world, float delta);
    }

    public void start(Array<CinematicStep> newSteps) {
        this.steps.clear();
        this.steps.addAll(newSteps);
        this.currentStepIndex = 0;
        this.isActive = true;
    }

    public CinematicStep getCurrentStep() {
        if (!isActive || currentStepIndex >= steps.size) return null;
        return steps.get(currentStepIndex);
    }

    public void nextStep() {
        currentStepIndex++;
        if (currentStepIndex >= steps.size) {
            isActive = false; // Все шаги выполнены, режим кино выключается
        }
    }
}
