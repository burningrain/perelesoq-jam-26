package com.github.br.perelesoq.jam26.screen;

import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.UserFactoryImpl;

public class LevelBossScreen extends AbstractLevelScreen {

    @Override
    protected void showLevel(UserFactoryImpl userFactory) {

    }

    @Override
    protected String getTiledMap() {
        return Resources.Tiled.LEVEL_BOSS;
    }

}
