package com.concinnity.tfc_weapons_plus.item.weapon;

import com.concinnity.tfc_weapons_plus.integration.MetalHelper;
import com.concinnity.tfc_weapons_plus.integration.TFCIntegration;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * Factory for creating weapon tiers that match TFC sword stats
 */
public final class WeaponTierFactory {
    
    /**
     * Create a tier that matches TFC sword stats for the given metal
     * 
     * @param metalName the metal name
     * @return a Tier instance matching TFC sword properties
     * @throws IllegalArgumentException if the metal is unknown
     */
    public static Tier createTier(String metalName) {
        return MetalHelper.getMetalProperties(metalName)
            .map(props -> new Tier() {
                @Override
                public int getUses() {
                    return props.durability();
                }
                
                @Override
                public float getSpeed() {
                    return props.efficiency();
                }
                
                @Override
                public float getAttackDamageBonus() {
                    return props.attackDamage();
                }
                
                @Override
                public int getEnchantmentValue() {
                    return props.tier() * 10; // Standard enchantability calculation
                }
                
                @Override
                public Ingredient getRepairIngredient() {
                    // Use TFC ingot of the same metal for repair
                    return TFCIntegration.getTFCMetalIngotIngredient(metalName)
                        .orElse(Ingredient.EMPTY);
                }
                
                @Override
                public TagKey<Block> getIncorrectBlocksForDrops() {
                    // Return empty tag - swords don't mine blocks, so this shouldn't matter
                    // But we need to return a valid TagKey
                    return TagKey.create(Registries.BLOCK, 
                        ResourceLocation.fromNamespaceAndPath("minecraft", "needs_diamond_tool"));
                }
            })
            .orElseThrow(() -> new IllegalArgumentException("Unknown metal: " + metalName));
    }
    
    private WeaponTierFactory() {
        // Utility class
    }
}

