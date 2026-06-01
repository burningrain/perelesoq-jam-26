package com.github.br.perelesoq.jam26.ecs;

import com.artemis.*;
import com.github.br.perelesoq.jam26.animation.AnimationFactory;
import com.github.br.perelesoq.jam26.ecs.component.AnimationComponent;
import com.github.br.perelesoq.jam26.ecs.component.RenderComponent;
import com.github.br.perelesoq.jam26.ecs.component.TransformComponent;

public class EntityFactory extends BaseSystem {

    private Archetype playerArchetype;

    protected ComponentMapper<TransformComponent> transformMapper;
    protected ComponentMapper<RenderComponent> renderMapper;
    protected ComponentMapper<AnimationComponent> animationMapper;

    @Override
    protected void initialize() {
        // Отключаем ежекадровое обновление, так как этот класс — просто фабрика
        setEnabled(false);

        playerArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(AnimationComponent.class)
            .add(RenderComponent.class)
            .build(world);
    }

    public int createPlayer(int x, int y) {
        int id = world.create(playerArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        AnimationComponent animationComponent = animationMapper.get(id);
        animationComponent.simpleAnimationComponent = AnimationFactory.createHero();

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.textureRegion = animationComponent.simpleAnimationComponent.animatorDynamicPart.currentFrame;
        renderComponent.layer = "objects";

        return id;
    }

    @Override
    protected void processSystem() {
        // Метод пустой, так как система passive (setEnabled(false))
    }

}
