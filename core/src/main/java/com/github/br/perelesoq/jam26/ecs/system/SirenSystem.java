package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.math.MathUtils;
import com.github.br.perelesoq.jam26.ecs.component.render.ChangeRenderLayerComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.SirenSingletonComponent;

public class SirenSystem extends BaseSystem {

    // НАСТРОЙКИ СИРЕНЫ
    private final String TARGET_LAYER = "back_blue";
    private final float BLINK_FREQUENCY = 0.5f; // Сколько полных циклов (туда-обратно) происходит за 1 секунду

    protected ComponentMapper<ChangeRenderLayerComponent> mLayerChange;

    private int sirenLayerEntityId = -1;
    private float totalTime = 0f;

    @Override
    protected void processSystem() {
        // Читаем глобальное состояние: активна ли тревога прямо сейчас
        boolean isSirenEnabled = SirenSingletonComponent.INSTANCE.isActive;

        // --- ЛОГИКА ВЫКЛЮЧЕНИЯ СИРЕНЫ ---
        if (!isSirenEnabled) {
            // Если сирена выключена, но сущность в мире еще существует — удаляем её
            if (sirenLayerEntityId != -1) {
                if (world.getEntityManager().isActive(sirenLayerEntityId)) {
                    // Создаем финальную разовую команду для полного скрытия слоя перед удалением
                    // (или можно оставить ее видимой, если это дефолтный слой, но для сирены лучше скрыть)
                    ChangeRenderLayerComponent cmd = mLayerChange.get(sirenLayerEntityId);
                    if (cmd != null) {
                        cmd.opacity = 0f;
                        cmd.isDirty = true; // Принудительно заставляем рендер скрыть слой в последний раз
                    }

                    // Удаляем сущность. Компонент автоматически вернется в пул Artemis
                    world.delete(sirenLayerEntityId);
                }

                // Сбрасываем локальное состояние системы
                sirenLayerEntityId = -1;
                totalTime = 0f;
            }
            return;
        }

        // --- ЛОГИКА ВКЛЮЧЕНИЯ И ИНИЦИАЛИЗАЦИИ СИРЕНЫ ---
        // Если сирена включена, но сущности-процесса еще нет в мире — создаем ОДИН раз
        if (sirenLayerEntityId == -1 || !world.getEntityManager().isActive(sirenLayerEntityId)) {
            sirenLayerEntityId = world.create();
            ChangeRenderLayerComponent cmd = mLayerChange.create(sirenLayerEntityId);
            cmd.layerName = TARGET_LAYER;
            cmd.opacity = 0f;
            cmd.isDirty = true; // Взводим флаг для первой обработки рендерером
        }

        // --- ЛОГИКА ПЛАВНОЙ ИНТЕРПОЛЯЦИИ (КАЖДЫЙ КАДР) ---
        totalTime += world.getDelta();

        // Берем синус от времени. MathUtils.PI2 задает полный круг (период в 1 секунду при частоте 1.0)
        float sinValue = MathUtils.sin(totalTime * BLINK_FREQUENCY * MathUtils.PI2);

        // Переводим математический диапазон синуса [-1, 1] в диапазон прозрачности LibGDX [0, 1]
        float targetAlpha = (sinValue + 1f) / 2f;

        ChangeRenderLayerComponent cmd = mLayerChange.get(sirenLayerEntityId);

        // Оптимизация: взводим грязный флаг для RenderSystem ТОЛЬКО если прозрачность реально изменилась
        if (cmd.opacity != targetAlpha) {
            cmd.opacity = targetAlpha;
            cmd.isDirty = true; // Сигнализируем рендереру, что данные свежие и их нужно применить к карте
        }
    }
}
