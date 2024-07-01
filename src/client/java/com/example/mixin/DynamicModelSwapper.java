package com.example.mixin;

import com.example.clientmisc.DynamicModelOverride;
import com.example.interfaces.IDynamicModelItem;
import com.example.utils.URLSprite;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.item.ItemStack;

import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;

@Mixin(ItemModels.class)
public class DynamicModelSwapper {
    Identifier itemIdentifier = Identifier.of("infinitecraft", "chatgpt");

    @Shadow
    @Final
    private BakedModelManager modelManager;

    @Inject(method= "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;",
        at=@At("HEAD"), cancellable=true)
    private void getDynamicModel(ItemStack stack, CallbackInfoReturnable<BakedModel> info){
        if(Registries.ITEM.getId(stack.getItem()).equals(itemIdentifier)) {
            URLSprite urlSprite = new URLSprite("https://cdn.sstatic.net/Img/home/illo-home-hero.png?v=4718e8f857c5", Identifier.of("infinitecraft", "item/chatgpt"));
            if (!urlSprite.getTextureId().equals(Identifier.of(""))) {
                Identifier spriteIdentifier = urlSprite.getTextureId();
                DynamicModelOverride override = new DynamicModelOverride(Identifier.of("infinitecraft", "chatgpt"), false, List.of(spriteIdentifier));
                BakedModel model = override.getBakedModel(modelManager);
                if(model != null){
                    info.setReturnValue(model);
                }
            }
        }
    }
}
