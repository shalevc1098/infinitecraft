package com.example;

import com.example.commands.GenerateItemCommand;
import com.example.components.PixelGridComponent;
import com.example.components.URLComponent;
import com.example.items.ChatGPT;
import com.example.items.TestItem;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("infinitecraft");

	public static final ComponentType<int[]> PIXEL_GRID = new PixelGridComponent();
	public static final ComponentType<String> URL = new URLComponent();

	public static final Item CHATGPT_ITEM = new ChatGPT();
	public static final Item TEST_ITEM = new TestItem();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of("infinitecraft", "pixel_grid"), PIXEL_GRID);
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of("infinitecraft", "url"), URL);

		Registry.register(Registries.ITEM, Identifier.of("infinitecraft", "chatgpt"), CHATGPT_ITEM);
		Registry.register(Registries.ITEM, Identifier.of("infinitecraft", "testitem"), TEST_ITEM);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			new GenerateItemCommand().register(dispatcher, registryAccess, environment);
		});
	}
}