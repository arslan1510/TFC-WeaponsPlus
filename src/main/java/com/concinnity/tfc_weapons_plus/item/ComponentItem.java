package com.concinnity.tfc_weapons_plus.item;

import net.minecraft.world.item.Item;

/**
 * Unified component item class for all weapon components (grip, guard, pommel, hilt, blades, heads).
 */
public class ComponentItem extends Item {
    private final ComponentType componentType;
    private final String materialName;
    
    public ComponentItem(ComponentType componentType, String materialName, Properties properties) {
        super(properties);
        this.componentType = componentType;
        this.materialName = materialName;
    }
}

