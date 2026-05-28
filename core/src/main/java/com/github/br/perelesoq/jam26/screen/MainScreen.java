package com.github.br.perelesoq.jam26.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.perelesoq.jam26.Constants;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.structure.AudioSettings;
import com.github.br.perelesoq.jam26.structure.audio.AudioAssetManager;
import com.github.br.perelesoq.jam26.structure.screen.AbstractGameScreen;
import de.golfgl.gdx.controllers.ControllerMenuStage;

public class MainScreen extends AbstractGameScreen {

    private SpriteBatch spriteBatch;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture texture;
    private Music music;

    private Skin skin;
    private ControllerMenuStage stage;

    @Override
    public void show() {
        AssetManager assetManager = getGameManager().assetManager;
        skin = assetManager.get(Resources.SKIN);

        spriteBatch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        centerCamera();

        stage = createStage();
        Gdx.input.setInputProcessor(stage);

        texture = assetManager.get(Resources.Pictures.MAIN_SCREEN, Texture.class);

        AudioSettings audioSettings = getGameManager().audioSettings;
        audioSettings.setMusicVolume(0.4f);
        AudioAssetManager audioAssetManager = getGameManager().audioAssetManager;

        music = audioAssetManager.getMusic(Resources.Music.MAIN_SCREEN_THEME);
        music.setLooping(true);
        music.play();
    }

    private ControllerMenuStage createStage() {
        ImageTextButton playButton = new ImageTextButton("Играть", skin);
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("играть");
            }
        });

        ImageTextButton settingsButton = new ImageTextButton("Настройки", skin);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("настройки");
            }
        });


        ImageTextButton aboutButton = new ImageTextButton("Титры", skin);
        aboutButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("титры");
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.bottom().padBottom(4f);

        table.add(settingsButton);
        table.add(playButton).spaceLeft(8f);
        table.add(aboutButton).spaceLeft(8f);
        ////

        ControllerMenuStage stage = new ControllerMenuStage(viewport);
        stage.addActor(table);

        aboutButton.validate();
        stage.addFocusableActor(settingsButton);
        stage.addFocusableActor(playButton);
        stage.addFocusableActor(aboutButton);

        stage.setFocusedActor(aboutButton);

        return stage;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        // Рисуем фон
        spriteBatch.begin();
        spriteBatch.draw(texture, 0, 0);
        spriteBatch.end();

        // Рисуем UI (он сам управляет своим batch)
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        centerCamera();
    }

    @Override
    public void pause() {
        music.pause();
    }

    @Override
    public void resume() {
        music.play();
    }

    @Override
    public void hide() {
        music.pause();
    }

    @Override
    public void dispose() {
        music = null;
        texture = null;
    }

    private void centerCamera() {
        camera.position.set(Constants.WORLD_WIDTH / 2f, Constants.WORLD_HEIGHT / 2f, 0);
    }

}
