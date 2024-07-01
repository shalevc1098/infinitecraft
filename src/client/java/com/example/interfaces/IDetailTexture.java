package com.example.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import com.example.utils.Spritelike;

// used for rendering some texture as a part of the item
public interface IDetailTexture {
    
    @Environment(EnvType.CLIENT)
    public DetailTexture getDetailTexture(ItemStack stack);

    // just do all these 0-1
    public class DetailTexture{
        public Spritelike sprite;

        private float wh = 0.5f;
        private boolean lockedWidth = true; // true if wh is width, false if it's the height

        private float vertAnchor = 0f;
        private boolean topLocked = true; // vertAnchor is from the top

        private float horizAnchor = 0f;
        private boolean leftLocked = true; // horizAnchor is from the left

        private boolean overItem = true;

        public DetailTexture(Spritelike sprite){
            this.sprite = sprite;
        }

        // many silly methods for building this but would be a pain without them

        public DetailTexture fromLeft(float left){
            this.horizAnchor = left;
            this.leftLocked = true;
            return this;
        }

        public DetailTexture fromRight(float right){
            this.horizAnchor = right;
            this.leftLocked = false;
            return this;
        }

        public DetailTexture fromTop(float top){
            this.vertAnchor = top;
            this.topLocked = true;
            return this;
        }

        public DetailTexture fromBottom(float bottom){
            this.vertAnchor = bottom;
            this.topLocked = false;
            return this;
        }

        public DetailTexture withWidth(float width){
            this.wh = width;
            this.lockedWidth = true;
            return this;
        }

        public DetailTexture withHeight(float height){
            this.wh = height;
            this.lockedWidth = false;
            return this;
        }

        public DetailTexture withOverItem(boolean overItem){
            this.overItem = overItem;
            return this;
        }

        public boolean isOverItem(){
            return overItem;
        }
        
        public float getTop(){
            if(topLocked){
                return vertAnchor;
            } else {
                return 1 - vertAnchor - getHeight();
            }
        }
        
        public float getLeft(){
            if(leftLocked){
                return horizAnchor;
            } else {
                return 1 - horizAnchor - getWidth();
            }
        }

        public float getWidth(){
            if(lockedWidth){
                return wh;
            } else {
                return wh * sprite.getSpriteWidth() / sprite.getSpriteHeight();
            }
        }

        public float getHeight(){
            if(lockedWidth){
                return wh * sprite.getSpriteHeight() / sprite.getSpriteWidth();
            } else {
                return wh;
            }
        }
    }
}
