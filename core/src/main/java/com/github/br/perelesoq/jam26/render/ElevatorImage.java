package com.github.br.perelesoq.jam26.render;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.github.br.perelesoq.jam26.render.ui.AnimatedImage;

public class ElevatorImage extends Group {

    private final AnimatedImage doors;
    private final AnimatedImage animationHero;

    public ElevatorImage(
        Image imageElevatorBack,
        AnimatedImage animationHero,
        AnimatedImage animatedImageElevatorDoors,
        Image imageElevatorFront
    ) {
        this.doors = animatedImageElevatorDoors;
        this.animationHero = animationHero;

        // 1. Сначала принудительно задаем размеры акторов под размеры их текстур.
        // Если этого не сделать, getWidth() и getHeight() вернут 0.
        imageElevatorBack.setSize(imageElevatorBack.getPrefWidth(), imageElevatorBack.getPrefHeight());
        animatedImageElevatorDoors.setSize(animatedImageElevatorDoors.getPrefWidth(), animatedImageElevatorDoors.getPrefHeight());
        imageElevatorFront.setSize(imageElevatorFront.getPrefWidth(), imageElevatorFront.getPrefHeight());

        // 2. Устанавливаем размер самого Group равным размеру фоновой картинки лифта
        this.setSize(imageElevatorBack.getWidth(), imageElevatorBack.getHeight());

        // 3. Добавляем актеров в группу
        this.addActor(imageElevatorBack);
        this.addActor(animationHero);
        this.addActor(animatedImageElevatorDoors);

        this.addActor(imageElevatorFront);

        // 4. Задний фон оставляем в нулевых координатах группы (0, 0)
        imageElevatorBack.setPosition(0, 0);
        imageElevatorBack.setOrigin(imageElevatorBack.getWidth() / 2f, imageElevatorBack.getHeight() / 2f);

        // 4.5. Центрируем двери (doors) относительно фона
        float heroX = (imageElevatorBack.getWidth() - animatedImageElevatorDoors.getWidth()) / 2f;
        float heroY = (imageElevatorBack.getHeight() - animatedImageElevatorDoors.getHeight()) / 2f - 1; // 1 - погрешность при округлении после деления
        animationHero.setPosition(heroX, heroY);

        // 5. Центрируем двери (doors) относительно фона
        float doorsX = (imageElevatorBack.getWidth() - animatedImageElevatorDoors.getWidth()) / 2f;
        float doorsY = (imageElevatorBack.getHeight() - animatedImageElevatorDoors.getHeight()) / 2f - 1; // 1 - погрешность при округлении после деления
        animatedImageElevatorDoors.setPosition(doorsX, doorsY);

        // 6. Центрируем переднюю панель (front) относительно фона
        float frontX = (imageElevatorBack.getWidth() - imageElevatorFront.getWidth()) / 2f;
        float frontY = (imageElevatorBack.getHeight() - imageElevatorFront.getHeight()) / 2f - 1; // 1 - погрешность при округлении после деления
        imageElevatorFront.setPosition(frontX, frontY);
    }

    public AnimatedImage getDoors() {
        return doors;
    }

}
