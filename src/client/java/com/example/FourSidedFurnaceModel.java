package com.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.TextureHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MeshImpl;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.*;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.render.item.ItemModels;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class FourSidedFurnaceModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private static final SpriteIdentifier[] SPRITE_IDS = new SpriteIdentifier[]{
            new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.of("infinitecraft", "item/chatgpt")),
    };
    private final Sprite[] sprites = new Sprite[SPRITE_IDS.length];

    // Some constants to avoid magic numbers, these need to match the SPRITE_IDS
    private static final int SPRITE_SIDE = 0;
    private static final int SPRITE_TOP = 1;

    private Mesh mesh;

    private static ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

    private static final Identifier RED_SQUARE_ID = Identifier.of("infinitecraft", "item/chatgpt");

    private static final SpriteIdentifier test = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, RED_SQUARE_ID);
    private static Identifier dynamicTexture;

    private JsonUnbakedModel makeUnbakedModel(){
        JsonObject modelJson = new JsonObject();
        modelJson.addProperty("parent", "builtin/generated");
        JsonObject textureList = new JsonObject();
        int count = 0;
        textureList.addProperty("layer" + count, dynamicTexture.toString());
        modelJson.add("textures", textureList);
        // we're just reimplementing the item base model
        modelJson.addProperty("gui_light", "front");
        JsonElement displayObj = JsonParser.parseString("{\"ground\": {\"rotation\": [ 0, 0, 0 ],\"translation\": [ 0, 2, 0],\"scale\":[ 0.5, 0.5, 0.5 ]},\"head\": {\"rotation\": [ 0, 180, 0 ],\"translation\": [ 0, 13, 7],\"scale\":[ 1, 1, 1]},\"thirdperson_righthand\": {\"rotation\": [ 0, 0, 0 ],\"translation\": [ 0, 3, 1 ],\"scale\": [ 0.55, 0.55, 0.55 ]},\"firstperson_righthand\": {\"rotation\": [ 0, -90, 25 ],\"translation\": [ 1.13, 3.2, 1.13],\"scale\": [ 0.68, 0.68, 0.68 ]},\"fixed\": {\"rotation\": [ 0, 180, 0 ],\"scale\": [ 1, 1, 1 ]}}");
        modelJson.add("display", displayObj);
        JsonUnbakedModel model = JsonUnbakedModel.deserialize(modelJson.toString());
        model.setParents((id) -> ModelLoader.GENERATION_MARKER);
        model = ITEM_MODEL_GENERATOR.create(FourSidedFurnaceModel::spriteLoader, model);
        return model;
    }

    private static Sprite spriteLoader(SpriteIdentifier spriteId) {
        return spriteId.getSprite();
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of(); // This model does not depend on other models.
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {
        // This is related to model parents, it's not required for our use case
    }

    @Nullable
    @Override
    public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer) {
        // Get the sprites
//        for(int i = 0; i < SPRITE_IDS.length; ++i) {
////            sprites[i] = textureGetter.apply(SPRITE_IDS[i]);
//
//            NativeImage newImage = new NativeImage(NativeImage.Format.RGBA, 16, 16, false);
//            for (int y = 0; y < 16; y++) {
//                for (int x = 0; x < 16; x++) {
//                    newImage.setColor(x, y, 0xFFFFFF00);
//                }
//            }
//
//            File outputFile = new File("C:\\Users\\Shalev\\IdeaProjects\\InfiniteCraft\\generated.png");
//
//            // Save the image
//            try {
//                newImage.writeTo(outputFile);
//                System.out.println("Image saved successfully to " + outputFile.getAbsolutePath());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            SpriteContents newSpriteContents = new SpriteContents(Identifier.of("infinitecraft", "item/chatgpt"), new SpriteDimensions(16, 16), newImage, ResourceMetadata.NONE);
//            newSpriteContents.generateMipmaps(4);
//
//            sprites[i] = new CustomSprite(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, newSpriteContents, 1024, 512, 560, 112);
//        }
//
//        // Build the mesh using the Renderer API
//        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
//        MeshBuilder builder = renderer.meshBuilder();
//        QuadEmitter emitter = builder.getEmitter();
//
//        for(Direction direction : Direction.values()) {
//            // UP and DOWN share the Y axis
//            int spriteIdx = 0;
//            // Add a new face to the mesh
//            emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
//            // Set the sprite of the face, must be called after .square()
//            // We haven't specified any UV coordinates, so we want to use the whole texture. BAKE_LOCK_UV does exactly that.
//            emitter.spriteBake(sprites[spriteIdx], MutableQuadView.BAKE_LOCK_UV);
//            // Enable texture usage
//            emitter.color(-1, -1, -1, -1);
//            // Add the quad to the mesh
//            emitter.emit();
//        }
//        mesh = builder.build();
//
//        return this;

//        return baker.bake(Identifier.of("minecraft", "item/diamond"), rotationContainer);
        MinecraftClient client = MinecraftClient.getInstance();
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, 16, 16, false);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                image.setColor(x, y, 0xFFFF0000); // Red color in RGBA format
            }
        }

        NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
        client.getTextureManager().registerTexture(RED_SQUARE_ID, texture);
        dynamicTexture = RED_SQUARE_ID;

        return makeUnbakedModel().bake(baker, FourSidedFurnaceModel::spriteLoader, ModelRotation.X0_Y0);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
        // Don't need because we use FabricBakedModel instead. However, it's better to not return null in case some mod decides to call this function.
        return List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true; // we want the block to have a shadow depending on the adjacent blocks
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return sprites[SPRITE_SIDE]; // Block break particle, let's use furnace_top
    }

    @Override
    public ModelTransformation getTransformation() {
        return ModelHelper.MODEL_TRANSFORM_BLOCK;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false; // False to trigger FabricBakedModel rendering
    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {
        mesh.outputTo(renderContext.getEmitter());
    }
}