package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.math.MathUtils;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.ecs.component.audio.PlaySoundComponent;
import com.github.br.perelesoq.jam26.ecs.component.ui.render.ChangeRenderLayerComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.SirenSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.CameraSystem;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;

public class SirenSystem extends BaseSystem {

    protected ComponentMapper<ChangeRenderLayerComponent> mLayerChange;
    protected ComponentMapper<PlaySoundComponent> mPlaySound;

    private int sirenLayerEntityId = -1;
    private float totalTime = 0f;
    private float soundTimer = 0f;

    // --- НОВЫЕ ПОЛЯ ДЛЯ ЗАДЕРЖКИ ТРЯСКИ ---
    private float shakeDelayTimer = -1f; // Таймер задержки (-1 означает, что ожидания нет)

    // НАСТРОЙКИ СИРЕНЫ
    private static final float BLINK_FREQUENCY = 1.0f; // Частота: 1 полный цикл в секунду

    // Длительность звука сирены. Измерьте ваш ассет siren.mp3/wav и вставьте точное время в секундах!
    // Например, если аудио идет 0.6 секунды, ставим 0.6f.
    private static final float SOUND_DURATION = 2.0f;

    @Override
    protected void processSystem() {
        boolean isSirenEnabled = SirenSingletonComponent.INSTANCE.isActive;
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
            soundTimer = 0f;
            shakeDelayTimer = -1f; // Сбрасываем таймер ожидания тряски
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
        float soundInterval = 1.0f / BLINK_FREQUENCY;
        if (soundTimer >= soundInterval) {
            soundTimer -= soundInterval;

            // 1. Создаем сущность-запрос на звук [10]
            createPlaySound();

            // 2. ВЗВОДИМ ТАЙМЕР ЗАДЕРЖКИ: Тряска начнется ровно через длительность звука
            shakeDelayTimer = SOUND_DURATION;
        }

        // --- ЛОГИКА ОТСЧЕТА ЗАДЕРЖКИ И ЗАПУСКА ТРЯСКИ ---
        if (shakeDelayTimer > 0) {
            shakeDelayTimer -= deltaTime;

            // Как только таймер дотикал до нуля — значит, звук сирены прямо сейчас завершился!
            if (shakeDelayTimer <= 0) {
                shakeDelayTimer = -1f; // Выключаем таймер ожидания

                // Запускаем тряску в CameraSystem
                CameraSystem camSystem = world.getSystem(CameraSystem.class);
                if (camSystem != null) {
                    camSystem.shake(1.5f, 0.35f);
                }
            }
        }

        // --- РАСЧЕТ СИНУСОИДЫ ДЛЯ ВИЗУАЛА ---
        float sinValue = MathUtils.sin(totalTime * BLINK_FREQUENCY * MathUtils.PI2);
        float targetAlpha = (sinValue + 1f) / 2f;

        ChangeRenderLayerComponent cmd = mLayerChange.get(sirenLayerEntityId);

        if (cmd != null && cmd.opacity != targetAlpha) {
            cmd.opacity = targetAlpha;
            cmd.isDirty = true;
        }
    }

    private void createPlaySound() {
        int soundRequestEntity = world.create();
        PlaySoundComponent playSound = mPlaySound.create(soundRequestEntity);
        playSound.soundName = Resources.Sound.SIREN;
        playSound.volume = 0.7f; // [10]
        playSound.pitch = 1.0f;
    }
}
