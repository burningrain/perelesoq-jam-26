// FloatingButton.java
package com.github.br.perelesoq.jam26.render.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class FloatingButton extends TypingImageButton {

    private float originalX;
    private float originalY;
    private float amplitude;      // Амплитуда в пикселях
    private float speed;          // Скорость (радиан в секунду)
    private float phase;          // Фаза (для разнообразия движения)
    private float time;

    // Движение по кругу
    private boolean circularMotion = false;
    private float radiusX;
    private float radiusY;

    public FloatingButton(Skin skin, String styleName, String text, float amplitude, float speed, float phase) {
        super(text, skin, styleName);
        this.amplitude = amplitude;
        this.speed = speed;
        this.phase = phase;
        this.time = 0;
    }

    public void setCircularMotion(float radiusX, float radiusY) {
        this.circularMotion = true;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.amplitude = 0;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Сохраняем начальную позицию при первом обновлении
        if (time == 0 && getStage() != null) {
            originalX = getX();
            originalY = getY();
        }

        time += delta;

        float offsetX, offsetY;

        if (circularMotion) {
            // Движение по эллипсу
            offsetX = (float) (Math.cos(time * speed + phase) * radiusX);
            offsetY = (float) (Math.sin(time * speed + phase) * radiusY);
        } else {
            // Синусоидальное движение (восьмерка)
            offsetX = (float) (Math.sin(time * speed + phase) * amplitude);
            offsetY = (float) (Math.cos(time * speed * 0.7f + phase) * amplitude);
        }

        setPosition(originalX + offsetX, originalY + offsetY);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public void resetPosition() {
        time = 0;
        if (originalX != 0 || originalY != 0) {
            setPosition(originalX, originalY);
        }
    }

}
