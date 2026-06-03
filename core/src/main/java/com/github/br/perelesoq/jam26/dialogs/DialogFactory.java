package com.github.br.perelesoq.jam26.dialogs;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.ecs.component.singleton.DialogueSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.SirenSingletonComponent;

public class DialogFactory {

    private final ObjectMap<String, Array<DialogueSingletonComponent.Phrase>> dialogMap = new ObjectMap<>();

    public DialogFactory() {
        terminal_1();
    }

    private void terminal_1() {
        addDialog("terminal_1", new Array<DialogueSingletonComponent.Phrase>() {{
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
                "Я начну вести обратный отсчет! А-ХА-ХА-ХА-ХА..."
            ));
        }});
    }

    public void addDialog(String dialog, Array<DialogueSingletonComponent.Phrase> array) {
        dialogMap.put(dialog, array);
    }

    public Array<DialogueSingletonComponent.Phrase> getDialog(String dialog) {
        Array<DialogueSingletonComponent.Phrase> phrases = dialogMap.get(dialog);
        if (phrases == null) {
            throw new GdxRuntimeException("prases are not found for dialog [" + dialog + "]");
        }

        return phrases;
    }

}
