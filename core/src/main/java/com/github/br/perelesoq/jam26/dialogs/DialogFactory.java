package com.github.br.perelesoq.jam26.dialogs;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.ecs.component.singleton.Controller1SingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.DialogueSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.SirenSingletonComponent;

public class DialogFactory {

    public static final String TERMINAL_1 = "terminal_1";
    public static final String CONTROLLER_1_IS_NOT_ACTIVATED = "controller_1_is_not_activated";

    public Array<DialogueSingletonComponent.Phrase> controller_1_is_not_activated() {
        return new Array<DialogueSingletonComponent.Phrase>() {{
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_HERO,
                "Хм...пропуск не работает..."
            ));
        }};
    }

    public Array<DialogueSingletonComponent.Phrase> terminal_1() {
        return new Array<DialogueSingletonComponent.Phrase>() {{
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Эй, ты там, вижу тебя. Здравствуй!"
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Можешь помахать рукой в камеру? Мне на память!"
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_HERO,
                "Привет. Не буду. Можешь отключить гравитацию?"
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Хорошо. Большая кнопка отключает гравитацию."
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_HERO,
                "Нажал. Ничего не происходит..."
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Теперь приложи пропуск к терминалу."
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_HERO,
                "Сделано.",
                () -> {
                    SirenSingletonComponent.INSTANCE.isActive = true;
                }
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Ух ты ж! Ох ты ж! А-ха-ха-ха-ха..."
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_HERO,
                "Что случилось?"
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Твой пропуск активировался в системе."
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_HERO,
                "И-и-и?!"
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "И механизм самоуничтожения модуля станции ТОЖЕ!"
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Не стой столбом, беги оттуда! СКОРЕЕ!!!"
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Я начну вести обратный отсчет! А-ХА-ХА-ХА-ХА...",
                () -> {
                    Controller1SingletonComponent.INSTANCE.isActivated = true;
                }
            ));
        }};
    }

    public static Array<DialogueSingletonComponent.Phrase> boss_is_dead() {
        return new Array<DialogueSingletonComponent.Phrase>() {{
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "С окончанием игры для геймджема 'перелесок' 2026 года!",
                () -> {
                    Controller1SingletonComponent.INSTANCE.isActivated = true;
                }
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_HERO,
                "Спасибо!"
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Чем теперь займешься?!",
                () -> {
                    Controller1SingletonComponent.INSTANCE.isActivated = true;
                }
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_HERO,
                "ТЕПЕРЬ Я ОТДОХНУ!!!"
            ));
        }};
    }

    public Array<DialogueSingletonComponent.Phrase> boss_opening() {
        return new Array<DialogueSingletonComponent.Phrase>() {{
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Вижу, ты нашел тот самый ПЫЛЕСОС!",
                () -> {
                    Controller1SingletonComponent.INSTANCE.isActivated = true;
                }
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_HERO,
                "Именно!"
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "И чем же теперь мы займемся?",
                () -> {
                    Controller1SingletonComponent.INSTANCE.isActivated = true;
                }
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_HERO,
                "Теперь я буду кидаться в тебя ифами!"
            ));
        }};
    }

    public Array<DialogueSingletonComponent.Phrase> getDialog(String dialog) {
        switch (dialog) {
            case TERMINAL_1:
                return terminal_1();
            case CONTROLLER_1_IS_NOT_ACTIVATED:
                return controller_1_is_not_activated();

            default:
                throw new GdxRuntimeException("prases are not found for dialog [" + dialog + "]");
        }
    }

}
