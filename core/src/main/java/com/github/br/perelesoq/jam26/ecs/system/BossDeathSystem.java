package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.github.ashvard.gdx.simple.animation.fsm.FsmContext;
import com.github.br.perelesoq.jam26.dialogs.CinematicFactory;
import com.github.br.perelesoq.jam26.ecs.component.HealthComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.cinematic.CinematicSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.ui.AnimationComponent;

public class BossDeathSystem extends IteratingSystem {

    private final CinematicFactory cinematicFactory;

    private ComponentMapper<HealthComponent> mHealth;
    private ComponentMapper<AnimationComponent> mAnimation;

    public BossDeathSystem(CinematicFactory cinematicFactory) {
        // Ищем объекты с компонентом здоровья, которые при этом анимированы (наш Босс)
        super(Aspect.all(HealthComponent.class, AnimationComponent.class));
        this.cinematicFactory = cinematicFactory;
    }

    @Override
    protected void process(int entityId) {
        HealthComponent health = mHealth.get(entityId);

        if (health.isDead) {
            AnimationComponent animComp = mAnimation.get(entityId);
            if (animComp.simpleAnimationComponent != null) {
                FsmContext fsm = animComp.simpleAnimationComponent.fsmContext;

                // Дергаем предикат смерти босса для его анимационного JSON
                // Подставьте имя предиката из JSON анимации вашего босса (например, "is_death" или "isDead")
                fsm.insert("is_death", true);
            }

            // Отключаем обработку, чтобы не дергать анимацию каждый кадр
            mHealth.remove(entityId);
            world.delete(entityId);

            Array<CinematicSingletonComponent.CinematicStep> script = cinematicFactory.bossIsDeadCinematic();
            cinematicFactory.start(script);
        }
    }

}
