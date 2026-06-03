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
        addDialog("terminal_1", new Array<DialogueSingletonComponent.Phrase>() {{
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Эй, ты там, вижу тебя через камеру. Здорова!"
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_HERO,
                "Привет. Можешь отключить гравитацию?"
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Могу уменьшить. Секунду..."
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Сделано! Заодно разблокировал двери",
                () -> {
                SirenSingletonComponent.INSTANCE.isActive = true;
            }
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_HERO,
                "..."
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_HERO,
                "Что случилось?"
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "Ну и ну, А-ха-ха-ха!!! Это запустило механизм самоуничтожения модуля станции!"
            ));
            add(new DialogueSingletonComponent.Phrase(
                Resources.Atlases.Objects.AVATAR_COMPUTER,
                "БЕГИ ОТТУДА! БЫСТРЕЕ!!! А-ха-ха-ха..."
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
