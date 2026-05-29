package com.github.br.perelesoq.jam26.screen;

import com.github.br.perelesoq.jam26.structure.screen.statemachine.GameScreenState;

public interface Screens {

    GameScreenState MAIN = new GameScreenState(
        new MainScreen(), new MainScreenLoader()
    );

    GameScreenState LEVEL_1 = new GameScreenState(
        new Level1Screen(), new Level1ScreenLoader()
    );

}
