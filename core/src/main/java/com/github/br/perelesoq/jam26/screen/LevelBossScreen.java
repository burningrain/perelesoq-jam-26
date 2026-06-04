package com.github.br.perelesoq.jam26.screen;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.utils.Array;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.UserFactoryImpl;
import com.github.br.perelesoq.jam26.dialogs.DialogFactory;
import com.github.br.perelesoq.jam26.ecs.component.SpawnObjectIntentComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.DialogueSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.RenderSystem;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;
import com.github.br.perelesoq.jam26.render.ui.AnimatedImage;
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

        AnimatedImage bossActor = renderer.getActor(
            TiledUiConstants.Layers.ACTORS_LAYER, TiledUiConstants.Actors.BOSS_ACTOR, AnimatedImage.class
        );
        bossActor.setVisible(false);

        HeroSingletonComponent.INSTANCE.hasWeapon = true;

        DialogFactory dialogFactory = userFactory.dialogFactory;
        DialogueSingletonComponent.INSTANCE.start(dialogFactory.boss_opening());
    }

    @Override
    protected String getTiledMap() {
        return Resources.Tiled.LEVEL_BOSS;
    }

}
