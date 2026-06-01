package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.dongbat.jbump.*;
import com.github.br.perelesoq.jam26.ecs.component.*;

public class PhysicsSystem extends IteratingSystem {

    // Константа гравитации под разрешение 320x180 (направлена вниз, поэтому отрицательная)
    private final float GRAVITY = -450f;

    // Инкапсулированный мир jbump (размер ячейки 32f совпадает с размером тайлов/героя)
    private final World<Integer> jbumpWorld;

    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<PhysicsComponent> mPhysics;
    private ComponentMapper<VelocityComponent> mVelocity;
    private ComponentMapper<JumpControlComponent> mJumpControl;
    private ComponentMapper<CharacterStateComponent> mState;

    public PhysicsSystem() {
        super(Aspect.all(TransformComponent.class, PhysicsComponent.class, VelocityComponent.class, CharacterStateComponent.class));
        this.jbumpWorld = new World<>(32f);
    }

    /**
     * Автоматически вызывается Artemis, когда у сущности появляется PhysicsComponent.
     * Отвечает за чистый перехват "Добавления" (вместо ручных очередей).
     */
    @Override
    protected void inserted(int entityId) {
        PhysicsComponent physics = mPhysics.get(entityId);
        TransformComponent transform = mTransform.get(entityId);

        // Создаем jbump Item и сразу связываем его с ID сущности
        physics.item = new Item<>(entityId);
        jbumpWorld.add(physics.item, transform.x, transform.y, physics.width, physics.height);
    }

    /**
     * Автоматически вызывается Artemis, когда сущность удаляется из ECS мира (world.delete(id)).
     * Отвечает за чистый перехват "Удаления".
     */
    @Override
    protected void removed(int entityId) {
        PhysicsComponent physics = mPhysics.get(entityId);
        if (physics != null && physics.item != null) {
            jbumpWorld.remove(physics.item);
        }
    }

    @Override
    protected void process(int entityId) {
        float deltaTime = world.getDelta();

        TransformComponent transform = mTransform.get(entityId);
        PhysicsComponent physics = mPhysics.get(entityId);
        VelocityComponent velocity = mVelocity.get(entityId);
        CharacterStateComponent state = mState.get(entityId);

        // 1. ГРАВИТАЦИЯ: Применяется непрерывно. При движении вниз включается тяжелое падение.
        if (physics.useGravity) {
            if (velocity.y < 0) {
                float fallMultiplier = 1.9f; // Усиленная гравитация для быстрого падения без зависаний
                velocity.y += (GRAVITY * fallMultiplier) * deltaTime;
            } else {
                velocity.y += GRAVITY * deltaTime;
            }
        }

        // 2. УДЕРЖАНИЕ ПРЫЖКА: Плавно добавляем силу к скорости, создавая естественное сопротивление гравитации
        if (mJumpControl.has(entityId)) {
            JumpControlComponent jumpCtrl = mJumpControl.get(entityId);
            if (jumpCtrl.isJumping) {
                if (jumpCtrl.jumpTimeCounter < jumpCtrl.MAX_HOLD_TIME) {
                    velocity.y += jumpCtrl.HOLD_JUMP_FORCE * deltaTime;
                    jumpCtrl.jumpTimeCounter += deltaTime;
                } else {
                    jumpCtrl.isJumping = false; // Время удержания вышло
                }
            }
        }

        // 3. Рассчитываем желаемое смещение
        float targetX = transform.x + velocity.x * deltaTime;
        float targetY = transform.y + velocity.y * deltaTime;

        // 4. Двигаем объект через jbump (используется встроенный фильтр скольжения Slide)
        Response.Result result = jbumpWorld.move(physics.item, targetX, targetY, CollisionFilter.defaultFilter);

        // Синхронизируем координаты с миром jbump
        transform.x = result.goalX;
        transform.y = result.goalY;

        // 5. Обработка столкновений по нормалям
        boolean hitGroundThisFrame = false;
        Collisions collisions = result.projectedCollisions;

        for (int i = 0; i < collisions.size(); i++) {
            Collision col = collisions.get(i);

            // Столкновение с землей (удар снизу вверх)
            if (col.normal.y == 1) {
                if (velocity.y <= 0) {
                    hitGroundThisFrame = true;
                    velocity.y = 0;
                }
            }

            // Удар головой о потолок / блоки сверху
            if (col.normal.y == -1) {
                velocity.y = 0; // Мгновенно теряем импульс взлета
                if (mJumpControl.has(entityId)) {
                    mJumpControl.get(entityId).isJumping = false; // Прерываем фазу удержания
                }
            }
        }

        state.onGround = hitGroundThisFrame;
    }
}
