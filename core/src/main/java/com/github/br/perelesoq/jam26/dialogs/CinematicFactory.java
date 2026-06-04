package com.github.br.perelesoq.jam26.dialogs;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.perelesoq.jam26.ecs.component.door.CloseDoorIntentComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.DialogueSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.HeroSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.cinematic.CinematicSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.cinematic.DelayStep;
import com.github.br.perelesoq.jam26.ecs.component.ui.render.RenderComponent;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.RenderSystem;
import com.github.br.perelesoq.jam26.render.ElevatorImage;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;
import com.github.br.perelesoq.jam26.render.ui.AnimatedImage;
import com.github.br.perelesoq.jam26.render.ui.CustomOrthogonalTiledMapRenderer;

public class CinematicFactory {

    public void start(Array<CinematicSingletonComponent.CinematicStep> script) {
        CinematicSingletonComponent.INSTANCE.start(script);
    }

    public Array<CinematicSingletonComponent.CinematicStep> startLevel1Cinematic() {
        Array<CinematicSingletonComponent.CinematicStep> script = new Array<>();
        script.add(new CinematicSingletonComponent.CinematicStep() {
            @Override
            public void onStart(World world) {
                Entity heroEntity = world.getEntity(HeroSingletonComponent.INSTANCE.playerId);
                RenderComponent component = heroEntity.getComponent(RenderComponent.class);
                component.isVisible = false;
            }

            @Override
            public boolean onUpdate(World world, float delta) {
                return true; // Мгновенный шаг, сразу переходим дальше
            }
        });
        script.add(new CinematicSingletonComponent.CinematicStep() {

            private ElevatorImage elevatorImage;

            @Override
            public void onStart(World world) {
                RenderSystem system = world.getSystem(RenderSystem.class);
                CustomOrthogonalTiledMapRenderer renderer = system.getRenderer();
                elevatorImage = renderer.getActor(
                    TiledUiConstants.Layers.ACTORS_LAYER,
                    TiledUiConstants.Actors.ELEVATOR,
                    ElevatorImage.class
                );
            }

            @Override
            public boolean onUpdate(World world, float delta) {
                float y = elevatorImage.getY();
                if (y <= 16) {
                    return true;
                }
                elevatorImage.setY(elevatorImage.getY() - delta * 25);
                return false;
            }
        });
        script.add(new CinematicSingletonComponent.CinematicStep() {

            private ElevatorImage elevatorImage;

            @Override
            public void onStart(World world) {
                RenderSystem system = world.getSystem(RenderSystem.class);
                CustomOrthogonalTiledMapRenderer renderer = system.getRenderer();
                elevatorImage = renderer.getActor(
                    TiledUiConstants.Layers.ACTORS_LAYER,
                    TiledUiConstants.Actors.ELEVATOR,
                    ElevatorImage.class
                );

                elevatorImage.getAnimationHero().play();
                elevatorImage.getDoors().play();
            }

            @Override
            public boolean onUpdate(World world, float delta) {
                AnimatedImage doors = elevatorImage.getDoors();
                return doors.isAnimationEnd();
            }
        });
        script.add(new DelayStep(2f));
        script.add(new CinematicSingletonComponent.CinematicStep() {

            private ElevatorImage elevatorImage;

            @Override
            public void onStart(World world) {
                RenderSystem system = world.getSystem(RenderSystem.class);
                CustomOrthogonalTiledMapRenderer renderer = system.getRenderer();
                elevatorImage = renderer.getActor(
                    TiledUiConstants.Layers.ACTORS_LAYER,
                    TiledUiConstants.Actors.ELEVATOR,
                    ElevatorImage.class
                );

                AnimatedImage animationHero = elevatorImage.getAnimationHero();
                animationHero.reset();
                animationHero.setVisible(false);
            }

            @Override
            public boolean onUpdate(World world, float delta) {
                return true;
            }
        });

        script.add(new CinematicSingletonComponent.CinematicStep() {
            @Override
            public void onStart(World world) {
                Entity heroEntity = world.getEntity(HeroSingletonComponent.INSTANCE.playerId);
                RenderComponent component = heroEntity.getComponent(RenderComponent.class);
                component.isVisible = true;
            }

            @Override
            public boolean onUpdate(World world, float delta) {
                return true; // Мгновенный шаг, сразу переходим дальше
            }
        });

        return script;
    }

    private Array<CinematicSingletonComponent.CinematicStep> endLevel1Cinematic() {
        Array<CinematicSingletonComponent.CinematicStep> script = new Array<>();
        script.add(new CinematicSingletonComponent.CinematicStep() {
            @Override
            public void onStart(World world) {
                Entity heroEntity = world.getEntity(HeroSingletonComponent.INSTANCE.playerId);
                RenderComponent component = heroEntity.getComponent(RenderComponent.class);
                component.isVisible = false;
            }

            @Override
            public boolean onUpdate(World world, float delta) {
                return true; // Мгновенный шаг, сразу переходим дальше
            }
        });
        script.add(new CinematicSingletonComponent.CinematicStep() {
            @Override
            public void onStart(World world) {
                int intentEntityId = world.create();
                CloseDoorIntentComponent closeIntent = world.getMapper(CloseDoorIntentComponent.class).create(intentEntityId);
                closeIntent.doorId = 1;
            }

            @Override
            public boolean onUpdate(World world, float delta) {
                return true; // Мгновенный шаг, сразу переходим дальше
            }
        });
        script.add(new DelayStep(1f));

        return script;
    }

    public Array<CinematicSingletonComponent.CinematicStep> bossIsDeadCinematic() {
        Array<CinematicSingletonComponent.CinematicStep> script = new Array<>();
        script.add(new CinematicSingletonComponent.CinematicStep() {

            private  AnimatedImage bossActor;

            @Override
            public void onStart(World world) {
                CustomOrthogonalTiledMapRenderer renderer = world.getSystem(RenderSystem.class).getRenderer();
                bossActor = renderer.getActor(TiledUiConstants.Layers.ACTORS_LAYER, TiledUiConstants.Actors.BOSS_ACTOR, AnimatedImage.class);
                bossActor.setVisible(true);
            }

            @Override
            public boolean onUpdate(World world, float delta) {
                return true;
            }
        });
        script.add(new CinematicSingletonComponent.CinematicStep() {
            @Override
            public void onStart(World world) {
                Array<DialogueSingletonComponent.Phrase> phrases = DialogFactory.boss_is_dead();
                DialogueSingletonComponent.INSTANCE.start(phrases);
            }

            @Override
            public boolean onUpdate(World world, float delta) {
                // Синематика НЕ перейдет к следующему шагу, пока диалог активен!
                // Как только игрок дочитает диалог и он закроется,
                // DialogueSingletonComponent.INSTANCE.isActive станет false, и синематика двинется дальше.
                return !DialogueSingletonComponent.INSTANCE.isActive;
            }
        });
        script.add(new CinematicSingletonComponent.CinematicStep() {

            private  AnimatedImage bossActor;

            @Override
            public void onStart(World world) {
                CustomOrthogonalTiledMapRenderer renderer = world.getSystem(RenderSystem.class).getRenderer();
                bossActor = renderer.getActor(
                    TiledUiConstants.Layers.ACTORS_LAYER, TiledUiConstants.Actors.BOSS_ACTOR, AnimatedImage.class
                );
            }

            @Override
            public boolean onUpdate(World world, float delta) {
                float y = bossActor.getY();
                if (y <= -160) {
                    return true;
                }
                bossActor.setY(bossActor.getY() - delta * 25);
                return false;
            }
        });
        script.add(new DelayStep(1f));

        return script;
    }

    public Array<CinematicSingletonComponent.CinematicStep> getCinematicScript(String cinematic) {
        switch (cinematic) {
            case "start_level_1":
            return startLevel1Cinematic();
            case "end_level_1":
                return endLevel1Cinematic();
            default:
                throw new GdxRuntimeException("cinematic [" + cinematic + "] is not found");
        }
    }


}
