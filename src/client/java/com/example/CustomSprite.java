package com.example;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;

public class CustomSprite extends Sprite {
    public CustomSprite(Identifier atlasId, SpriteContents contents, int atlasWidth, int atlasHeight, int x, int y) {
        super(atlasId, contents, atlasWidth, atlasHeight, x, y);
    }
}