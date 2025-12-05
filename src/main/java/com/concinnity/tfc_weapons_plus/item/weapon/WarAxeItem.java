package com.concinnity.tfc_weapons_plus.item.weapon;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Waraxe item - heavy axe-style weapon.
 * Uses explicit attribute modifiers for balanced damage/speed.
 */
public class WarAxeItem extends SwordItem {
    private final String metal;
    
    public WarAxeItem(String metal, Item.Properties properties) {
        super(
            WeaponTierFactory.createTier(metal),
            properties.attributes(WeaponAttributes.createWarAxeAttributes(WeaponTierFactory.createTier(metal)))
        );
        this.metal = metal;
    }
    
    public String getMetal() {
        return metal;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        // Custom tooltip hook
    }
}



