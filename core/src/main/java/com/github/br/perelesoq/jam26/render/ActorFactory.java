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
            case StageActorNames.CAMERA:
                return createCamera(properties);
            case StageActorNames.ELEVATOR:
                return createElevator(properties);
            case StageActorNames.CONTROLLER:
                return createController(properties);
            case StageActorNames.DOOR:
                return createDoor(properties);
            case StageActorNames.TERMINAL:
                return createTerminal(properties);
            default:
                throw new IllegalArgumentException("unknown stage2d actor: " + name);
        }
    }

    private Actor createDoor(MapProperties properties) {
        return createAnimationImage(StageActorNames.DOOR);
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
        animatedImageHero.play();

        TextureAtlas.AtlasRegion elevatorFront = commonAtlas.findRegion("elevator_front");
        Image imageElevatorFront = new Image(elevatorFront);

        Array<TextureAtlas.AtlasRegion> elevatorDoors = commonAtlas.findRegions("elevator_doors");
        Animation<TextureRegion> animation = new Animation<>(
            0.083f, elevatorDoors, Animation.PlayMode.LOOP_PINGPONG
        );

        AnimatedImage animatedImageElevatorDoors = new AnimatedImage(animation);
        animatedImageElevatorDoors.play();

        return new ElevatorImage(imageElevatorBack, animatedImageHero, animatedImageElevatorDoors, imageElevatorFront);
    }

    private Actor createCamera(MapProperties properties) {
        return createAnimationImage(StageActorNames.CAMERA);
    }

    private Actor createTerminal(MapProperties properties) {
        return createAnimationImage(StageActorNames.TERMINAL);
    }

    private AnimatedImage createAnimationImage(String name) {
        TextureAtlas commonAtlas = assetManager.get(Resources.Atlases.GAME_OBJECTS, TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> regions = commonAtlas.findRegions(name);

        Animation<TextureRegion> animation = new Animation<>(
            0.083f, regions, Animation.PlayMode.NORMAL
        );

        return new AnimatedImage(animation);
    }


}
