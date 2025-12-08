package com.concinnity.tfc_weapons_plus.item.weapon;

import net.dries007.tfc.common.items.HammerItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Greathammer item - heavy hammer-style weapon.
 * Uses explicit attribute modifiers for balanced damage/speed.
 */
public class GreatHammerItem extends HammerItem {
    private final String metal;
    
    public GreatHammerItem(String metal, Item.Properties properties) {
        this(metal, WeaponTierFactory.createTier(metal), properties);
    }

    public GreatHammerItem(String metal, Tier tier, Item.Properties properties) {
        super(tier, properties.attributes(WeaponAttributes.createGreatHammerAttributes(tier)));
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



