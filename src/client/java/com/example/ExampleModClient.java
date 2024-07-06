package com.example;

import json.JsonHandler;
import com.example.loaders.TutorialModelLoadingPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

public class ExampleModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		JsonHandler.loadItems();

		ModelLoadingPlugin.register(new TutorialModelLoadingPlugin());
		BuiltinItemRendererRegistry.INSTANCE.register(ExampleMod.CHATGPT_ITEM, new CustomItemRenderer());
	}
}