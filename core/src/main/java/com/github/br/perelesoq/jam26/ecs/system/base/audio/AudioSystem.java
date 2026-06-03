package com.github.br.perelesoq.jam26.ecs.system.base.audio;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.github.br.perelesoq.jam26.ecs.component.audio.BackgroundMusicComponent;
import com.github.br.perelesoq.jam26.ecs.component.audio.PlaySoundComponent;

public class AudioSystem extends BaseSystem {

    private final AssetManager assetManager;

    private EntitySubscription soundSubscription;
    private EntitySubscription musicSubscription;

    private ComponentMapper<PlaySoundComponent> mSound;
    private ComponentMapper<BackgroundMusicComponent> mMusic;

    public AudioSystem(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    protected void initialize() {
        soundSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(PlaySoundComponent.class));

        musicSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(BackgroundMusicComponent.class));
    }

    @Override
    protected void processSystem() {
        float deltaTime = world.getDelta();

        // --- 1. ОБРАБОТКА РАЗОВЫХ ЗВУКОВЫХ ЭФФЕКТОВ ---
        IntBag soundEntities = soundSubscription.getEntities();
        int[] soundIds = soundEntities.getData();
        for (int i = 0, s = soundEntities.size(); i < s; i++) {
            int entityId = soundIds[i];
            PlaySoundComponent soundReq = mSound.get(entityId);

            if (soundReq != null && soundReq.soundName != null) {
                // ИГРАЕМ ЗВУК ЧЕРЕЗ ВАШ КЭШ ЗВУКОВ / ASSET MANAGER
                 com.badlogic.gdx.audio.Sound sfx = assetManager.get(soundReq.soundName, Sound.class);
                 long id = sfx.play(soundReq.volume);
                 sfx.setPitch(id, soundReq.pitch);
            }

            world.delete(entityId);
        }

        // --- 2. ОБРАБОТКА ФОНОВОЙ МУЗЫКИ С ЭФФЕКТАМИ FADE-IN / FADE-OUT ---
        IntBag musicEntities = musicSubscription.getEntities();
        int[] musicIds = musicEntities.getData();
        for (int i = 0, s = musicEntities.size(); i < s; i++) {
            int entityId = musicIds[i];
            BackgroundMusicComponent musicComp = mMusic.get(entityId);

            if (musicComp == null) continue;

            // Инициализация нового LibGDX стрима музыки
            if (musicComp.musicInstance == null && musicComp.trackName != null) {
                 Music gdxMusic = assetManager.get(musicComp.trackName, Music.class);
                 gdxMusic.setLooping(musicComp.isLooping);
                 gdxMusic.setVolume(musicComp.currentVolume); // Начинаем с 0f
                 gdxMusic.play();
                 musicComp.musicInstance = gdxMusic;
            }

            Music instance = musicComp.musicInstance;
            if (instance == null) continue;

            // Если трек помечен на уничтожение, его целевая громкость принудительно падает в 0
            if (musicComp.isMarkedForDestroy) {
                musicComp.targetVolume = 0f;
            }

            // ПЛАВНАЯ ИНТЕРПОЛЯЦИЯ ГРОМКОСТИ К ЦЕЛЕВОЙ (ЧИСТАЯ JAVA МАТЕМАТИКА ВМЕСТО APPROACH)
            if (musicComp.currentVolume != musicComp.targetVolume) {
                float step = musicComp.fadeSpeed * deltaTime;

                if (musicComp.currentVolume < musicComp.targetVolume) {
                    // Плавное нарастание (Fade-in)
                    musicComp.currentVolume = Math.min(musicComp.currentVolume + step, musicComp.targetVolume);
                } else {
                    // Плавное затухание (Fade-out)
                    musicComp.currentVolume = Math.max(musicComp.currentVolume - step, musicComp.targetVolume);
                }

                instance.setVolume(musicComp.currentVolume);
            }

            // ПОЛНОЕ ЗАТУХАНИЕ ЗАВЕРШЕНО: Если трек ушел в 0 и был помечен на удаление — глушим его полностью
            if (musicComp.currentVolume <= 0f && musicComp.isMarkedForDestroy) {
                instance.stop();
                world.delete(entityId); // Полностью стираем сущность старой музыки из ECS мира
            }
        }
    }
}
