package com.github.br.perelesoq.jam26.render;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.github.br.perelesoq.jam26.Resources;
import com.github.br.perelesoq.jam26.render.ui.AnimatedImage;
import com.github.tommyettinger.textra.TypingLabel;

public class ActorFactory {

    private final Skin skin;
    private final AssetManager assetManager;

    public ActorFactory(Skin skin, AssetManager assetManager) {
        this.skin = skin;
        this.assetManager = assetManager;
    }

    public Actor getActor(MapObject object) {
        String name = object.getName();
        MapProperties properties = object.getProperties();

        switch (name) {
            case TiledUiConstants.Actors.CAMERA:
                return createCamera(properties);
            case TiledUiConstants.Actors.ELEVATOR:
                return createElevator(properties);
            case TiledUiConstants.Actors.CONTROLLER:
                return createController(properties);
            case TiledUiConstants.Actors.DOOR:
                return createDoor(properties);
            case TiledUiConstants.Actors.TERMINAL:
                return createTerminal(properties);
            case TiledUiConstants.Actors.DIALOG.WINDOW:
                return createDialogWindow(properties);
            case TiledUiConstants.Actors.DIALOG.AVATAR:
                return createDialogAvatar(properties);
            case TiledUiConstants.Actors.DIALOG.TEXT:
                return createDialogText(properties);

            // boss level
            case TiledUiConstants.Actors.PLATFORM:
                return createPlatform(properties);
            case TiledUiConstants.Actors.BACK_BOX:
                return createBackBox(properties);
            case TiledUiConstants.Actors.BOSS_ACTOR:
                return createBoss(properties);
            default:
                throw new IllegalArgumentException("unknown stage2d actor: " + name);
        }
    }

    private Actor createBoss(MapProperties properties) {
        return createAnimationImage("boss");
    }

    private Actor createBackBox(MapProperties properties) {
        return createAnimationImage(TiledUiConstants.Actors.BACK_BOX);
    }

    private Actor createPlatform(MapProperties properties) {
        return createAnimationImage("small_platform");
    }

    private Actor createDialogText(MapProperties properties) {
        TypingLabel label = new TypingLabel("", skin);
        label.setWrap(true);
        return label;
    }

    private Actor createDialogAvatar(MapProperties properties) {
        return new Image();
    }

    private Actor createDialogWindow(MapProperties properties) {
        return createAnimationImage(TiledUiConstants.Actors.DIALOG.WINDOW);
    }

    private Actor createDoor(MapProperties properties) {
        return createAnimationImage(TiledUiConstants.Actors.DOOR);
    }

    private Actor createController(MapProperties properties) {
        return createAnimationImage("elevator_controller");
    }

    private Actor createElevator(MapProperties properties) {
        TextureAtlas commonAtlas = assetManager.get(Resources.Atlases.GAME_OBJECTS, TextureAtlas.class);
        TextureAtlas.AtlasRegion elevatorBack = commonAtlas.findRegion("elevator_back");
        Image imageElevatorBack = new Image(elevatorBack);

        Array<TextureAtlas.AtlasRegion> hero = commonAtlas.findRegions("hero");
        Array<TextureAtlas.AtlasRegion> heroTraining = new Array<>();
        for (int i = (40 - 1); i < 43; i++) {
            heroTraining.add(hero.get(i));
        }
        Animation<TextureRegion> animationHero = new Animation<>(
            (1 / 7f), heroTraining, Animation.PlayMode.LOOP_PINGPONG
        );
        AnimatedImage animatedImageHero = new AnimatedImage(animationHero);

        TextureAtlas.AtlasRegion elevatorFront = commonAtlas.findRegion("elevator_front");
        Image imageElevatorFront = new Image(elevatorFront);

        Array<TextureAtlas.AtlasRegion> elevatorDoors = commonAtlas.findRegions("elevator_doors");
        Animation<TextureRegion> animation = new Animation<>(
            0.083f, elevatorDoors, Animation.PlayMode.NORMAL
        );

        AnimatedImage animatedImageElevatorDoors = new AnimatedImage(animation);
        return new ElevatorImage(imageElevatorBack, animatedImageHero, animatedImageElevatorDoors, imageElevatorFront);
    }

    private Actor createCamera(MapProperties properties) {
        AnimatedImage animationImage = createAnimationImage(TiledUiConstants.Actors.CAMERA);
        animationImage.getAnimation().setPlayMode(Animation.PlayMode.LOOP);
        animationImage.getAnimation().setFrameDuration(0.5f);
        animationImage.play();
        return animationImage;
    }

    private Actor createTerminal(MapProperties properties) {
        return createAnimationImage(TiledUiConstants.Actors.TERMINAL);
    }

    private AnimatedImage createAnimationImage(String name) {
        TextureAtlas commonAtlas = assetManager.get(Resources.Atlases.GAME_OBJECTS, TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> regions = commonAtlas.findRegions(name);

        Animation<TextureRegion> animation = new Animation<>(
            0.083f, regions, Animation.PlayMode.NORMAL
        );

        AnimatedImage animatedImage = new AnimatedImage(animation);
        return animatedImage;
    }

}
