package com.github.br.perelesoq.jam26.ecs.system.base.ui;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.github.br.perelesoq.jam26.ecs.component.ui.render.RenderComponent;

public class RenderFlashSystem extends IteratingSystem {

    private ComponentMapper<RenderComponent> mRender;

    public RenderFlashSystem() {
        // Система обрабатывает только те объекты, которые сейчас мерцают белым
        super(Aspect.all(RenderComponent.class));
    }

    @Override
    protected void process(int entityId) {
        RenderComponent render = mRender.get(entityId);

        if (render.flashTimer > 0f) {
            render.flashTimer -= world.getDelta();

            if (render.flashTimer < 0f) {
                render.flashTimer = 0f; // Жестко глушим таймер в ноль, когда время вышло
            }
        }
    }

}
