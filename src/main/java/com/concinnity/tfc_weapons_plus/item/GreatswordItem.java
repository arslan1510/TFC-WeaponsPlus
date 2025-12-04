package com.concinnity.tfc_weapons_plus.item;

import com.concinnity.tfc_weapons_plus.integration.MetalHelper;
import com.concinnity.tfc_weapons_plus.integration.TFCIntegration;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Greatsword item - handheld weapon with stats matching grandsword from Simply Swords
 * Blade and hilt must be the same metal
 * Uses ItemAttributeModifiers for explicit attack damage and speed control
 * Attack speed: 0.25f (very slow for balance), Attack damage: longsword + 3 (6 base + tier attack damage bonus)
 */
public class GreatswordItem extends SwordItem {
    private final String metal;
    
    public GreatswordItem(String metal, Item.Properties properties) {
        super(createTier(metal), properties.attributes(createTFCAttributes(createTier(metal))));
        this.metal = metal;
    }
    
    /**
     * Create TFC-style attributes for the greatsword
     * Attack damage: longsword + 3 (6 base + tier attack damage bonus)
     * Attack speed: 0.25f (very slow for balance - greatswords are heavy weapons)
     */
    private static ItemAttributeModifiers createTFCAttributes(Tier tier) {
        // Longsword damage is 3 + tier.getAttackDamageBonus(), so greatsword = (3 + tier) + 3 = 6 + tier
        int attackDamage = 6 + (int)tier.getAttackDamageBonus();
        // Greatsword speed is slower for balance (0.25f - very slow for heavy weapon)
        float attackSpeed = 0.25f;
        
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
            .build();
    }
    
    public String getMetal() {
        return metal;
    }
    
    /**
     * Create a tier that matches TFC sword stats for the given metal
     */
    private static Tier createTier(String metalName) {
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
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "needs_diamond_tool"));
                }
            })
            .orElseThrow(() -> new IllegalArgumentException("Unknown metal: " + metalName));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        // Tooltip can be customized here if needed
    }
}



