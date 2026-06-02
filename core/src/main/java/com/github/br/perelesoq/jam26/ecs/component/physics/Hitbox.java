package com.github.br.perelesoq.jam26.ecs.component.physics;

public class Hitbox {
    // Размеры исходного графического холста (например, 32f на 32f)
    public float canvasWidth;
    public float canvasHeight;

    // Чистые отступы от краев холста, когда персонаж НЕ развернут (смотрит вправо, стоит на ногах)
    public float paddingLeft;
    public float paddingRight;
    public float paddingTop;
    public float paddingBottom;

    public Hitbox(
        float canvasWidth,
        float canvasHeight,
        float paddingLeft,
        float paddingRight,
        float paddingTop,
        float paddingBottom
    ) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
    }

    /** Вычисление реальной физической ширины тела */
    public float getPhysicsWidth() {
        return canvasWidth - paddingLeft - paddingRight;
    }

    /** Вычисление реальной физической высоты тела */
    public float getPhysicsHeight() {
        return canvasHeight - paddingTop - paddingBottom;
    }
}
