package com.github.br.perelesoq.jam26.ecs.component.singleton;

import com.artemis.Component;

public class BossSingletonComponent extends Component {
    public static final BossSingletonComponent INSTANCE = new BossSingletonComponent();


    public enum State {
        IDLE,
        PREPARE,
        ATTACK
    }

    public State currentState = State.IDLE;
    public float stateTimer = 0f;

    public float flashTimer;

    // Настройки времени для фаз
    public final float IDLE_DURATION = 2f;      // Сколько отдыхает
    public final float PREPARE_DURATION = 4f;   // Настраиваемое время подготовки (5 сек)

    public final float FLASH_DURATION = 0.15f; // Длительность вспышки

    public void reset() {
        currentState = State.IDLE;
        stateTimer = 0f;
        flashTimer = 0f;
    }
}
