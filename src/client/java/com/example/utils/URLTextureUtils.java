package com.example.utils;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class URLTextureUtils {

    private static final Map<Identifier, Identifier> LOADED_TEXTURES = Collections.synchronizedMap(new HashMap<>());
    // same key as the loaded textures
    private static final Map<Identifier, Pair<Integer, Integer>> TEXTURE_DIMENSIONS = Collections.synchronizedMap(new HashMap<>());

    // informed by hellozyemlya on discord
    public static Identifier loadTextureFromURL(String url, Identifier textureId){
        if(LOADED_TEXTURES.containsKey(textureId)){
            return LOADED_TEXTURES.get(textureId);
        }
//        CobbleCards.logPrint("Loading texture from URL: " + url);
        MinecraftClient.getInstance().execute(() -> {
        try{
            URL textureUrl = new URL(url);
            InputStream stream = textureUrl.openStream();
//                CobbleCards.logPrint("in thread maybe ?");
                try{
                    NativeImageBackedTexture texture = new NativeImageBackedTexture(NativeImage.read(stream));
                    NativeImage baseImage = texture.getImage();
                    Identifier actualTextureId = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(textureId.toTranslationKey(), texture);
                    LOADED_TEXTURES.put(textureId, actualTextureId);
                    TEXTURE_DIMENSIONS.put(textureId, new Pair<>(baseImage.getWidth(), baseImage.getHeight()));
                } catch (Exception e){
//                    CobbleCards.LOGGER.error("Failed to load texture from URL: " + url + "\n:" + e);
                }
            } catch (Exception e){
//                CobbleCards.LOGGER.error("Failed to load texture from URL: " + url + "\n:" + e);
            }
            });
        return Identifier.of("");
    }

    public static Pair<Integer, Integer> getTextureDimensions(Identifier textureId){
        return TEXTURE_DIMENSIONS.get(textureId);
    }
}
