package com.github.br.perelesoq.jam26.ecs.system.base.ui;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.github.br.perelesoq.jam26.ecs.component.TransformComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.ViewPortSingletonComponent;

public class CameraSystem extends BaseSystem {

    protected ComponentMapper<TransformComponent> transformMapper;

    private int focusEntityId = -1;
    private final float xMin, xMax, yMin, yMax;

    // Идеальная (сглаженная) позиция камеры без учета тряски
    private final Vector3 idealPosition = new Vector3();
    private final Vector3 mVector3 = new Vector3();
    private float newZoom = 1f;

    // Переменные для Screen Shake
    private float shakeTimeLeft = 0f;
    private float shakeDuration = 0f;
    private float shakeMaxIntensity = 0f;

    // Счетчик для высокочастотного синуса (создает жесткую вибрацию)
    private float shakeWobbleTimer = 0f;

    public CameraSystem(float xMin, float xMax, float yMin, float yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    public void shake(float intensity, float duration) {
        if (intensity >= this.shakeMaxIntensity || this.shakeTimeLeft <= 0) {
            this.shakeMaxIntensity = intensity;
            this.shakeTimeLeft = duration;
            this.shakeDuration = duration;
            // Не сбрасываем wobbleTimer, чтобы вибрация шла непрерывно
        }
    }

    public void changeZoomTo(float newZoom) {
        this.newZoom = newZoom;
    }

    public void setFocusEntityId(int focusEntityId) {
        this.focusEntityId = focusEntityId;
    }

    @Override
    protected void processSystem() {
        ViewPortSingletonComponent viewPortComponent = ViewPortSingletonComponent.INSTANCE;
        OrthographicCamera camera = viewPortComponent.camera;
        float deltaTime = world.getDelta();

        // 1. Обработка зума
        if (camera.zoom != newZoom) {
            camera.zoom = MathUtils.lerp(camera.zoom, newZoom, 0.01f);
        }

        // ИСПРАВЛЕНО: Синхронизируем базовую позицию с реальным положением камеры.
        // Это подхватит центрирование из Level1Screen и уберет сдвиг на пол-экрана.
        if (idealPosition.x == 0 && idealPosition.y == 0) {
            idealPosition.set(camera.position);
        }

        // 2. Расчет ИДЕАЛЬНОЙ позиции (Следование за игроком)
        if (focusEntityId != -1) {
            TransformComponent transformComponent = transformMapper.get(focusEntityId);

            if (transformComponent != null) {
                // Ограничиваем координаты игрока рамками уровня
                float targetX = Math.max(xMin, Math.min(xMax, transformComponent.x));
                float targetY = Math.max(yMin, Math.min(yMax, transformComponent.y + 2));

                mVector3.set(targetX, targetY, 0);

                // Плавное движение ИДЕАЛЬНОЙ позиции к игроку
                idealPosition.lerp(mVector3, 0.1f);
            }
        }

        // Принудительно возвращаем камеру в идеальную точку (сбрасываем шум предыдущего кадра)
        camera.position.set(idealPosition);

        // 3. --- МАТЕМАТИКА ЖЕСТКОЙ ТРЯСКИ КОРАБЛЯ ---
        if (shakeTimeLeft > 0) {
            shakeTimeLeft -= deltaTime;
            shakeWobbleTimer += deltaTime * 50f; // Высокая частота для вибрации обшивки

            float progress = shakeTimeLeft / shakeDuration;
            float currentIntensity = shakeMaxIntensity * (progress * progress);

            // Генерируем хаотичную вибрацию стального корпуса без накопления дрейфа
            float shakeX = MathUtils.sin(shakeWobbleTimer) * currentIntensity;
            float shakeY = MathUtils.cos(shakeWobbleTimer * 1.3f) * currentIntensity;

            // Смещаем камеру строго на один текущий кадр
            camera.position.x += shakeX;
            camera.position.y += shakeY;

            if (shakeTimeLeft <= 0) {
                shakeMaxIntensity = 0f;
                shakeDuration = 0f;
                shakeWobbleTimer = 0f;
            }
        }

        // Применяем изменения матриц LibGDX
        camera.update();
    }
}
