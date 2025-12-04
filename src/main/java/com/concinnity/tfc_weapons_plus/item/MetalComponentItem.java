package com.concinnity.tfc_weapons_plus.item;

import net.minecraft.world.item.Item;

/**
 * Component item with metal variant support
 * Used for guard and pommel which are made of metal
 */
public class MetalComponentItem extends SwordComponentItem {
    
    public MetalComponentItem(ComponentType componentType, String metalName, Item.Properties properties) {
        super(componentType, metalName, properties);
    }
}

