package com.github.br.perelesoq.jam26;

public interface Resources {

    String SKIN_ATLAS = "skin/export/perelesoq_26_jam.atlas";
    String SKIN = "skin/export/perelesoq_26_jam.json";

    interface Pictures {
        String MAIN_SCREEN = "pictures/menu/menu_screen2.png";
    }

    interface Music {
        String MAIN_SCREEN_THEME = "music/8_Bit_Video_Game_Fight_Music_by_bone666138.mp3";
    }

    interface Sound {
        String SIREN = "sfx/Freesound _alarmamtest1_by_jilgueroo.mp3";
    }

    interface Tiled {

        String LEVEL_1_ENTRANCE = "tiled-packed/entrance.tmx";

    }

    interface Atlases {
        String GAME_OBJECTS = "pictures/atlas/game-objects.atlas";

        interface Objects {
            String AVATAR_HERO = "dialog_hero";
            String AVATAR_COMPUTER = "dialog_computer";
        }
    }

    interface Animations {
        String HERO_ANIM_FSM = "animations/hero.afsm";
        String HERO = "hero";
    }

}
