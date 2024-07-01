package com.example;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;

@Mixin(JsonUnbakedModel.class)
public interface JsonUnbakedModelAccessors {
    @Accessor("parentId")
    Identifier getParentId();

    @Accessor("parent")
    JsonUnbakedModel getParent();
}