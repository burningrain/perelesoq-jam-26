package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.github.br.perelesoq.jam26.ecs.component.BulletComponent;
import com.github.br.perelesoq.jam26.ecs.component.TransformComponent;
import com.github.br.perelesoq.jam26.ecs.component.VelocityComponent;

public class BulletSystem extends IteratingSystem {

    private ComponentMapper<BulletComponent> mBullet;
    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<VelocityComponent> mVelocity;

    public BulletSystem() {
        super(Aspect.all(BulletComponent.class, TransformComponent.class, VelocityComponent.class));
    }

    @Override
    protected void process(int entityId) {
        BulletComponent bullet = mBullet.get(entityId);

        // Уменьшаем таймер жизни пули
        bullet.lifeTime -= world.getDelta();
        if (bullet.lifeTime <= 0) {
            world.delete(entityId); // Удаляем пулю, если улетела слишком далеко
            return;
        }

        // Скорость полета пули (например, 250 пикселей в секунду)
        // Само движение хитбокса произойдет чуть позже в вашей PhysicsSystem
        VelocityComponent velocity = mVelocity.get(entityId);
        // Направление полета уже заложено в знак velocity.x при спавне
    }
}
