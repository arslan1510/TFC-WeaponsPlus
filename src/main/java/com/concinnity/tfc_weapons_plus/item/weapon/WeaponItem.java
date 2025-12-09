package com.concinnity.tfc_weapons_plus.item.weapon;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.function.BiFunction;

/**
 * Base weapon item class for sword-type weapons (longsword, greatsword, shortsword, morningstar, quarterstaff).
 * Uses attribute factories to configure weapon-specific stats.
 */
public class WeaponItem extends SwordItem {
    private final String metal;
    
    public WeaponItem(String metal, Item.Properties properties, BiFunction<net.minecraft.world.item.Tier, String, ItemAttributeModifiers> attributeFactory) {
        super(WeaponTiers.of(metal), properties.attributes(attributeFactory.apply(WeaponTiers.of(metal), metal)));
        this.metal = metal;
    }
}

