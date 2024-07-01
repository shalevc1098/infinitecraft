package com.example.loaders;

import com.example.FourSidedFurnaceModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TutorialModelLoadingPlugin implements ModelLoadingPlugin {
    public static final ModelIdentifier FOUR_SIDED_FURNACE_MODEL = new ModelIdentifier(Identifier.of("infinitecraft", "chatgpt"), "inventory");

    @Override
    public void onInitializeModelLoader(Context pluginContext) {
//        // We want to add our model when the models are loaded
//        pluginContext.modifyModelOnLoad().register((original, context) -> {
//            // This is called for every model that is loaded, so make sure we only target ours
//            ModelIdentifier modelIdentifier = context.topLevelId();
//            if(modelIdentifier != null && modelIdentifier.equals(FOUR_SIDED_FURNACE_MODEL)) {
//                return new FourSidedFurnaceModel();
//            } else {
//                // If we don't modify the model we just return the original as-is
//                return original;
//            }
//        });
    }
}