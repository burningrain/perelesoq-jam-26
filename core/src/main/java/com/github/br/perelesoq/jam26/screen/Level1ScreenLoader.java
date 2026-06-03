package com.github.br.perelesoq.jam26.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.github.ashvard.gdx.simple.animation.SimpleAnimationFsmLoadedCallback;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.structure.screen.loading.AssetsLoader;

public class Level1ScreenLoader implements AssetsLoader {

    @Override
    public void loadAssets(AssetManager assetManager) {
        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();

        params.textureMinFilter = Texture.TextureFilter.Nearest;
        params.textureMagFilter = Texture.TextureFilter.Nearest;
        assetManager.load(Resources.Tiled.LEVEL_1_ENTRANCE, TiledMap.class, params);

        TextureAtlasLoader.TextureAtlasParameter gameObjectsAtlasParams = new TextureAtlasLoader.TextureAtlasParameter();
        gameObjectsAtlasParams.loadedCallback = new SimpleAnimationFsmLoadedCallback(
            Resources.Atlases.GAME_OBJECTS,
            new SimpleAnimationFsmLoadedCallback.SimpleAnimationPath[]{
                new SimpleAnimationFsmLoadedCallback.SimpleAnimationPath(
                    Resources.Animations.HERO_ANIM_FSM,
                    Resources.Animations.HERO
                ),
                new SimpleAnimationFsmLoadedCallback.SimpleAnimationPath(
                    Resources.Animations.DOOR_ANIM_FSM,
                    Resources.Animations.DOOR
                )
            }
        );
        assetManager.load(Resources.Atlases.GAME_OBJECTS, TextureAtlas.class, gameObjectsAtlasParams);

        assetManager.load(Resources.Sound.SIREN, Sound.class);
        assetManager.load(Resources.Sound.BIG_DOOR, Sound.class);
    }

    @Override
    public void unloadAssets(AssetManager assetManager) {
        assetManager.unload(Resources.Tiled.LEVEL_1_ENTRANCE);
        assetManager.unload(Resources.Sound.SIREN);
        assetManager.unload(Resources.Sound.BIG_DOOR);
    }

}
