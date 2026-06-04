package com.github.br.perelesoq.jam26.ecs.system.dialog;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.github.br.perelesoq.jam26.ecs.component.ui.render.ChangeRenderLayerComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.DialogueSingletonComponent;
import com.github.br.perelesoq.jam26.ecs.system.InputSystemImpl;
import com.github.br.perelesoq.jam26.ecs.system.base.ui.RenderSystem;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;
import com.github.br.perelesoq.jam26.render.ui.AnimatedImage;
import com.github.tommyettinger.textra.TypingLabel;

public class DialogueSystem extends BaseSystem {

    private DialogueView dialogueView;

    private final InputSystemImpl inputSystem;
    private final RenderSystem renderSystem;

    private int dialogueUiEntityId = -1;

    // ОПТИМИЗАЦИЯ: Храним индекс последней успешно обработанной фразы
    private int lastProcessedPhraseIndex = -1;

    // Флаг, указывающий, что мы сейчас находимся в процессе закрытия окна (Fade-out)
    private boolean isClosingPhase = false;

    protected ComponentMapper<ChangeRenderLayerComponent> mLayerChange;

    private DialogViewAvatarFactory avatarFactory;

    public void refreshDialogView() {
        this.dialogueView = new DialogueView(
            avatarFactory,
            renderSystem.getRenderer().getActor(
                TiledUiConstants.Layers.DIALOG_ACTORS,
                TiledUiConstants.Actors.DIALOG.TEXT,
                TypingLabel.class
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

    public DialogueSystem(
        InputSystemImpl inputSystem,
        RenderSystem renderSystem,
        DialogViewAvatarFactory avatarFactory
    ) {
        this.avatarFactory = avatarFactory;
        this.inputSystem = inputSystem;
        this.renderSystem = renderSystem;
        refreshDialogView();
    }

    @Override
    protected void processSystem() {
        DialogueSingletonComponent dialogue = DialogueSingletonComponent.INSTANCE;
        float delta = world.getDelta(); // Получаем дельту времени кадра

        // --- 1. ЛОГИКА ЗАКРЫТИЯ ОКНА (FADE-OUT) ---
        if (isClosingPhase) {
            boolean isFadeOutFinished = dialogueView.fadeOut(delta); // Передаем дельту

            if (dialogueUiEntityId != -1 && world.getEntityManager().isActive(dialogueUiEntityId)) {
                ChangeRenderLayerComponent cmd = mLayerChange.get(dialogueUiEntityId);
                if (cmd != null) {
                    cmd.opacity = dialogueView.getCurrentOpacity();
                    cmd.isDirty = true;
                }
            }

            if (isFadeOutFinished) {
                if (dialogueUiEntityId != -1 && world.getEntityManager().isActive(dialogueUiEntityId)) {
                    world.delete(dialogueUiEntityId);
                }
                isClosingPhase = false;
                dialogueUiEntityId = -1;
                renderSystem.getRenderer().getLayer(TiledUiConstants.Layers.DIALOG_GROUP).setVisible(false);
                DialogueSingletonComponent.INSTANCE.isActive = false;
                lastProcessedPhraseIndex = -1;
                dialogueView.reset(); // Чистим вьюху для следующего использования
            }
            return;
        }

        // --- 2. ЕСЛИ ДИАЛОГ ПРЕРВАН ИЗВНЕ ---
        if (!dialogue.isActive) {
            if (dialogueUiEntityId != -1 && world.getEntityManager().isActive(dialogueUiEntityId)) {
                world.delete(dialogueUiEntityId);
            }
            dialogueUiEntityId = -1;
            lastProcessedPhraseIndex = -1;
            renderSystem.getRenderer().getLayer(TiledUiConstants.Layers.DIALOG_GROUP).setVisible(false);
            dialogueView.reset();
            return;
        }

        // --- 3. ИНИЦИАЛИЗАЦИЯ ОКНА И АНИМАЦИЯ ПОЯВЛЕНИЯ (FADE-IN) ---
        if (dialogueUiEntityId == -1 || !world.getEntityManager().isActive(dialogueUiEntityId)) {
            renderSystem.getRenderer().getLayer(TiledUiConstants.Layers.DIALOG_GROUP).setVisible(true);
            dialogueUiEntityId = world.create();
            ChangeRenderLayerComponent cmd = mLayerChange.create(dialogueUiEntityId);
            cmd.layerName = TiledUiConstants.Layers.DIALOG_GROUP;
            cmd.opacity = 0f;
            cmd.isVisible = true;
            cmd.isDirty = true;
        }

        // Передаем дельту кадра для точного расчета
        boolean isFadeInFinished = dialogueView.fadeIn(delta);

        ChangeRenderLayerComponent cmd = mLayerChange.get(dialogueUiEntityId);
        if (cmd != null) {
            cmd.opacity = dialogueView.getCurrentOpacity();
            cmd.isDirty = true;
        }

        if (!isFadeInFinished) {
            return; // Пока окно открывается, текст не рендерим
        }

        // --- 4. ОБНОВЛЕНИЕ ТЕКСТА РЕПЛИКИ (СТРОГО 1 РАЗ) ---
        int currentIdx = dialogue.currentPhraseIndex;
        if (currentIdx != lastProcessedPhraseIndex) {
            DialogueSingletonComponent.Phrase currentPhrase = dialogue.getCurrentPhrase();
            if (currentPhrase != null) {
                dialogueView.update(currentPhrase.avatar, currentPhrase.text);
                Runnable reaction = currentPhrase.reaction;
                if (reaction != null) {
                    reaction.run();
                }
            }
            lastProcessedPhraseIndex = currentIdx;
        }

        // --- 5. УМНОЕ ПЕРЕКЛЮЧЕНИЕ ФРАЗ ---
        if (inputSystem.isAnyActionJustPressed()) {
            if (!dialogueView.isTextFullyDisplayed()) {
                dialogueView.skipTextAnimation();
            } else {
                boolean hasNext = dialogue.nextPhrase();
                if (!hasNext) {
                    dialogueView.startDialogueClose(); // Инициализируем покадровый отсчет назад
                    isClosingPhase = true;
                }
            }
        }
    }
}
