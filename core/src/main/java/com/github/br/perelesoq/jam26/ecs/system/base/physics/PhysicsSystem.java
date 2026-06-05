package com.github.br.perelesoq.jam26.ecs.system.base.physics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.dongbat.jbump.*;
import com.github.br.perelesoq.jam26.ecs.component.*;
import com.github.br.perelesoq.jam26.ecs.component.physics.Hitbox;
import com.github.br.perelesoq.jam26.ecs.component.physics.PhysicsComponent;
import com.github.br.perelesoq.jam26.ecs.component.physics.RemoveFromPhysicsWorldComponent;
import com.github.br.perelesoq.jam26.ecs.component.trigger.OverlappedThisFrameComponent;
import com.github.br.perelesoq.jam26.ecs.component.trigger.TriggerComponent;
import com.github.br.perelesoq.jam26.ecs.component.ui.render.RenderComponent;

public class PhysicsSystem extends IteratingSystem {

    private final float GRAVITY = -450f;
    private final World<Integer> jbumpWorld;

    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<PhysicsComponent> mPhysics;
    private ComponentMapper<VelocityComponent> mVelocity;
    private ComponentMapper<JumpControlComponent> mJumpControl;
    private ComponentMapper<CharacterStateComponent> mState;
    protected ComponentMapper<TriggerComponent> mTrigger;

    private ComponentMapper<BulletComponent> mBullet;
    private ComponentMapper<HealthComponent> mHealth;

    // маппер для компонента удаления
    private ComponentMapper<RemoveFromPhysicsWorldComponent> mRemovePhysics;

    private final CollisionFilter triggerCollisionFilter = new CollisionFilter() {
        @Override
        public Response filter(Item item, Item other) {
            if (item.userData == null || other.userData == null) {
                return Response.slide;
            }

            int entityIdA = (Integer) item.userData;
            int entityIdB = (Integer) other.userData;

            // Проверяем, является ли кто-то триггером или пулей
            boolean isTriggerA = mTrigger.has(entityIdA) || mBullet.has(entityIdA);
            boolean isTriggerB = mTrigger.has(entityIdB) || mBullet.has(entityIdB);

            if (isTriggerA || isTriggerB) {
                // Если это пуля, настраиваем поведение при встрече с твердыми объектами
                if (mBullet.has(entityIdA)) {
                    boolean isWallOrBoss = !mTrigger.has(entityIdB) && entityIdB != com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent.INSTANCE.playerId;
                    if (isWallOrBoss) return Response.touch;
                }
                if (mBullet.has(entityIdB)) {
                    boolean isWallOrBoss = !mTrigger.has(entityIdA) && entityIdA != com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent.INSTANCE.playerId;
                    if (isWallOrBoss) return Response.touch;
                }

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
        // --- 1. ОЧИСТКА СТАРЫХ МАРКЕРОВ ТРИГГЕРОВ (Ваш текущий код) ---
        IntBag allOverlapped = world.getAspectSubscriptionManager()
            .get(Aspect.all(OverlappedThisFrameComponent.class)).getEntities();
        for (int i = allOverlapped.size() - 1; i >= 0; i--) {
            world.edit(allOverlapped.get(i)).remove(OverlappedThisFrameComponent.class);
        }

        // --- 2. НОВАЯ ЛОГИКА: ПРИНУДИТЕЛЬНОЕ УДАЛЕНИЕ ОБЪЕКТОВ ИЗ JBUMP ---
        IntBag toRemoveBag = world.getAspectSubscriptionManager()
            .get(Aspect.all(RemoveFromPhysicsWorldComponent.class)).getEntities();

        for (int i = toRemoveBag.size() - 1; i >= 0; i--) {
            int entityId = toRemoveBag.get(i);
            RemoveFromPhysicsWorldComponent req = mRemovePhysics.get(entityId);

            if (req != null && req.itemToRemove != null) {
                // Жестко выкидываем айтем из физического мира, пока он еще существует!
                jbumpWorld.remove(req.itemToRemove);
            }

            // Удаляем сущность-запрос, отправляя компонент в пул
            world.delete(entityId);
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

        // --- БЕЗОПАСНАЯ СИНХРОНИЗАЦИЯ FLIP (ЗАЩИТА ОТ ТЕЛЕПОРТАЦИИ В СТЕНЫ) ---
        Rect rect = jbumpWorld.getRect(physics.item);
        if (rect != null) {
            float desiredHitboxX = transform.x + actualOffsetX;
            float desiredHitboxY = transform.y + actualOffsetY;

            if (rect.x != desiredHitboxX || rect.y != desiredHitboxY) {
                Response.Result flipResult = jbumpWorld.move(physics.item, desiredHitboxX, desiredHitboxY, triggerCollisionFilter);
                // Сразу корректируем трансформ графического холста под результат безопасного смещения
                transform.x = flipResult.goalX - actualOffsetX;
                transform.y = flipResult.goalY - actualOffsetY;
            }
        }

        // Вытаскиваем точные физические координаты хитбокса после обработки флипа
        Rect finalRect = jbumpWorld.getRect(physics.item);
        float currentX = finalRect != null ? finalRect.x : transform.x + actualOffsetX;
        float currentY = finalRect != null ? finalRect.y : transform.y + actualOffsetY;

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

            if (responseType == Response.touch && mBullet.has(entityId)) {
                int hitEntityId = (entityId == entityIdA) ? entityIdB : entityIdA;

                // 1. Наносим урон, если у цели есть здоровье (Босс)
                if (mHealth.has(hitEntityId)) {
                    HealthComponent health = mHealth.get(hitEntityId);
                    if (!health.isDead) {
                        BulletComponent bullet = mBullet.get(entityId);
                        health.hp -= bullet.damage;

                        // ЗАЖИГАЕМ ОБЩИЙ ТАЙМЕР ВСПЫШКИ В КОМПОНЕНТЕ РЕНДЕРА ЖЕРТВЫ
                        ComponentMapper<RenderComponent> mRender = world.getMapper(RenderComponent.class);
                        if (mRender.has(hitEntityId)) {
                            mRender.get(hitEntityId).flashTimer = 0.12f; // Задали вспышку на 12 сотых секунды
                        }

                        if (health.hp <= 0) {
                            health.hp = 0;
                            health.isDead = true;
                        }
                    }
                }

                // 2. Спавним команду на удаление пули из JBump через ваш компонент
//                int removeRequestEntity = world.create();
//                RemoveFromPhysicsWorldComponent removeReq = mRemovePhysics.create(removeRequestEntity);
//                removeReq.itemToRemove = physics.item;

                // 3. Удаляем пулю из ECS
                world.delete(entityId);
                return; // Завершаем обработку кадра для этой пули
            }

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
