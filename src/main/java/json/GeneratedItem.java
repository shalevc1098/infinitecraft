package json;

import com.example.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.Base64;

public class GeneratedItem {
    public String url;
    public int width;
    public int height;
    public String pixelGrid;

    public GeneratedItem(String url, int width, int height, int[] pixelGrid) {
        this.url = url;
        this.width = width;
        this.height = height;
        this.pixelGrid = encodePixelGrid(pixelGrid);
    }

    private String encodePixelGrid(int[] pixelGrid) {
        byte[] byteArray = new byte[pixelGrid.length * 4];
        for (int i = 0; i < pixelGrid.length; i++) {
            int value = pixelGrid[i];
            byteArray[i * 4] = (byte) (value >> 24);
            byteArray[i * 4 + 1] = (byte) (value >> 16);
            byteArray[i * 4 + 2] = (byte) (value >> 8);
            byteArray[i * 4 + 3] = (byte) value;
        }
        return Base64.getEncoder().encodeToString(byteArray);
    }

    // Method to decode Base64 pixelGrid back to int array
    public int[] decodePixelGrid() {
        byte[] byteArray = Base64.getDecoder().decode(this.pixelGrid);
        int[] pixelGrid = new int[byteArray.length / 4];
        for (int i = 0; i < pixelGrid.length; i++) {
            pixelGrid[i] = ((byteArray[i * 4] & 0xFF) << 24)
                    | ((byteArray[i * 4 + 1] & 0xFF) << 16)
                    | ((byteArray[i * 4 + 2] & 0xFF) << 8)
                    | (byteArray[i * 4 + 3] & 0xFF);
        }
        return pixelGrid;
    }

    public static GeneratedItem imageToGeneratedItem(String url, BufferedImage image) {
        int width = 16;
        int height = 16;

        if (image.getWidth() > width || image.getHeight() > height) {
            image = ImageUtils.resizeImage(image, width, height);
        }

        int[] pixelGrid = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelGrid[y * width + x] = image.getRGB(x, y);
            }
        }

        GeneratedItem itemData = new GeneratedItem(url, image.getWidth(), image.getHeight(), pixelGrid);
        JsonHandler.saveItem(itemData);

        return itemData;
    }
}