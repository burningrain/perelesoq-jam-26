package com.github.br.perelesoq.jam26.render.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class AnimatedImage extends Image {

    private final AnimatedDrawable animatedDrawable;

    public AnimatedImage(Animation<TextureRegion> animation) {
        super(new AnimatedDrawable(animation));
        this.setSize(this.getPrefWidth(), this.getPrefHeight());
        this.setOrigin(this.getWidth() / 2f, this.getHeight() / 2f);

        this.animatedDrawable = (AnimatedDrawable) getDrawable();

        pause();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (animatedDrawable != null) {
            animatedDrawable.update(delta);
        }
    }

    public void play() {
        animatedDrawable.play();
    }

    public Animation<TextureRegion> getAnimation() {
        return animatedDrawable.getAnimation();
    }

    public void pause() {
        animatedDrawable.pause();
    }

    public void reset() {
        animatedDrawable.resetAndPause();
    }

    public void setFrameAndPause(int frame) {
        animatedDrawable.setFrameAndPause(frame);
    }

    public boolean isAnimationEnd() {
        return animatedDrawable.isAnimationEnd();
    }

}
