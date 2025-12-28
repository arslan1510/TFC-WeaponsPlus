package com.concinnity.tfcweaponsplus.item.client;

import com.concinnity.tfcweaponsplus.item.custom.AnimatedItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ItemRenderer extends GeoItemRenderer<AnimatedItem> {
    public ItemRenderer(){
        super(new ItemModel());
    }
}
