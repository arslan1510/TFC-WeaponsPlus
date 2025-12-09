package com.concinnity.tfc_weapons_plus.item.weapon;

import com.concinnity.tfc_weapons_plus.util.NameUtils;
import net.dries007.tfc.common.LevelTier;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Builds tool tiers for weapon items directly from TFC metal data.
 */
public final class WeaponTiers {

    public static Tier of(String metalName) {
        Metal metal = Metal.valueOf(metalName.toUpperCase());
        LevelTier tier = metal.toolTier();
        String normalizedMetal = NameUtils.normalizeMetalName(metalName);
        TagKey<Item> repairTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/" + normalizedMetal));

        return new Tier() {
            @Override
            public int getUses() {
                return tier.getUses();
            }

            @Override
            public float getSpeed() {
                return tier.getSpeed();
            }

            @Override
            public float getAttackDamageBonus() {
                return tier.getAttackDamageBonus();
            }

            @Override
            public int getEnchantmentValue() {
                return tier.getEnchantmentValue();
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(repairTag);
            }

            @Override
            public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
                return tier.getIncorrectBlocksForDrops();
            }
        };
    }

    private WeaponTiers() {}
}

