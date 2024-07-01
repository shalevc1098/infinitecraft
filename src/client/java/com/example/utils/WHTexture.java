package com.example.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

// specifies some texture and a width/height. can be either from a URL or a local texture. Mostly used for in tooltips
public class WHTexture{
    private Identifier id;
    private Identifier textureId;
    private boolean isLocal;
    // for url:
    private String url;
    // for local:
    private int width;
    private int height;

    public static WHTexture fromUrl(String url, Identifier id){
        WHTexture texture = new WHTexture();
        texture.url = url;
        texture.id = id;
        texture.isLocal = false;
        return texture;
    }

    public static WHTexture fromLocal(Identifier textureId, int width, int height){
        WHTexture texture = new WHTexture();
        texture.textureId = textureId;
        texture.id = textureId;
        texture.isLocal = true;
        texture.width = width;
        texture.height = height;
        return texture;
    }

    @Nullable
    public static WHTexture fromJson(JsonElement json){
        if(!json.isJsonObject()){
            return null;
        }
        JsonObject obj = json.getAsJsonObject();
        if(obj.has("texture")){
            Identifier textureId = Identifier.of(obj.get("texture").getAsString());
            int width = obj.get("width").getAsInt();
            int height = obj.get("height").getAsInt();
            return fromLocal(textureId, width, height);
        } else if(obj.has("url")){
            Identifier id = Identifier.of(obj.get("id").getAsString());
            return fromUrl(obj.get("url").getAsString(), id);
        }
        return null;
    }

    public JsonObject serialize(){
        JsonObject json = new JsonObject();
        if(isLocal){
            json.addProperty("texture", textureId.toString());
            json.addProperty("width", width);
            json.addProperty("height", height);
        } else {
            json.addProperty("url", url);
            json.addProperty("id", id.toString());
        }
        return json;
    }

    public static WHTexture deserialize(JsonObject json){
        if(json.has("texture")){
            Identifier textureId = Identifier.of(json.get("texture").getAsString());
            int width = json.get("width").getAsInt();
            int height = json.get("height").getAsInt();
            return fromLocal(textureId, width, height);
        } else if(json.has("url")){
            Identifier id = Identifier.of(json.get("id").getAsString());
            return fromUrl(json.get("url").getAsString(), id);
        }
        return null;
    }

    public Identifier getTextureId(){
        if(isLocal){
            return textureId;
        } else {
            return URLTextureUtils.loadTextureFromURL(url, id);
        }
    }

    public int getWidth(){
        if(isLocal){
            return width;
        } else {
            getTextureId(); // force it to load real quick
            Pair<Integer, Integer> dims = URLTextureUtils.getTextureDimensions(id);
            if(dims == null){
                return 0;
            }
            return dims.getLeft();
        }
    }

    public int getHeight(){
        if(isLocal){
            return height;
        } else {
            getTextureId(); // force it to load real quick
            Pair<Integer, Integer> dims = URLTextureUtils.getTextureDimensions(id);
            if(dims == null){
                return 0;
            }
            return dims.getRight();
        }
    }
}
