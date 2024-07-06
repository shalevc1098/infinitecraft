package com.example.commands;

import com.example.ExampleMod;
import com.example.utils.ImageUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import json.GeneratedItem;
import json.JsonHandler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GenerateItemCommand {
    private final String name = "generateitem";

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal(this.name).then(argument("url", StringArgumentType.greedyString()).executes(context -> {
            final String url = StringArgumentType.getString(context, "url");
            final ServerCommandSource source = context.getSource();

            if (source.getEntity() instanceof ServerPlayerEntity player) {
                GeneratedItem itemData = JsonHandler.getItemByURL(url);
                if (itemData == null) {
                    BufferedImage image = ImageUtils.downloadImage(url);

                    if (image == null) {
                        player.sendMessage(Text.of("The URL does not contain a valid image"), false);
                        return 0;
                    }

                    itemData = GeneratedItem.imageToGeneratedItem(url, image);
                }

                ItemStack generatedItem = createItemStackWithImage(itemData);

                if (!player.getInventory().insertStack(generatedItem)) {
                    player.sendMessage(Text.of("Could not add item to inventory"), false);
                    return 0;
                }
                player.sendMessage(Text.of("Item added to inventory"), false);
                return 1;
            } else {
                source.sendError(Text.of("This command can only be used by a player"));
                return 0;
            }
        })));
    }

    private ItemStack createItemStackWithImage(GeneratedItem itemData) {
        ItemStack generatedItem = new ItemStack(Registries.ITEM.get(Identifier.of("infinitecraft", "chatgpt")));
//        generatedItem.set(ExampleMod.PIXEL_GRID, itemData.pixelGrid);
        generatedItem.set(ExampleMod.URL, itemData.url);
        return generatedItem;
    }
}
