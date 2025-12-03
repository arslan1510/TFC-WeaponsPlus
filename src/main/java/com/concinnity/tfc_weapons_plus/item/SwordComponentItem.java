package com.concinnity.tfc_weapons_plus.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

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
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        // No custom tooltips - just show item name and mod name
    }
}

