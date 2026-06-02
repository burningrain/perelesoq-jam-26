package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.math.MathUtils;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.ecs.component.audio.PlaySoundComponent;
import com.github.br.perelesoq.jam26.ecs.component.render.ChangeRenderLayerComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.SirenSingletonComponent;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;

public class SirenSystem extends BaseSystem {

    protected ComponentMapper<ChangeRenderLayerComponent> mLayerChange;
    protected ComponentMapper<PlaySoundComponent> mPlaySound; // Маппер для создания запросов на звук

    // Храним ID единственной долгоживущей сущности-процесса сирены
    private int sirenLayerEntityId = -1;

    // Счетчик времени для расчета синусоиды мерцания
    private float totalTime = 0f;

    // Таймер для отслеживания момента проигрывания звука
    private float soundTimer = 0f;

    // НАСТРОЙКИ СИРЕНЫ
    private static final float BLINK_FREQUENCY = 1.0f; // Частота: 1 полный цикл (туда-обратно) в секунду

    @Override
    protected void processSystem() {
        // Читаем глобальное состояние: активна ли тревога прямо сейчас
        boolean isSirenEnabled = SirenSingletonComponent.INSTANCE.isActive;

        // ПРОВЕРКА ВАЛИДНОСТИ: существует ли сущность в ECS мире прямо сейчас
        boolean isEntityActive = sirenLayerEntityId != -1 && world.getEntityManager().isActive(sirenLayerEntityId);

        // --- ЛОГИКА ВЫКЛЮЧЕНИЯ СИРЕНЫ ---
        if (!isSirenEnabled) {
            if (isEntityActive) {
                ChangeRenderLayerComponent cmd = mLayerChange.get(sirenLayerEntityId);
                if (cmd != null) {
                    cmd.opacity = 0f;
                    cmd.isVisible = false;
                    cmd.isDirty = true;
                }
                world.delete(sirenLayerEntityId);
            }

            sirenLayerEntityId = -1;
            totalTime = 0f;
            soundTimer = 0f; // Сбрасываем звуковой таймер
            return;
        }

        // --- ЛОГИКА ВКЛЮЧЕНИЯ И ИНИЦИАЛИЗАЦИИ СИРЕНЫ ---
        if (!isEntityActive) {
            sirenLayerEntityId = world.create();
            ChangeRenderLayerComponent cmd = mLayerChange.create(sirenLayerEntityId);
            cmd.layerName = TiledUiConstants.Layers.BACK_BLUE;
            cmd.isVisible = true;
            cmd.opacity = 0f;
            cmd.isDirty = true;
        }

        // --- ЛОГИКА ПЛАВНОЙ ИНТЕРПОЛЯЦИИ (КАЖДЫЙ КАДР) ---
        float deltaTime = world.getDelta();
        totalTime += deltaTime;
        soundTimer += deltaTime;

        // --- ПРОИГРЫВАНИЕ ЗВУКА СИРЕНЫ (Раз в цикл) ---
        // Интервал равен 1.0f / BLINK_FREQUENCY (при частоте 1.0f это ровно 1 секунда)
        float soundInterval = 1.0f / BLINK_FREQUENCY;
        if (soundTimer >= soundInterval) {
            soundTimer -= soundInterval; // Мягкий сброс таймера без потери долей секунды

            // Создаем сущность-запрос на звук [10]
            int soundRequestEntity = world.create();
            PlaySoundComponent playSound = mPlaySound.create(soundRequestEntity);
            playSound.soundName = Resources.Sound.SIREN;
            playSound.volume = 0.7f; // Настраиваемая громкость эффекта сирены [10]
            playSound.pitch = 1.0f;
        }

        // --- РАСЧЕТ СИНУСОИДЫ ДЛЯ ВИЗУАЛА ---
        float sinValue = MathUtils.sin(totalTime * BLINK_FREQUENCY * MathUtils.PI2);
        float targetAlpha = (sinValue + 1f) / 2f;

        ChangeRenderLayerComponent cmd = mLayerChange.get(sirenLayerEntityId);

        if (cmd.opacity != targetAlpha) {
            cmd.opacity = targetAlpha;
            cmd.isDirty = true;
        }
    }
}
