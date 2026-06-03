package com.github.br.perelesoq.jam26.ecs.component.ui;

import com.artemis.PooledComponent;
import com.github.ashvard.gdx.simple.animation.component.SimpleAnimationComponent;

public class AnimationComponent extends PooledComponent {

    public transient SimpleAnimationComponent simpleAnimationComponent;

    public AnimationComponent(SimpleAnimationComponent simpleAnimationComponent) {
        this.simpleAnimationComponent = simpleAnimationComponent;
    }

    public AnimationComponent(){}

    @Override
    protected void reset() {
        simpleAnimationComponent = null;
    }

}
