package com.github.br.perelesoq.jam26.ecs.system.dialog;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.github.br.perelesoq.jam26.ecs.component.render.ChangeRenderLayerComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.DialogueSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.system.InputSystemImpl;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.RenderSystem;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;
import com.github.br.perelesoq.jam26.render.ui.AnimatedImage;

public class DialogueSystem extends BaseSystem {

    private final DialogueView dialogueView;

    private final InputSystemImpl inputSystem;
    private final RenderSystem renderSystem;

    private int dialogueUiEntityId = -1;

    protected ComponentMapper<ChangeRenderLayerComponent> mLayerChange;

    public DialogueSystem(
        InputSystemImpl inputSystem,
        RenderSystem renderSystem,
        DialogViewAvatarFactory avatarFactory
    ) {
        this.inputSystem = inputSystem;
        this.renderSystem = renderSystem;
        this.dialogueView = new DialogueView(
            avatarFactory,
            renderSystem.getRenderer().getActor(
                TiledUiConstants.Layers.DIALOG_ACTORS,
                TiledUiConstants.Actors.DIALOG.TEXT,
                Label.class //TypingLabel.class
            ),
            renderSystem.getRenderer().getActor(
                TiledUiConstants.Layers.DIALOG_ACTORS,
                TiledUiConstants.Actors.DIALOG.AVATAR,
                Image.class
            ),
            renderSystem.getRenderer().getActor(
                TiledUiConstants.Layers.DIALOG_BACK,
                TiledUiConstants.Actors.DIALOG.WINDOW,
                AnimatedImage.class
            )
        );
    }

    @Override
    protected void processSystem() {
        DialogueSingletonComponent dialogue = DialogueSingletonComponent.INSTANCE;

        // --- 1. ЕСЛИ ДИАЛОГ ЗАКРЫТ ---
        if (!dialogue.isActive) {
            if (dialogueUiEntityId != -1 && world.getEntityManager().isActive(dialogueUiEntityId)) {
                ChangeRenderLayerComponent cmd = mLayerChange.get(dialogueUiEntityId);
                if (cmd != null) {
                    cmd.isVisible = false;
                    cmd.opacity = 0f;
                    cmd.isDirty = true;
                }
                world.delete(dialogueUiEntityId);
            }
            dialogueUiEntityId = -1;

            renderSystem.getRenderer().getLayer(TiledUiConstants.Layers.DIALOG_GROUP).setVisible(false);
            return;
        }

        // --- 2. ИНИЦИАЛИЗАЦИЯ ОКНА (Плавное появление) ---
        if (dialogueUiEntityId == -1 || !world.getEntityManager().isActive(dialogueUiEntityId))   {
            renderSystem.getRenderer().getLayer(TiledUiConstants.Layers.DIALOG_GROUP).setVisible(true);
            boolean isFadeInFinished = dialogueView.fadeIn();
            if (!isFadeInFinished) {
                return;
            }
        }

        // --- 3. ОБНОВЛЕНИЕ ДАННЫХ В UI АКТУАЛЬНОЙ ФРАЗЫ ---
        DialogueSingletonComponent.Phrase currentPhrase = dialogue.getCurrentPhrase();
        dialogueView.update(currentPhrase.avatar, currentPhrase.text);
        Runnable reaction = currentPhrase.reaction;
        if (reaction != null) {
            reaction.run();
        }

        // --- 4. ПЕРЕКЛЮЧЕНИЕ ФРАЗ ЧЕРЕЗ РЕЕСТР ВВОДА (Без Gdx.input) ---
        // Используем метод inputRegistry из вашей системы ввода.
        // Если внутри AbstractInputSystem есть геймпад/контроллер, опрашиваем через него:
        if (inputSystem.isAnyActionJustPressed()) {
            boolean hasNext = dialogue.nextPhrase();
            if (!hasNext) {
                boolean isFadeOutFinished = dialogueView.fadeOut();
                if (isFadeOutFinished) {
                    // Диалог завершен, слои закроются на следующем кадре,
                    // а Level1Screen автоматически разморозит игру
                    dialogueUiEntityId = -1;
                    renderSystem.getRenderer().getLayer(TiledUiConstants.Layers.DIALOG_GROUP).setVisible(false);
                    DialogueSingletonComponent.INSTANCE.isActive = false;
                }
            }
        }
    }
}
