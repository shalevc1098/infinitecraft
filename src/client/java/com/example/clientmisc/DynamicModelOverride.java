package com.example.clientmisc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader.Synchronizer;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class DynamicModelOverride {
    private boolean fromJson;
    private Identifier id; // id to use just in general
    private List<Identifier> textures;
    public static ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private DynamicModelOverride fallback = null;

    public DynamicModelOverride(Identifier id, boolean fromJson, List<Identifier> textures){
        this.fromJson = fromJson;
        this.id = id;
        this.textures = textures;
    }

    public static DynamicModelOverride fromJsonModel(Identifier simpleId){
        return new DynamicModelOverride(simpleId, true, new ArrayList<>());
    }

    // modelId just to keep track of it
    public static DynamicModelOverride fromTextures(List<Identifier> textureIds, Identifier modelId){
        return new DynamicModelOverride(modelId, false, textureIds);
    }

    public DynamicModelOverride withFallback(DynamicModelOverride fallback){
        this.fallback = fallback;
        return this;
    }

    private static Map<Identifier, BakedModel> bakedModelsFromTextures = new HashMap<>();

    @Nullable
    public BakedModel getBakedModel(BakedModelManager modelManager){
        return getBakedModel(modelManager, 0);
    }

    @Nullable
    private BakedModel getBakedModel(BakedModelManager modelManager, int nests){
        if(nests > 10){
            return null; // to prevent infinite loops
        }
        if(!resourcesExist()){
            if(fallback != null){
                return fallback.getBakedModel(modelManager, nests + 1);
            }
            return null;
        }
        if(fromJson){
            BakedModel model = modelManager.getModel(new ModelIdentifier(id, "inventory"));
            if(model != modelManager.getMissingModel()){
                return model;
            }
        } else {
            if(bakedModelsFromTextures.containsKey(id)) return bakedModelsFromTextures.get(id);
            JsonUnbakedModel jsonModel = makeUnbakedModel();
//            CobbleCards.logPrint("successfully parsed jsonModel? (" + jsonModel.id + "): " + jsonModel.toString());
//            CobbleCards.logPrint("root model: " + jsonModel.getRootModel());
            BakedModel bakedModel = jsonModel.bake(null, DynamicModelOverride::spriteLoader, ModelRotation.X0_Y0);
//            CobbleCards.logPrint("baked the model: " + bakedModel.toString());
            bakedModelsFromTextures.put(id, bakedModel);
            return bakedModel;
        }
        return null;
    }

    private JsonUnbakedModel makeUnbakedModel(){
        JsonObject modelJson = new JsonObject();
        modelJson.addProperty("parent", "builtin/generated");
        JsonObject textureList = new JsonObject();
        int count = 0;
        for(Identifier texture : textures){
            textureList.addProperty("layer" + count, texture.toString());
            count++;
        }
        modelJson.add("textures", textureList);
        // we're just reimplementing the item base model
        modelJson.addProperty("gui_light", "front");
        JsonElement displayObj = JsonParser.parseString("{\"ground\": {\"rotation\": [ 0, 0, 0 ],\"translation\": [ 0, 2, 0],\"scale\":[ 0.5, 0.5, 0.5 ]},\"head\": {\"rotation\": [ 0, 180, 0 ],\"translation\": [ 0, 13, 7],\"scale\":[ 1, 1, 1]},\"thirdperson_righthand\": {\"rotation\": [ 0, 0, 0 ],\"translation\": [ 0, 3, 1 ],\"scale\": [ 0.55, 0.55, 0.55 ]},\"firstperson_righthand\": {\"rotation\": [ 0, -90, 25 ],\"translation\": [ 1.13, 3.2, 1.13],\"scale\": [ 0.68, 0.68, 0.68 ]},\"fixed\": {\"rotation\": [ 0, 180, 0 ],\"scale\": [ 1, 1, 1 ]}}");
        modelJson.add("display", displayObj);
//        CobbleCards.logPrint("made json: " + modelJson.toString());
        JsonUnbakedModel model = JsonUnbakedModel.deserialize(modelJson.toString());
//        JsonUnbakedModelAccessors accessors = (JsonUnbakedModelAccessors) model;
//        CobbleCards.logPrint("model has parentId: " + accessors.getParentId());
//        if(accessors.getParent() != null){
//            CobbleCards.logPrint("model has parent: " + accessors.getParent().toString());
//        }
        model.setParents((id) -> {
//            CobbleCards.logPrint("requesting parent: " + id);
            return ModelLoader.GENERATION_MARKER;
        });
        model = ITEM_MODEL_GENERATOR.create(DynamicModelOverride::spriteLoader, model);
//        CobbleCards.logPrint("model after has parentId: " + accessors.getParentId());
//        if(accessors.getParent() != null){
//            CobbleCards.logPrint("model after has parent: " + accessors.getParent().toString());
//        }
        return model;
    }

    public boolean resourcesExist(){
        if(fromJson){
            return hasModel(id);
        } else {
            for(Identifier textureId : textures){
                if(!hasTexture(textureId)){
                    return false;
                }
            }
            return true;
        }
    }

    private static Sprite spriteLoader(SpriteIdentifier spriteId) {
        // CobbleCards.logPrint("requesting sprite: " + spriteId.toString());
        Sprite sprite = spriteId.getSprite();
        // CobbleCards.logPrint("found: " + sprite.toString());
        return sprite;
    }

    public static boolean hasTexture(Identifier textureId){
        boolean isPresent = false;
        if (!textureId.getPath().isEmpty()) {
            Identifier resourceId = Identifier.of(textureId.getNamespace(), "textures/" + textureId.getPath() + ".png");
            Optional<AbstractTexture> maybeTexture = Optional.ofNullable(MinecraftClient.getInstance().getTextureManager().getTexture(resourceId));
            isPresent = maybeTexture.isPresent();
        }

        return isPresent;
    }

    public static boolean hasModel(Identifier modelId){
        Identifier resourceId = Identifier.of(modelId.getNamespace(), "models/" + modelId.getPath() + ".json");
        Optional<Resource> maybeModel = MinecraftClient.getInstance().getResourceManager().getResource(resourceId);
        return maybeModel.isPresent();
    }

    // should be called on reload
    public static void clearCache(){
        bakedModelsFromTextures.clear();
    }

    private static CompletableFuture<Void> reload(Synchronizer var1, ResourceManager var2, Profiler var3, Profiler var4, Executor var5, Executor var6){
        clearCache();
        return CompletableFuture.completedFuture(null);
    }

//    public static void init(){
//        ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, DynamicModelOverride::reload);
//    }
}