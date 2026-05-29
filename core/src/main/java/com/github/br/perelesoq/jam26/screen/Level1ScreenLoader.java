package com.github.br.perelesoq.jam26.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.structure.screen.loading.AssetsLoader;

public class Level1ScreenLoader implements AssetsLoader {

    @Override
    public void loadAssets(AssetManager assetManager) {
        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();

        params.textureMinFilter = Texture.TextureFilter.Nearest;
        params.textureMagFilter = Texture.TextureFilter.Nearest;
        assetManager.load(Resources.Tiled.LEVEL_1, TiledMap.class, params);
    }

    @Override
    public void unloadAssets(AssetManager assetManager) {
        assetManager.unload(Resources.Tiled.LEVEL_1);
    }

}
