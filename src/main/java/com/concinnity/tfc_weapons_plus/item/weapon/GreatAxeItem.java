package com.concinnity.tfc_weapons_plus.item.weapon;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tier;

import java.util.List;

/**
 * Greataxe item - heavy axe-style weapon.
 * Uses explicit attribute modifiers for balanced damage/speed.
 */
public class GreatAxeItem extends AxeItem {
    private final String metal;
    
    public GreatAxeItem(String metal, Item.Properties properties) {
        this(metal, WeaponTierFactory.createTier(metal), properties);
    }

    public GreatAxeItem(String metal, Tier tier, Item.Properties properties) {
        super(tier, properties.attributes(WeaponAttributes.createGreatAxeAttributes(tier)));
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











