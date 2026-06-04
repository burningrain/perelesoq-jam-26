package com.github.br.perelesoq.jam26.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.perelesoq.jam26.ecs.component.ChangeLevelIntentComponent;
import com.github.br.perelesoq.jam26.screen.Screens;
import com.github.br.perelesoq.jam26.structure.GameManager;
import com.github.br.perelesoq.jam26.structure.screen.statemachine.GameScreenState;

public class LevelTransitionSystem extends IteratingSystem {

    private ComponentMapper<ChangeLevelIntentComponent> mIntent;
    private final GameManager gameManager;

    public LevelTransitionSystem(GameManager gameManager) {
        super(Aspect.all(ChangeLevelIntentComponent.class));
        this.gameManager = gameManager;
    }

    @Override
    protected void process(int entityId) {
        ChangeLevelIntentComponent intent = mIntent.get(entityId);
        String nextLevel = intent.nextLevelKey;

        // Удаляем интент, чтобы не обрабатывать дважды
        world.delete(entityId);

        // Переключаем экран через ваш стейт-менеджер
        // Здесь должна быть ваша фабрика состояний (например, через фабрику или switch)
        GameScreenState nextState = getScreenStateByKey(nextLevel);

        if (nextState != null) {
            gameManager.screenStateManager.changeCurrentState(nextState);
        }
    }

    private GameScreenState getScreenStateByKey(String key) {
        switch (key) {
            case "MAIN":
                return Screens.MAIN;
            case "LEVEL_BOSS":
                return Screens.LEVEL_BOSS;
            default:
                throw new GdxRuntimeException("level [" + key + "] is not found");
        }
    }
}
