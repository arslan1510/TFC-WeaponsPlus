package com.concinnity.tfc_weapons_plus.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Component item with metal variant support
 * Used for guard and pommel which are made of metal
 */
public class MetalComponentItem extends SwordComponentItem {
    
    public MetalComponentItem(ComponentType componentType, String metalName, Item.Properties properties) {
        super(componentType, metalName, properties);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        // No custom tooltips - just show item name and mod name
    }
}

