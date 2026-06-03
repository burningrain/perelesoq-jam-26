package com.github.br.perelesoq.jam26.ecs.system.dialog;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.perelesoq.jam26.Resources;

public class DialogViewAvatarFactory {

    private final AssetManager assetManager;

    public DialogViewAvatarFactory(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public TextureRegion getAvatar(String textureName) {
        TextureAtlas commonAtlas = assetManager.get(Resources.Atlases.GAME_OBJECTS, TextureAtlas.class);
        TextureAtlas.AtlasRegion region = commonAtlas.findRegion(textureName);
        if (region == null) {
            throw new GdxRuntimeException("texture [" + textureName + "] is not found in atlas [" + Resources.Atlases.GAME_OBJECTS + "]");
        }

        return region;
    }

}
