package com.example;

import com.example.enums.MySide;
import net.minecraft.client.render.model.json.ItemModelGenerator;

public class MyFrame {
    private final MySide side;
    private int min;
    private int max;
    private int level;

    public MyFrame(MySide side, int width, int depth) {
        this.side = side;
        this.min = width;
        this.max = width;
        this.level = depth;
    }

    public void expand(int newValue) {
        if (newValue < this.min) {
            this.min = newValue;
        } else if (newValue > this.max) {
            this.max = newValue;
        }

    }

    public MySide getSide() {
        return this.side;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public int getLevel() {
        return this.level;
    }
}