package com.example.interfaces;

import com.example.clientmisc.DynamicModelOverride;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;

public interface IDynamicModelItem {

    @Environment(EnvType.CLIENT)
    public DynamicModelOverride getModelIdentifier(ItemStack stack);
}
