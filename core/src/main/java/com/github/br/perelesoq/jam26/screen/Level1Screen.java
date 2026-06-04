package com.github.br.perelesoq.jam26.screen;

import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.UserFactoryImpl;
import com.github.br.perelesoq.jam26.dialogs.CinematicFactory;

public class Level1Screen extends AbstractLevelScreen {

    @Override
    protected void showLevel(UserFactoryImpl userFactory) {
        CinematicFactory cinematicFactory = userFactory.cinematicFactory;
        cinematicFactory.start(cinematicFactory.startLevel1Cinematic());
    }

    @Override
    protected String getTiledMap() {
        return Resources.Tiled.LEVEL_1_ENTRANCE;
    }

    @Override
    protected void onFirstFrame(UserFactoryImpl userFactory) {
    }

}
