package com.github.br.perelesoq.jam26.ecs.system.trigger;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.trigger.InteractionIntentComponent;
import com.github.br.perelesoq.jam26.ecs.component.trigger.OverlappedThisFrameComponent;
import com.github.br.perelesoq.jam26.ecs.component.trigger.TriggerComponent;

public class TriggerSystem extends IteratingSystem {

    protected ComponentMapper<TriggerComponent> mTrigger;
    protected ComponentMapper<OverlappedThisFrameComponent> mOverlapped;
    protected ComponentMapper<InteractionIntentComponent> mIntent;

    public TriggerSystem() {
        super(Aspect.all(TriggerComponent.class));
    }

    @Override
    protected void process(int triggerEntityId) {
        int playerEntityId = HeroSingletonComponent.INSTANCE.playerId;
        if (playerEntityId == 0) return;

        TriggerComponent trigger = mTrigger.get(triggerEntityId);

        // jbump сообщил нам на этом кадре, что игрок стоит в триггере?
        boolean isCurrentlyOverlapping = mOverlapped.has(triggerEntityId);

        // --- ЛОГИКА ВХОДА / ВЫХОДА ---
        if (isCurrentlyOverlapping && !trigger.isPlayerInside) {
            trigger.isPlayerInside = true;
            if (trigger.action != null) trigger.action.onEnter(playerEntityId, triggerEntityId);

            // Если триггер автоматический (сирена), сразу выполняем действие
            if (!trigger.requiresExecution && trigger.action != null) {
                trigger.action.onExecute(playerEntityId, triggerEntityId);
            }
        }
        else if (!isCurrentlyOverlapping && trigger.isPlayerInside) {
            trigger.isPlayerInside = false;
            if (trigger.action != null) trigger.action.onExit(playerEntityId, triggerEntityId);
        }

        // --- ЛОГИКА НАЖАТИЯ КНОПКИ ВЗАИМОДЕЙСТВИЯ (Через намерение) ---
        if (trigger.isPlayerInside && trigger.requiresExecution) {
            // Проверяем, появилось ли у игрока намерение нажать "E" в этом кадре
            if (mIntent.has(playerEntityId)) {
                if (trigger.action != null) {
                    trigger.action.onExecute(playerEntityId, triggerEntityId);
                }
                // Удаляем намерение сразу после успешного срабатывания,
                // чтобы одно нажатие случайно не активировало два триггера подряд
                world.edit(playerEntityId).remove(InteractionIntentComponent.class);
            }
        }
    }

}
