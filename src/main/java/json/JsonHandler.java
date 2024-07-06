package json;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonHandler {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final HashMap<String, GeneratedItem> items = new HashMap<>();

    private static Path getItemsJsonPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("infinitecraft/items.json");
    }

    private static void ensureParentDirectoryExists(Path filePath) throws IOException {
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
    }

    public static GeneratedItem getItemByURL(String url) {
        return items.get(url);
    }

    public static void loadItems() {
        Path filePath = getItemsJsonPath();
        try {
            if (!Files.exists(filePath)) {
                ensureParentDirectoryExists(filePath);
                Files.createFile(filePath);
                try (FileWriter writer = new FileWriter(filePath.toFile())) {
                    gson.toJson(new ArrayList<GeneratedItem>(), writer);
                }
            }
            try (FileReader reader = new FileReader(filePath.toFile())) {
                Type listType = new TypeToken<ArrayList<GeneratedItem>>() {}.getType();
                List<GeneratedItem> itemList;
                try {
                    itemList = gson.fromJson(reader, listType);
                    if (itemList == null) {
                        itemList = new ArrayList<>();
                    }
                } catch (JsonSyntaxException e) {
                    itemList = new ArrayList<>();
                    try (FileWriter writer = new FileWriter(filePath.toFile())) {
                        gson.toJson(itemList, writer);
                    }
                }
                for (GeneratedItem item : itemList) {
                    items.put(item.url, item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveItem(GeneratedItem generatedItem) {
        // Add the item to the hashmap
        items.put(generatedItem.url, generatedItem);

        Path filePath = getItemsJsonPath();
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            List<GeneratedItem> itemList = new ArrayList<>(items.values());
            gson.toJson(itemList, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
