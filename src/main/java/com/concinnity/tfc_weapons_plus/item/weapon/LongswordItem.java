package com.concinnity.tfc_weapons_plus.item.weapon;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Longsword item - handheld weapon with stats matching TFC sword of the same metal
 * Blade and hilt must be the same metal
 * Uses ItemAttributeModifiers for explicit attack damage and speed control
 */
public class LongswordItem extends SwordItem {
    private final String metal;
    
    public LongswordItem(String metal, Item.Properties properties) {
        super(
            WeaponTierFactory.createTier(metal),
            properties.attributes(WeaponAttributes.createLongswordAttributes(WeaponTierFactory.createTier(metal)))
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

