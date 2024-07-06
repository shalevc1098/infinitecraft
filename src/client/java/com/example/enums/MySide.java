package com.example.enums;

import net.minecraft.util.math.Direction;

public enum MySide {
    UP(Direction.UP, 0, -1),
    DOWN(Direction.DOWN, 0, 1),
    LEFT(Direction.EAST, -1, 0),
    RIGHT(Direction.WEST, 1, 0);

    private final Direction direction;
    private final int offsetX;
    private final int offsetY;

    MySide(final Direction direction, final int offsetX, final int offsetY) {
        this.direction = direction;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public int getOffsetX() {
        return this.offsetX;
    }

    public int getOffsetY() {
        return this.offsetY;
    }

    public boolean isVertical() {
        return this == DOWN || this == UP;
    }
}