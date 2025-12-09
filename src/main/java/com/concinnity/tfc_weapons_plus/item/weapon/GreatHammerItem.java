package com.concinnity.tfc_weapons_plus.item.weapon;

import net.dries007.tfc.common.items.HammerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public class GreatHammerItem extends HammerItem {
    private final String metal;

    public GreatHammerItem(String metal, Item.Properties properties) {
        this(metal, WeaponTiers.of(metal), properties);
    }

    public GreatHammerItem(String metal, Tier tier, Item.Properties properties) {
        super(tier, properties.attributes(WeaponAttributes.createAttributes(tier, metal, "greathammer")));
        this.metal = metal;
    }
}
