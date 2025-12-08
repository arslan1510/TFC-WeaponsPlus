package com.concinnity.tfc_weapons_plus.item.weapon;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Shortsword item - handheld weapon with less damage than sword but faster attack speed
 * Blade and hilt must be the same metal
 * Uses ItemAttributeModifiers for explicit attack damage and speed control
 */
public class ShortswordItem extends SwordItem {
    private final String metal;
    
    public ShortswordItem(String metal, Item.Properties properties) {
        super(
            WeaponTierFactory.createTier(metal),
            properties.attributes(WeaponAttributes.createShortswordAttributes(WeaponTierFactory.createTier(metal)))
        );
        this.metal = metal;
    }
    
    public String getMetal() {
        return metal;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        // Tooltip can be customized here if needed
    }
}



