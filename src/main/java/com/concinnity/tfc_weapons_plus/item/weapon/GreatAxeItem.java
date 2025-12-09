package com.concinnity.tfc_weapons_plus.item.weapon;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public class GreatAxeItem extends AxeItem {
    private final String metal;

    public GreatAxeItem(String metal, Item.Properties properties) {
        this(metal, WeaponTiers.of(metal), properties);
    }

    public GreatAxeItem(String metal, Tier tier, Item.Properties properties) {
        super(tier, properties.attributes(WeaponAttributes.createAttributes(tier, metal, "greataxe")));
        this.metal = metal;
    }
}
