package com.github.br.perelesoq.jam26.render;

public interface TiledUiConstants {

    interface Layers {
        String GAME_OBJECTS_LAYER = "game_objects";
        String TRIGGERS_LAYER = "triggers";
        String ACTORS_LAYER = "actors";
        String BACK_BLUE = "back_blue";

        String DIALOG_GROUP = "dialog_group";
        String DIALOG_ACTORS = "dialog_actors";
        String DIALOG_BACK = "dialog_back";

        String BACKGROUND_WHITE = "background_white";
        String BACKGROUND_BLACK = "background_black";

    }

    interface Actors {
        String CAMERA = "camera";
        String ELEVATOR = "elevator";
        String CONTROLLER = "controller";
        String TERMINAL = "terminal";
        String DOOR = "door";

        String PLATFORM = "platform";
        String BACK_BOX = "back_box";
        String BOSS_ACTOR = "boss_actor";

        interface DIALOG {
            String AVATAR = "avatar";
            String TEXT = "text";
            String WINDOW = "dialog_window";
        }
    }

}
