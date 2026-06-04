package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.Aspect;
import com.github.br.perelesoq.jam26.ecs.EntityFactory;
import com.github.br.perelesoq.jam26.ecs.component.SpawnBulletIntentComponent;

public class BulletSpawnerSystem extends com.artemis.systems.IteratingSystem {

    public BulletSpawnerSystem() {
        super(Aspect.all(SpawnBulletIntentComponent.class));
    }

    @Override
    protected void process(int entityId) {
        SpawnBulletIntentComponent intent = world.getMapper(SpawnBulletIntentComponent.class).get(entityId);

        // Вызываем фабрику для создания физической пули
        world.getSystem(EntityFactory.class).createBullet(intent.startX, intent.startY, intent.dirX);

        // Приказ выполнен — удаляем сущность интента
        world.delete(entityId);
    }

}
