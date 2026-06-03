package com.github.br.perelesoq.jam26.ecs.component.trigger;

import com.artemis.PooledComponent;
import com.github.br.perelesoq.jam26.ecs.system.base.trigger.TriggerAction;

public class TriggerComponent extends PooledComponent {

    public TriggerAction action;

    // Требуется ли нажимать кнопку "E" (EXECUTE), или триггер срабатывает автоматически при наступлении
    public boolean requiresExecution = false;

    // Внутреннее состояние: находится ли сейчас игрок внутри этой зоны
    public boolean isPlayerInside = false;
    public boolean isNotReused = false;

    @Override
    protected void reset() {
        requiresExecution = false;
        isPlayerInside = false;
        action = null;
        isNotReused = false;
    }

}
