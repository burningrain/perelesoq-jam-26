package com.github.br.perelesoq.jam26.ecs.component.singleton;

import com.artemis.Component;
import com.badlogic.gdx.utils.Array;

public class DialogueSingletonComponent extends Component {
    public static final DialogueSingletonComponent INSTANCE = new DialogueSingletonComponent();

    public boolean isActive = false;
    public int currentPhraseIndex = 0;

    public final Array<Phrase> phrases = new Array<>();

    public static class Phrase {
        public String avatar;
        public String text;
        public Runnable reaction;

        public Phrase(String avatar, String text) {
            this.avatar = avatar;
            this.text = text;
        }

        public Phrase(String avatar, String text, Runnable reaction) {
            this.avatar = avatar;
            this.text = text;
            this.reaction = reaction;
        }
    }

    public void start(Array<Phrase> newPhrases) {
        this.phrases.clear();
        this.phrases.addAll(newPhrases);
        this.currentPhraseIndex = 0;
        this.isActive = true;
    }

    public boolean nextPhrase() {
        currentPhraseIndex++;
        if (currentPhraseIndex >= phrases.size) {
            isActive = false; // Диалог закончился
            return false;
        }
        return true;
    }

    public Phrase getCurrentPhrase() {
        if (!isActive || phrases.size == 0) return null;
        return phrases.get(currentPhraseIndex);
    }

}
