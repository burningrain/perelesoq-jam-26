package com.github.br.perelesoq.jam26.ecs.system.dialog;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.github.br.perelesoq.jam26.render.ui.AnimatedImage;
import com.github.tommyettinger.textra.TypingLabel;

public class DialogueView {

    private final DialogViewAvatarFactory avatarFactory;

    //private final TypingLabel label;
    private final Label label;
    private final Image image;
    private final AnimatedImage background;

    public DialogueView(DialogViewAvatarFactory avatarFactory, Label text, Image image, AnimatedImage background) {
        this.avatarFactory = avatarFactory;
        this.label = text;
        this.image = image;
        this.background = background;
    }

    public boolean fadeIn() {
        return true;
    }

    public void update(String actorName, String text) {
        image.setDrawable(new TextureRegionDrawable(avatarFactory.getAvatar(actorName)));
        label.setText(text);
    }

    public boolean fadeOut() {
        return true;
    }

}
