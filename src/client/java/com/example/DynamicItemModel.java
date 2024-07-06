package com.example;

import com.example.utils.ImageUtils;
import json.GeneratedItem;
import json.JsonHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.*;
import net.minecraft.client.texture.*;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class DynamicItemModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private static final SpriteIdentifier SPRITE_ID = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.of("infinitecraft", "item/chatgpt"));
    private static Sprite sprite;

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
        sprite = textureGetter.apply(SPRITE_ID);
        return this;
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
        return sprite; // Block break particle, let's use furnace_top
    }

    private static Transformation makeTransform(float rotationX, float rotationY, float rotationZ, float translationX, float translationY, float translationZ, float scaleX, float scaleY, float scaleZ) {
        Vector3f translation = new Vector3f(translationX, translationY, translationZ);
        translation.mul(0.0625f);
        translation.set(MathHelper.clamp(translation.x, -5.0F, 5.0F), MathHelper.clamp(translation.y, -5.0F, 5.0F), MathHelper.clamp(translation.z, -5.0F, 5.0F));
        return new Transformation(new Vector3f(rotationX, rotationY, rotationZ), translation, new Vector3f(scaleX, scaleY, scaleZ));
    }

    final Transformation TRANSFORM_BLOCK_GUI = makeTransform(0, 0, 0, 0, 0, 0, 1.0f, 1.0f, 1.0f);
    final Transformation TRANSFORM_BLOCK_GROUND = makeTransform(0, 0, 0, 0, 2, 0, 0.5f, 0.5f, 0.5f);
    final Transformation TRANSFORM_BLOCK_FIXED = makeTransform(0, 180.0f, 0, 0, 0, 0, 1.0f, 1.0f, 1.0f);
    final Transformation TRANSFORM_BLOCK_3RD_PERSON = makeTransform(0, 0, 0, 0, 3.0f, 1.0f, 0.55f, 0.55f, 0.55f);
    final Transformation TRANSFORM_BLOCK_1ST_PERSON = makeTransform(0, -90.0f, 25.0f, 1.13f, 3.2f, 1.13f, 0.68f, 0.68f, 0.68f);
    final Transformation TRANSFORM_BLOCK_HEAD = makeTransform(0, 180.0f, 0, 0, 13.0f, 7.0f, 1.0f, 1.0f, 1.0f);

    final ModelTransformation MODEL_TRANSFORMATION = new ModelTransformation(TRANSFORM_BLOCK_3RD_PERSON, TRANSFORM_BLOCK_3RD_PERSON, TRANSFORM_BLOCK_1ST_PERSON, TRANSFORM_BLOCK_1ST_PERSON, TRANSFORM_BLOCK_HEAD, TRANSFORM_BLOCK_GUI, TRANSFORM_BLOCK_GROUND, TRANSFORM_BLOCK_FIXED);

    @Override
    public ModelTransformation getTransformation() {
        return MODEL_TRANSFORMATION;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false; // False to trigger FabricBakedModel rendering
    }

    private final HashMap<String, Mesh> meshCache = new HashMap<>();
    private final List<String> invalidImageURLs = new ArrayList<>();

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {
        try {
            int width = 16;
            int height = 16;
            int[] pixelGrid = new int[width * height];
            boolean shouldCache = false;
            String url = null;
            Mesh mesh = null;

            ComponentMap components = itemStack.getComponents();

            if (components.contains(ExampleMod.URL)) {
                url = itemStack.get(ExampleMod.URL);
                if (meshCache.containsKey(url)) {
                    mesh = meshCache.get(url);
                } else if (!invalidImageURLs.contains(url)) {
                    GeneratedItem itemData = JsonHandler.getItemByURL(url);
                    if (itemData != null) {
                        pixelGrid = itemData.decodePixelGrid();
                        shouldCache = true;
                    } else {
                        BufferedImage image = ImageUtils.downloadImage(url);
                        if (image == null) {
                            invalidImageURLs.add(url);
                        } else {
                            itemData = GeneratedItem.imageToGeneratedItem(url, image);
                            pixelGrid = itemData.decodePixelGrid();
                            shouldCache = true;
                        }
                    }
                }
            }

            if (!shouldCache && mesh == null) {
                String absolutePath = "C:\\Users\\Shalev\\Downloads\\a_16d669492d203918865f2b66e47850bd.png";
                BufferedImage bufferedImage = ImageIO.read(new File(absolutePath));
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        pixelGrid[y * width + x] = bufferedImage.getRGB(x, y);
                    }
                }
            }

            if (mesh == null) {
                mesh = generateMesh(pixelGrid, width, height, 1.0f / width);

                if (shouldCache) {
                    meshCache.put(url, mesh);
                }
            }

            mesh.outputTo(renderContext.getEmitter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Mesh generateMesh(int[] pixelGrid, int width, int height, float pixelSize) {
        Renderer renderer = Objects.requireNonNull(RendererAccess.INSTANCE.getRenderer());
        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int color = pixelGrid[(y * width) + x]; // to index the array: multiply y by the stride (width) and add x
                if ((color >> 24 & 255) == 0)
                    continue;

                for (Direction direction : Direction.values()) {
                    if (direction.equals(Direction.NORTH)) {
                        emitter.square(direction, 1.0f - (x + 1) * pixelSize, 1.0f - (y + 1) * pixelSize, 1.0f - x * pixelSize, 1.0f - y * pixelSize, (15 * pixelSize) / 2);
                    } else if (direction.equals(Direction.SOUTH)) {
                        emitter.square(direction, x * pixelSize, 1.0f - (y + 1) * pixelSize, (x + 1) * pixelSize, 1.0f - y * pixelSize, (1.0f - pixelSize) / 2);
                    } else if (direction.equals(Direction.EAST)) {
//                        if (x != (width - 1) && (pixelGrid[(y * width) + (x + 1)] >> 24 & 255) == 0)
//                            continue; // cull face
                        emitter.square(direction, (1.0f - pixelSize) / 2, 1.0f - (y + 1) * pixelSize, (1.0f + pixelSize) / 2, 1.0f - y * pixelSize, 1.0f - (x + 1) * pixelSize);
                    } else if (direction.equals(Direction.WEST)) {
//                        if (x != 0 && (pixelGrid[(y * width) + (x - 1)] >> 24 & 255) == 0)
//                            continue; // cull face
                        emitter.square(direction, (1.0f - pixelSize) / 2, 1.0f - (y + 1) * pixelSize, (1.0f + pixelSize) / 2, 1.0f - y * pixelSize, x * pixelSize);
                    } else if (direction.equals(Direction.UP)) {
//                        if (y != 0 && (pixelGrid[((y - 1) * width) + x] >> 24 & 255) == 0)
//                            continue; // cull face
                        emitter.square(direction, x * pixelSize, (1.0f - pixelSize) / 2, (x + 1) * pixelSize, (1.0f + pixelSize) / 2, y * pixelSize);
                    } else if (direction.equals(Direction.DOWN)) {
//                        if (y != (height - 1) && (pixelGrid[((y + 1) * width) + x] >> 24 & 255) == 0)
//                            continue; // cull face
                        emitter.square(direction, x * pixelSize, (1.0f - pixelSize) / 2, (x + 1) * pixelSize, (1.0f + pixelSize) / 2, 1.0f - (y + 1) * pixelSize);
                    }

                    emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
                    emitter.color((255 << 24) | color, (255 << 24) | color, (255 << 24) | color, (255 << 24) | color);
                    emitter.emit();
                }
            }
        }
        return builder.build();
    }
}