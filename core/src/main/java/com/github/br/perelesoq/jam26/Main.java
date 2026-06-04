package com.github.br.perelesoq.jam26;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.ashvard.gdx.simple.animation.SimpleAnimation;
import com.github.ashvard.gdx.simple.animation.SimpleAnimationSyncLoader;
import com.github.br.perelesoq.jam26.screen.PreScreenAssetLoader;
import com.github.br.perelesoq.jam26.screen.Screens;
import com.github.br.perelesoq.jam26.structure.AbstractSimpleGame;
import com.github.br.perelesoq.jam26.structure.GameSettings;
import com.github.br.perelesoq.jam26.structure.screen.statemachine.GameScreenState;
import com.github.tommyettinger.textra.FWSkinLoader;

public class Main extends AbstractSimpleGame<UserFactoryImpl> {

    private PreScreenAssetLoader preScreenAssetLoader = new PreScreenAssetLoader();

    @Override
    protected UserFactoryImpl createUserFactory() {
        return new UserFactoryImpl();
    }

    @Override
    protected GameScreenState createStartState() {
        return Screens.MAIN;
    }

    @Override
    protected void initLoaders(AssetManager assetManager, InternalFileHandleResolver fileHandleResolver) {
        // графика
        assetManager.setLoader(Texture.class, new TextureLoader(fileHandleResolver));
        assetManager.setLoader(TextureAtlas.class, new TextureAtlasLoader(fileHandleResolver));
        //assetManager.setLoader(Skin.class, new FreeTypeSkinLoader(fileHandleResolver));

        // Регистрируем лоадер для FWSkin
        assetManager.setLoader(Skin.class, new FWSkinLoader(assetManager.getFileHandleResolver()));

        // анимация
        assetManager.setLoader(SimpleAnimation.class, new SimpleAnimationSyncLoader(fileHandleResolver));

        // эффекты частиц
        assetManager.setLoader(ParticleEffect.class, ".p", new ParticleEffectLoader(fileHandleResolver));

        // звук
        assetManager.setLoader(Sound.class, new SoundLoader(fileHandleResolver));
        assetManager.setLoader(Music.class, new MusicLoader(fileHandleResolver));

        // карты редакторов уровней
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(fileHandleResolver));
        assetManager.setLoader(TiledMap.class, new AtlasTmxMapLoader(fileHandleResolver));

        // шрифты
        assetManager.setLoader(BitmapFont.class, new FreetypeFontLoader(fileHandleResolver));

        //TODO вынести в абстрактный класс, который в свою очередь вынести уже в либу!
        loadCommonResources(assetManager);
    }

    private void loadCommonResources(AssetManager assetManager) {
        preScreenAssetLoader.loadAssets(assetManager);

        // skin
        assetManager.load(Resources.SKIN_ATLAS, TextureAtlas.class);
        assetManager.load(Resources.SKIN, Skin.class, new SkinLoader.SkinParameter(Resources.SKIN_ATLAS));

        Skin skin = assetManager.finishLoadingAsset(Resources.SKIN);
        BitmapFont font12 = skin.getFont("PressStart2P-Regular_12");
        font12.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        BitmapFont font8 = skin.getFont("PressStart2P-Regular_8");
        font8.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        assetManager.finishLoading();
    }

    @Override
    protected void fillGameSettings(GameSettings.Builder builder) {
        builder.setVirtualScreenWidth(Constants.WORLD_WIDTH);
        builder.setVirtualScreenHeight(Constants.WORLD_HEIGHT);
    }

}
