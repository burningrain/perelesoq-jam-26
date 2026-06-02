package com.github.br.perelesoq.jam26.ecs.system.ui;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.ashvard.gdx.simple.animation.SimpleAnimation;
import com.github.ashvard.gdx.simple.animation.SimpleAnimationSystem;
import com.github.br.perelesoq.jam26.ecs.component.AnimationComponent;
import com.github.br.perelesoq.jam26.ecs.component.render.RenderComponent;

@All({AnimationComponent.class, RenderComponent.class})
public class AnimationSystem extends IteratingSystem {

    private final SimpleAnimationSystem simpleAnimationSystem = new SimpleAnimationSystem();

    protected ComponentMapper<AnimationComponent> animationMapper;
    protected ComponentMapper<RenderComponent> textureRegionComponentMapper;

    @Override
    protected void process(int entityId) {
        AnimationComponent animationComponent = animationMapper.get(entityId);
        RenderComponent textureRegionComponent = textureRegionComponentMapper.get(entityId);

        if (animationComponent.simpleAnimationComponent == null) {
            throw new GdxRuntimeException("simpleAnimationComponent was not set for entityId=" + entityId);
        }

        simpleAnimationSystem.update(Gdx.graphics.getDeltaTime(), animationComponent.simpleAnimationComponent);
        textureRegionComponent.textureRegion = animationComponent.simpleAnimationComponent.animatorDynamicPart.currentFrame;
    }

    public void addAnimation(SimpleAnimation simpleAnimation) {
        simpleAnimationSystem.addAnimation(simpleAnimation);
    }

    public void unload(String title) {
        simpleAnimationSystem.unload(title);
    }

    @Override
    public void dispose() {
        simpleAnimationSystem.dispose();
    }

}
