package com.github.br.perelesoq.jam26.screen;

import com.artemis.World;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.UserFactoryImpl;
import com.github.br.perelesoq.jam26.ecs.component.SpawnObjectIntentComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.Controller1SingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.RenderSystem;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;
import com.github.br.perelesoq.jam26.render.ui.CustomOrthogonalTiledMapRenderer;
import com.github.tommyettinger.textra.TypingLabel;

public class SettingsScreen extends AbstractLevelScreen {

    @Override
    protected void showLevel(UserFactoryImpl userFactory) {
        HeroSingletonComponent.INSTANCE.hasWeapon = true;
        Controller1SingletonComponent.INSTANCE.isActivated = true;

        World world = userFactory.ecsWorld;
        int intentId = world.create();
        world.edit(intentId).create(SpawnObjectIntentComponent.class).targetZoneName = "spawn1";

        RenderSystem system = userFactory.ecsWorld.getSystem(RenderSystem.class);
        CustomOrthogonalTiledMapRenderer renderer = system.getRenderer();

        TypingLabel textLabel = renderer.getActor(
            TiledUiConstants.Layers.ACTORS_LAYER, TiledUiConstants.Actors.DIALOG.TEXT, TypingLabel.class
        );
        textLabel.setText(
            "(A) - влево\n" +
                "(D) - вправо\n" +
                "(ПРОБЕЛ) - прыжок\n" +
                "(E) - взаимодействие\n" +
                "(ENTER) - стрельба\n" +
                "\n" +
                "* чтобы выстрелить, сперва возьмите ифовый куст\n" +
                "\n" +
                "* чтобы вернуться в меню подойдите к терминалу, нажмите (E) и пройдите через дверь\n" +
                "\n" +
                "p.s. геймпад поддерживается!"
        );
        textLabel.restart();
    }

    @Override
    protected String getTiledMap() {
        return Resources.Tiled.SETTINGS;
    }

    @Override
    protected void onFirstFrame(UserFactoryImpl userFactory) {
    }

}
