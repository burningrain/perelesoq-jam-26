package com.github.br.perelesoq.jam26.ecs.system.dialog;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.github.br.perelesoq.jam26.render.ui.AnimatedImage;
import com.github.tommyettinger.textra.TypingLabel;

public class DialogueView {

    private final DialogViewAvatarFactory avatarFactory;

    private final TypingLabel label;
    private final Image image;
    private final AnimatedImage background;

    private float currentOpacity = 0f;

    // Внутренний точный счетчик кадра для плавной ручной анимации
    private float animationFrameCounter = 0f;

    public DialogueView(DialogViewAvatarFactory avatarFactory, TypingLabel text, Image image, AnimatedImage background) {
        this.avatarFactory = avatarFactory;
        this.label = text;
        this.image = image;
        this.background = background;
    }

    /**
     * ПЛАВНОЕ ОТКРЫТИЕ (Идем от ПОСЛЕДНЕГО кадра к ПЕРВОМУ)
     * Последний кадр = окно закрыто (opacity = 0).
     * Первый кадр (0) = окно открыто (opacity = 1).
     */
    public boolean fadeIn(float delta) {
        Animation<?> anim = background.getAnimation();
        int totalFrames = anim.getKeyFrames().length;
        int maxIndex = totalFrames - 1;

        // Наращиваем счетчик времени/кадров вперед
        animationFrameCounter += delta / anim.getFrameDuration();

        // Вычисляем шаг от 0 до maxIndex
        int step = (int) animationFrameCounter;

        // Зеркалим индекс: при шаге 0 покажем maxIndex, при максимальном шаге — кадр 0
        int targetFrame = maxIndex - step;

        if (targetFrame <= 0) {
            targetFrame = 0;
            background.setFrameAndPause(targetFrame);
            currentOpacity = 1f;
            return true; // Полностью открылись (пришли к 0 кадру)
        }

        background.setFrameAndPause(targetFrame);
        // Прозрачность растет по мере того, как мы приближаемся к 0 кадру
        currentOpacity = (float) step / maxIndex;
        return false;
    }

    public void update(String actorName, String text) {
        image.setDrawable(new TextureRegionDrawable(avatarFactory.getAvatar(actorName)));
        label.setText(text);
        label.restart();
    }

    /**
     * Вызывается один раз при старте закрытия в DialogueSystem.
     * Сбрасывает счетчик в 0, чтобы начать отсчет кадров заново.
     */
    public void startDialogueClose() {
        animationFrameCounter = 0f;
        image.setDrawable(null);
        label.setText("");
    }

    /**
     * ПЛАВНОЕ ЗАКРЫТИЕ (Идем от ПЕРВОГО кадра к ПОСЛЕДНЕМУ)
     * Первый кадр (0) = окно открыто (opacity = 1).
     * Последний кадр = окно закрыто (opacity = 0).
     */
    public boolean fadeOut(float delta) {
        Animation<?> anim = background.getAnimation();
        int totalFrames = anim.getKeyFrames().length;
        int maxIndex = totalFrames - 1;

        // Наращиваем счетчик кадров вперед
        animationFrameCounter += delta / anim.getFrameDuration();

        // Прямой индекс: от 0 до maxIndex
        int targetFrame = (int) animationFrameCounter;

        if (targetFrame >= maxIndex) {
            background.setFrameAndPause(maxIndex);
            currentOpacity = 0f;
            return true; // Полностью закрылись (пришли к последнему кадру)
        }

        background.setFrameAndPause(targetFrame);
        // Прозрачность падает от 1.0 до 0.0 по мере движения к последнему кадру
        currentOpacity = 1f - ((float) targetFrame / maxIndex);
        return false;
    }

    public float getCurrentOpacity() {
        return currentOpacity;
    }

    public boolean isTextFullyDisplayed() {
        return label.hasEnded();
    }

    public void skipTextAnimation() {
        label.skipToTheEnd();
    }

    /** Сброс состояния для будущих диалогов */
    public void reset() {
        animationFrameCounter = 0f;
        currentOpacity = 0f;
        background.reset();
    }
}
