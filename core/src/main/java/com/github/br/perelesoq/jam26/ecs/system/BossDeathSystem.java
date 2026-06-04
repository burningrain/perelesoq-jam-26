package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.github.ashvard.gdx.simple.animation.fsm.FsmContext;
import com.github.br.perelesoq.jam26.dialogs.CinematicFactory;
import com.github.br.perelesoq.jam26.ecs.component.ChangeLevelIntentComponent;
import com.github.br.perelesoq.jam26.ecs.component.HealthComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.cinematic.CinematicSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.cinematic.DelayStep;
import com.github.br.perelesoq.jam26.ecs.component.ui.AnimationComponent;
import com.github.br.perelesoq.jam26.screen.Screens;

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
            script.add(new CinematicSingletonComponent.CinematicStep() {
                @Override
                public void onStart(World world) {
                    int intentEntity = world.create();
                    ChangeLevelIntentComponent intent = world.edit(intentEntity).create(ChangeLevelIntentComponent.class);
                    intent.nextLevelKey = "MAIN";
                }

                @Override
                public boolean onUpdate(World world, float delta) {
                    return true;
                }
            });
            cinematicFactory.start(script);
        }
    }

}
