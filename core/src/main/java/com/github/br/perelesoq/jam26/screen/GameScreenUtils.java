package com.github.br.perelesoq.jam26.screen;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.github.br.perelesoq.jam26.Constants;

public class GameScreenUtils {

    public static void centerCamera(OrthographicCamera camera) {
        camera.position.set(Constants.WORLD_WIDTH / 2f, Constants.WORLD_HEIGHT / 2f, 0);
    }


}
