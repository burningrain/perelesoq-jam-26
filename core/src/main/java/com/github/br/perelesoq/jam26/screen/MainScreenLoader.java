package com.github.br.perelesoq.jam26.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.structure.screen.loading.AssetsLoader;

public class MainScreenLoader implements AssetsLoader {

    @Override
    public void loadAssets(AssetManager assetManager) {
        assetManager.load(Resources.Pictures.MAIN_SCREEN, Texture.class);
        assetManager.load(Resources.Music.MAIN_SCREEN_THEME, Music.class);
    }

    @Override
    public void unloadAssets(AssetManager assetManager) {
        assetManager.unload(Resources.Pictures.MAIN_SCREEN);
        assetManager.unload(Resources.Music.MAIN_SCREEN_THEME);
    }

}
