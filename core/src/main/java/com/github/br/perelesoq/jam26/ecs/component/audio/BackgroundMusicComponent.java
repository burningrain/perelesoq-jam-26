package com.github.br.perelesoq.jam26.ecs.component.audio;

import com.artemis.Component;
import com.badlogic.gdx.audio.Music;

public class BackgroundMusicComponent extends Component {

    public String trackName;

    // Настройки громкости
    public float currentVolume = 0f;    // Стартует с 0 для плавного появления (Fade-in)
    public float targetVolume = 0.6f;   // К какой громкости стремимся (базовая громкость трека)
    public float fadeSpeed = 1.5f;      // Скорость изменения громкости (в секунду)

    public boolean isLooping = true;
    public boolean isMarkedForDestroy = false; // Флаг, сообщающий системе, что трек надо увести в 0 и выключить

    // Ссылка на низкоуровневый объект LibGDX
    public transient Music musicInstance;

}
