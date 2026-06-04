package com.github.br.perelesoq.jam26.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.perelesoq.jam26.Constants;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.ecs.system.base.input.MyControllerMapping;
import com.github.br.perelesoq.jam26.structure.AudioSettings;
import com.github.br.perelesoq.jam26.structure.audio.AudioAssetManager;
import com.github.br.perelesoq.jam26.structure.screen.AbstractGameScreen;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.mapping.ControllerToInputAdapter;

public class MainScreen extends AbstractGameScreen {

    private SpriteBatch spriteBatch;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture texture;
    private Music music;

    private Skin skin;
    private ControllerMenuStage stage;

    // Сохраняем адаптер как поле класса, чтобы его не удалил Garbage Collector
    private ControllerToInputAdapter menuAdapter;

    @Override
    public void show() {
        AssetManager assetManager = getGameManager().assetManager;
        skin = assetManager.get(Resources.SKIN);

        spriteBatch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        GameScreenUtils.centerCamera(camera);

        // createStage теперь также настроит menuAdapter внутри себя
        stage = createStage();

        // Инициализируем контроллеры, чтобы gdx-controllers проснулся
        Controllers.addListener(menuAdapter);

        // КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: Регистрируем процессор адаптера, а не сам stage!
        // Теперь LibGDX будет слать события в адаптер, а адаптер перенаправит их в stage.
        Gdx.input.setInputProcessor(menuAdapter.getInputProcessor());

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
                //getGameManager().screenStateManager.changeCurrentState(Screens.LEVEL_BOSS);
                getGameManager().screenStateManager.changeCurrentState(Screens.LEVEL_1);
            }
        });

//        ImageTextButton settingsButton = new ImageTextButton("Настройки", skin);
//        settingsButton.addListener(new ChangeListener() {
//            @Override
//            public void changed(ChangeEvent event, Actor actor) {
//                System.out.println("настройки");
//            }
//        });
//
        ImageTextButton aboutButton = new ImageTextButton("Титры", skin);
        aboutButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("титры");
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center().bottom().padBottom(6f);

//        table.add(settingsButton).left();
        table.add(playButton).spaceLeft(8f);
//        table.add(aboutButton).spaceLeft(8f);

        ControllerMenuStage stage = new ControllerMenuStage(viewport);
        stage.addActor(table);

        aboutButton.validate();
        //stage.addFocusableActor(settingsButton);
        stage.addFocusableActor(playButton);
        stage.addFocusableActor(aboutButton);

        stage.setFocusedActor(playButton);

        // Создаем обновленный маппинг
        MyControllerMapping myMapping = new MyControllerMapping();
        menuAdapter = new ControllerToInputAdapter(myMapping);

        // 1. Управление с аналогового левого стика (как было)
        menuAdapter.addAxisMapping(MyControllerMapping.AXIS_LEFT_X, Input.Keys.LEFT, Input.Keys.RIGHT);
        menuAdapter.addAxisMapping(MyControllerMapping.AXIS_LEFT_Y, Input.Keys.UP, Input.Keys.DOWN);

        // 2. ДОБАВЛЯЕМ управление с крестовины (D-Pad) геймпада
        // Теперь нажатия на физический D-Pad будут слать те же самые стрелочки в ControllerMenuStage
        menuAdapter.addButtonMapping(MyControllerMapping.DPAD_LEFT, Input.Keys.LEFT);
        menuAdapter.addButtonMapping(MyControllerMapping.DPAD_RIGHT, Input.Keys.RIGHT);
        menuAdapter.addButtonMapping(MyControllerMapping.DPAD_UP, Input.Keys.UP);
        menuAdapter.addButtonMapping(MyControllerMapping.DPAD_DOWN, Input.Keys.DOWN);

        // 3. Функциональные кнопки (A, B, Start, Back)
        menuAdapter.addButtonMapping(MyControllerMapping.BUTTON_A, Input.Keys.ENTER);
        menuAdapter.addButtonMapping(MyControllerMapping.BUTTON_START, Input.Keys.ENTER);
        menuAdapter.addButtonMapping(MyControllerMapping.BUTTON_B, Input.Keys.ESCAPE);
        menuAdapter.addButtonMapping(MyControllerMapping.BUTTON_BACK, Input.Keys.ESCAPE);

        // Направляем транслируемые клавиши клавиатуры в Stage меню
        menuAdapter.setInputProcessor(stage);

        return stage;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        spriteBatch.setProjectionMatrix(camera.combined);

        spriteBatch.begin();
        spriteBatch.draw(texture, 0, 0);
        spriteBatch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        GameScreenUtils.centerCamera(camera);
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

        // ОЧИСТКА: Обязательно убираем слушателей, иначе при возвращении в меню
        // или переходе на LEVEL_1 старый адаптер будет дублировать нажатия в фоне
        Gdx.input.setInputProcessor(null);
        if (menuAdapter != null) {
            Controllers.removeListener(menuAdapter);
        }
    }

    @Override
    public void dispose() {
        music = null;
        texture = null;
        if (stage != null) {
            stage.dispose();
        }
    }
}
