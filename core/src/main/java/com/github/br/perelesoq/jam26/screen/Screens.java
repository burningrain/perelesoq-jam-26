package com.github.br.perelesoq.jam26.screen;

import com.github.br.perelesoq.jam26.structure.screen.statemachine.GameScreenState;

public interface Screens {

    GameScreenState MAIN = new GameScreenState(
        new MainScreen(), new MainScreenLoader()
    );

}
