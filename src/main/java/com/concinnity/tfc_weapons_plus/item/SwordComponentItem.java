package com.concinnity.tfc_weapons_plus.item;

import net.minecraft.world.item.Item;

/**
 * Base class for all sword components
 * Uses modern Java features for cleaner code
 */
public class SwordComponentItem extends Item {
    private final ComponentType componentType;
    private final String materialName;
    
    public SwordComponentItem(ComponentType componentType, String materialName, Properties properties) {
        super(properties);
        this.componentType = componentType;
        this.materialName = materialName;
    }
    
    public ComponentType getComponentType() {
        return componentType;
    }
    
    public String getMaterialName() {
        return materialName;
    }
}

