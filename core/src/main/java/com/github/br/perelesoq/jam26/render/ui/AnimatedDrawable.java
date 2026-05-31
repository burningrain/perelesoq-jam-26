package com.github.br.perelesoq.jam26.render.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class AnimatedDrawable extends TextureRegionDrawable {

    private final Animation<TextureRegion> animation;
    private float stateTime = 0;
    private boolean isPaused = false;

    public AnimatedDrawable(Animation<TextureRegion> animation) {
        this.animation = animation;
        TextureRegion key = animation.getKeyFrame(0);

        this.setLeftWidth(key.getRegionWidth() / 2f);
        this.setRightWidth(key.getRegionWidth() / 2f);
        this.setTopHeight(key.getRegionHeight() / 2f);
        this.setBottomHeight(key.getRegionHeight() / 2f);
        this.setMinWidth(key.getRegionWidth());
        this.setMinHeight(key.getRegionHeight());

        // Сразу инициализируем базовый регион
        setRegion(key);
    }

    // Метод для обновления времени, который нужно вызывать из Actor.act()
    public void update(float delta) {
        if (!isPaused) {
            stateTime += delta;
        }
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        // ИССПРАВЛЕНО: Используем метод без флага, чтобы работал LOOP_PINGPONG
        setRegion(animation.getKeyFrame(stateTime));
        super.draw(batch, x, y, width, height);
    }

    @Override
    public void draw(
        Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX,
        float scaleY, float rotation
    ) {
        // ИСПРАВЛЕНО: Убрано "true", теперь анимация корректно идет в обратную сторону
        setRegion(animation.getKeyFrame(stateTime));
        super.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    public boolean isAnimationEnd() {
        return animation.isAnimationFinished(stateTime);
    }

    public void play() {
        isPaused = false;
    }

    public void pause() {
        isPaused = true;
    }

    public void resetAndPause() {
        pause();
        stateTime = 0;
    }

    public void setFrameAndPause(int frameIndex) {
        TextureRegion keyFrame = animation.getKeyFrames()[frameIndex];
        setRegion(keyFrame);

        stateTime = frameIndex * animation.getFrameDuration();
        pause();
    }

}
