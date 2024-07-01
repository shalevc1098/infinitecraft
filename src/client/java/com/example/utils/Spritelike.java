package com.example.utils;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

public abstract class Spritelike {

    public abstract SpritelikeType getType();

    public abstract Identifier getTextureId();

    public abstract float getMinU();
    public abstract float getMinV();
    public abstract float getMaxU();
    public abstract float getMaxV();

    // these are mostly just here for the w:h ratio
    public abstract int getTextureWidth();
    public abstract int getTextureHeight();

    public int getSpriteWidth(){
        return (int) ((getMaxU()-getMinU()) * getTextureWidth());
    }

    public int getSpriteHeight(){
        return (int) ((getMaxV()-getMinV()) * getTextureHeight());
    }

    @Environment(EnvType.CLIENT)
    public void drawSpriteWithLight(DrawContext ctx, float x, float y, float z, float width, float height, int light, int argb){
        Identifier texture = this.getTextureId();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapProgram);
        Matrix4f matrix4f = ctx.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_LIGHT_COLOR);
        bufferBuilder.vertex(matrix4f, x, y, z).texture(getMinU(), getMinV()).light(light).color(argb);
        bufferBuilder.vertex(matrix4f, x, y+height, z).texture(getMinU(), getMaxV()).light(light).color(argb);
        bufferBuilder.vertex(matrix4f, x+width, y+height, z).texture(getMaxU(), getMaxV()).light(light).color(argb);
        bufferBuilder.vertex(matrix4f, x+width, y, z).texture(getMaxU(), getMinV()).light(light).color(argb);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    @Environment(EnvType.CLIENT)
    public void drawSprite(DrawContext ctx, float x, float y, float z, float width, float height){
        Identifier texture = this.getTextureId();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        Matrix4f matrix4f = ctx.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f, x, y, z).texture(getMinU(), getMinV());
        bufferBuilder.vertex(matrix4f, x, y+height, z).texture(getMinU(), getMaxV());
        bufferBuilder.vertex(matrix4f, x+width, y+height, z).texture(getMaxU(), getMaxV());
        bufferBuilder.vertex(matrix4f, x+width, y, z).texture(getMaxU(), getMinV());
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static Spritelike fromJson(JsonElement json){
        return Spritelike.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> {
                    System.err.println("Error parsing Spritelike from JSON: " + error);
                })
                .orElse(null);
    }

    public static Spritelike fromNbt(NbtElement nbt){
        return Spritelike.CODEC.parse(NbtOps.INSTANCE, nbt)
                .resultOrPartial(error -> {
                    System.err.println("Error parsing Spritelike from NBT: " + error);
                })
                .orElse(null);
    }

    private static final Map<String, SpritelikeType> TYPES = new HashMap<>();

    static {
        registerType(URLSprite.UrlSpriteType.INSTANCE);
    }

    public static void registerType(SpritelikeType type){
        TYPES.put(type.getId(), type);
    }

    private static final Codec<SpritelikeType> TYPE_CODEC = Codec.STRING.comapFlatMap(id -> {
        SpritelikeType type = TYPES.get(id);
        if (type == null) {
            return DataResult.error(() -> "Unknown spritelike type: " + id);
        }
        return DataResult.success(type);
    }, SpritelikeType::getId);

    public static final Codec<Spritelike> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TYPE_CODEC.fieldOf("type").forGetter(Spritelike::getType)
            ).apply(instance, Spritelike::createInstance)
    );

    protected static Spritelike createInstance(SpritelikeType type) {
        return type.getCodec().parse(JsonOps.INSTANCE, JsonOps.INSTANCE.empty())
                .resultOrPartial(error -> {
                    System.err.println("Error creating Spritelike instance: " + error);
                })
                .orElse(null);
    }

    public interface SpritelikeType {
        Codec<? extends Spritelike> getCodec();
        String getId();

        static SpritelikeType of(String id, Codec<Spritelike> codec) {
            return new Simple(id, codec);
        }

        class Simple implements SpritelikeType {
            private final String id;
            private final Codec<Spritelike> codec;

            public Simple(String id, Codec<Spritelike> codec) {
                this.id = id;
                this.codec = codec;
            }

            @Override
            public Codec<Spritelike> getCodec() {
                return codec;
            }

            @Override
            public String getId() {
                return id;
            }
        }
    }
}