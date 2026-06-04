package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.github.br.perelesoq.jam26.ecs.EntityFactory;
import com.github.br.perelesoq.jam26.ecs.component.SpawnObjectIntentComponent;
import com.github.br.perelesoq.jam26.ecs.component.SpawnZoneComponent;
import com.github.br.perelesoq.jam26.ecs.component.TransformComponent;

public class ObjectSpawnerSystem extends IteratingSystem {

    private ComponentMapper<SpawnObjectIntentComponent> mIntent;
    private ComponentMapper<SpawnZoneComponent> mZone;
    private ComponentMapper<TransformComponent> mTransform;

    public ObjectSpawnerSystem() {
        super(Aspect.all(SpawnObjectIntentComponent.class));
    }

    @Override
    protected void process(int intentEntityId) {
        String targetZone = mIntent.get(intentEntityId).targetZoneName;

        // Ищем все невидимые зоны спавна в мире
        IntBag zoneEntities = world.getAspectSubscriptionManager()
            .get(Aspect.all(SpawnZoneComponent.class, TransformComponent.class)).getEntities();

        EntityFactory entityFactory = world.getSystem(EntityFactory.class);

        for (int i = 0; i < zoneEntities.size(); i++) {
            int zoneId = zoneEntities.get(i);
            SpawnZoneComponent zone = mZone.get(zoneId);

            // Если имя зоны совпадает или приказ отправлен для всех зон ("ANY")
            if (zone.spawnZoneName.equals(targetZone) || "ANY".equals(targetZone)) {
                TransformComponent zoneTrans = mTransform.get(zoneId);

                // Просим фабрику заспавнить "if_tree" внутри этой зоны
                entityFactory.spawnIfTreeInZone(zone, zoneTrans);
            }
        }

        // Приказ выполнен — удаляем сущность-интент
        world.delete(intentEntityId);
    }
}
