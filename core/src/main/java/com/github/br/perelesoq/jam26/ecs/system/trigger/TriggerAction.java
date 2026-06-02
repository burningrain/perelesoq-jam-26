package com.github.br.perelesoq.jam26.ecs.system.trigger;


public interface TriggerAction {

    // Вызывается, когда игрок заходит в зону триггера
    void onEnter(int playerEntityId, int triggerEntityId);

    // Вызывается, когда игрок выходит из зоны триггера
    void onExit(int playerEntityId, int triggerEntityId);

    // Вызывается, если игрок нажал кнопку действия (GameAction.EXECUTE / клавиша "E") внутри зоны
    void onExecute(int playerEntityId, int triggerEntityId);

}
