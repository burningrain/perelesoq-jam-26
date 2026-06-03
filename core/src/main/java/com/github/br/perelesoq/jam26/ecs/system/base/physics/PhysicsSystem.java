package com.github.br.perelesoq.jam26.ecs.system.base.physics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.dongbat.jbump.*;
import com.github.br.perelesoq.jam26.ecs.component.*;
import com.github.br.perelesoq.jam26.ecs.component.physics.Hitbox;
import com.github.br.perelesoq.jam26.ecs.component.physics.PhysicsComponent;
import com.github.br.perelesoq.jam26.ecs.component.trigger.OverlappedThisFrameComponent;
import com.github.br.perelesoq.jam26.ecs.component.trigger.TriggerComponent;

public class PhysicsSystem extends IteratingSystem {

    private final float GRAVITY = -450f;
    private final World<Integer> jbumpWorld;

    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<PhysicsComponent> mPhysics;
    private ComponentMapper<VelocityComponent> mVelocity;
    private ComponentMapper<JumpControlComponent> mJumpControl;
    private ComponentMapper<CharacterStateComponent> mState;
    protected ComponentMapper<TriggerComponent> mTrigger;

    private final CollisionFilter triggerCollisionFilter = new CollisionFilter() {
        @Override
        public Response filter(Item item, Item other) {
            int entityIdA = (Integer) item.userData;
            int entityIdB = (Integer) other.userData;
            if (mTrigger.has(entityIdA) || mTrigger.has(entityIdB)) {
                return Response.cross;
            }
            return Response.slide;
        }
    };

    public PhysicsSystem() {
        super(Aspect.all(TransformComponent.class, PhysicsComponent.class, VelocityComponent.class, CharacterStateComponent.class));
        this.jbumpWorld = new World<>(32f);
    }

    @Override
    protected void begin() {
        // Каждый кадр перед симуляцией физики очищаем маркеры наложения у всех объектов,
        // чтобы jbump заново выставил их актуальное состояние
        IntBag allOverlapped = world.getAspectSubscriptionManager()
            .get(Aspect.all(OverlappedThisFrameComponent.class)).getEntities();
        for (int i = 0; i < allOverlapped.size(); i++) {
            world.edit(allOverlapped.get(i)).remove(OverlappedThisFrameComponent.class);
        }
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

        Hitbox hitbox = physics.hitbox;
        // Математика флипа: определяем расстояние от левого/нижнего края холста до левого/нижнего края хитбокса
        float actualOffsetX = transform.flipX ? hitbox.paddingRight : hitbox.paddingLeft;
        float actualOffsetY = transform.flipY ? hitbox.paddingTop : hitbox.paddingBottom;

        float startX = transform.x + actualOffsetX;
        float startY = transform.y + actualOffsetY;

        // Добавляем в jbump объект с чистыми размерами физического тела
        jbumpWorld.add(physics.item, startX, startY, hitbox.getPhysicsWidth(), hitbox.getPhysicsHeight());
    }

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
        Hitbox hitbox = physics.hitbox;

        // РАСЧЕТ АКТУАЛЬНЫХ СМЕЩЕНИЙ ДЛЯ ТЕКУЩЕГО КАДРА
        // Если произошел flipX, левым отступом физического тела становится правый отступ текстуры.
        // Если произошел flipY, нижним отступом физического тела становится верхний отступ текстуры.
        float actualOffsetX = transform.flipX ? hitbox.paddingRight : hitbox.paddingLeft;
        float actualOffsetY = transform.flipY ? hitbox.paddingTop : hitbox.paddingBottom;

        // Синхронизируем положение хитбокса в jbump (на случай, если flip изменился на этом кадре)
        float currentX = transform.x + actualOffsetX;
        float currentY = transform.y + actualOffsetY;
        jbumpWorld.update(physics.item, currentX, currentY);

        // 1. ГРАВИТАЦИЯ
        if (physics.useGravity) {
            if (velocity.y < 0) {
                float fallMultiplier = 1.9f;
                velocity.y += (GRAVITY * fallMultiplier) * deltaTime;
            } else {
                velocity.y += GRAVITY * deltaTime;
            }
        }

        // 2. УДЕРЖАНИЕ ПРЫЖКА
        if (mJumpControl.has(entityId)) {
            JumpControlComponent jumpCtrl = mJumpControl.get(entityId);
            if (jumpCtrl.isJumping) {
                if (jumpCtrl.jumpTimeCounter < jumpCtrl.MAX_HOLD_TIME) {
                    velocity.y += jumpCtrl.HOLD_JUMP_FORCE * deltaTime;
                    jumpCtrl.jumpTimeCounter += deltaTime;
                } else {
                    jumpCtrl.isJumping = false;
                }
            }
        }

        // 3. Расчет желаемой позиции хитбокса
        float targetX = currentX + velocity.x * deltaTime;
        float targetY = currentY + velocity.y * deltaTime;

        // 4. Движение хитбокса через jbump
        Response.Result result = jbumpWorld.move(physics.item, targetX, targetY, triggerCollisionFilter);

        // 5. СИНХРОНИЗАЦИЯ С ТРАНСФОРМОМ (ОБРАТНАЯ)
        // Вычитаем текущие смещения, чтобы получить чистые координаты левого нижнего угла графического холста
        transform.x = result.goalX - actualOffsetX;
        transform.y = result.goalY - actualOffsetY;

        // 6. Обработка столкновений по нормалям
        boolean hitGroundThisFrame = false;
        Collisions collisions = result.projectedCollisions;

        for (int i = 0; i < collisions.size(); i++) {
            Collision col = collisions.get(i);
            Response responseType = col.type;

            int entityIdA = (Integer) col.item.userData;
            int entityIdB = (Integer) col.other.userData;

            if (mTrigger.has(entityIdA) && entityIdB == entityId) {
                world.edit(entityIdA).create(OverlappedThisFrameComponent.class);
            } else if (mTrigger.has(entityIdB) && entityIdA == entityId) {
                world.edit(entityIdB).create(OverlappedThisFrameComponent.class);
            }

            if (responseType == Response.slide) {
                // Приземление (столкновение снизу вверх)
                if (col.normal.y == 1 && velocity.y <= 0) {
                    hitGroundThisFrame = true;
                    velocity.y = 0;
                }
                // Удар головой о потолок (столкновение сверху вниз)
                if (col.normal.y == -1) {
                    velocity.y = 0;
                    if (mJumpControl.has(entityId)) {
                        mJumpControl.get(entityId).isJumping = false;
                    }
                }
            }
        }

        state.onGround = hitGroundThisFrame;
    }
}
