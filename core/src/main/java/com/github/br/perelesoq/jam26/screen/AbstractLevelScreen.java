package com.github.br.perelesoq.jam26.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.github.br.perelesoq.jam26.UserFactoryImpl;
import com.github.br.perelesoq.jam26.ecs.component.singleton.ViewPortSingletonComponent;
import com.github.br.perelesoq.jam26.structure.GameManager;
import com.github.br.perelesoq.jam26.structure.screen.AbstractGameScreen;

public abstract class AbstractLevelScreen extends AbstractGameScreen {

    private UserFactoryImpl userFactory;

    @Override
    public void show() {
        GameManager gameManager = getGameManager();
        AssetManager assetManager = getGameManager().assetManager;
        TiledMap tiledMap = assetManager.get(getTiledMap());

        userFactory = (UserFactoryImpl) gameManager.userFactory; //TODO здесь каст, надо убрать
        userFactory.resetCurrentLevel(tiledMap);

        GameScreenUtils.centerCamera(ViewPortSingletonComponent.INSTANCE.camera);

        showLevel(userFactory);
    }

    protected abstract void showLevel(UserFactoryImpl userFactory);

    protected abstract String getTiledMap();

    @Override
    public void render(float delta) {
        userFactory.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        ViewPortSingletonComponent.INSTANCE.viewPort.update(width, height);
        GameScreenUtils.centerCamera(ViewPortSingletonComponent.INSTANCE.camera);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

}
