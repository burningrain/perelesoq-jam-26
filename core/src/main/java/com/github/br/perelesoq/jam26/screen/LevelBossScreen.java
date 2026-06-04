package com.github.br.perelesoq.jam26.screen;

import com.badlogic.gdx.maps.MapLayer;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.UserFactoryImpl;
import com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.RenderSystem;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;
import com.github.br.perelesoq.jam26.render.ui.CustomOrthogonalTiledMapRenderer;

public class LevelBossScreen extends AbstractLevelScreen {

    @Override
    protected void showLevel(UserFactoryImpl userFactory) {
        //HeroSingletonComponent.INSTANCE.hasWeapon = true;

        RenderSystem system = userFactory.ecsWorld.getSystem(RenderSystem.class);
        CustomOrthogonalTiledMapRenderer renderer = system.getRenderer();

        MapLayer backBlackLayer = renderer.getLayer(TiledUiConstants.Layers.BACKGROUND_BLACK);
        backBlackLayer.setVisible(true);

        MapLayer backWhiteLayer = renderer.getLayer(TiledUiConstants.Layers.BACKGROUND_WHITE);
        backWhiteLayer.setVisible(false);

        //TODO подвинуть актор на правильную позицию. хз какую, надо выяснять
    }

    @Override
    protected String getTiledMap() {
        return Resources.Tiled.LEVEL_BOSS;
    }

}
