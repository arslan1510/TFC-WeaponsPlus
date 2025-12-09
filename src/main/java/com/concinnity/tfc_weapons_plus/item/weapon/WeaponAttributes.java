package com.concinnity.tfc_weapons_plus.item.weapon;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Utility class for creating weapon attribute modifiers
 */
public final class WeaponAttributes {
    
    /**
     * Create TFC-style attributes for a longsword
     * Attack damage: base sword (3) + tier attack damage bonus
     * Attack speed: standard sword speed (-2.4f)
     * 
     * @param tier the weapon tier
     * @return ItemAttributeModifiers for a longsword
     */
    public static ItemAttributeModifiers createLongswordAttributes(Tier tier) {
        // Base sword damage is 3, so modifier = 3 + tier.getAttackDamageBonus()
        int attackDamage = 3 + (int)tier.getAttackDamageBonus();
        // Standard sword speed is -2.4f
        float attackSpeed = -2.4f;
        
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(SwordItem.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(SwordItem.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
            .build();
    }
    
    /**
     * Create TFC-style attributes for a greatsword
     * Attack damage: longsword + 3 (6 base + tier attack damage bonus)
     * Attack speed: slower than longsword but still usable (greatswords are heavy weapons)
     * 
     * @param tier the weapon tier
     * @return ItemAttributeModifiers for a greatsword
     */
    public static ItemAttributeModifiers createGreatswordAttributes(Tier tier) {
        // Longsword damage is 3 + tier.getAttackDamageBonus(), so greatsword = (3 + tier) + 3 = 6 + tier
        int attackDamage = 6 + (int)tier.getAttackDamageBonus();
        // Greatsword speed is slower for balance (-3.0f gives effective speed of 1.0, slow but usable)
        // Must be > -4.0 to avoid being clamped to 0
        float attackSpeed = -3.0f;
        
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(SwordItem.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(SwordItem.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
            .build();
    }
    
    private WeaponAttributes() {
        // Utility class
    }
    
    /**
     * Create attributes for a greataxe.
     * Attack damage: heavy cleaver (6 + tier bonus, matching greatsword damage)
     * Attack speed: slightly faster than before to feel like a sharpened axe.
     */
    public static ItemAttributeModifiers createGreatAxeAttributes(Tier tier) {
        int attackDamage = 6 + (int) tier.getAttackDamageBonus();
        float attackSpeed = -3.0f;
        
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(SwordItem.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                new AttributeModifier(SwordItem.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .build();
    }
    
    /**
     * Create attributes for a greathammer.
     * Attack damage: heavier than greataxe (7 + tier bonus)
     * Attack speed: heavy swing but still usable.
     */
    public static ItemAttributeModifiers createGreatHammerAttributes(Tier tier) {
        int attackDamage = 7 + (int) tier.getAttackDamageBonus();
        float attackSpeed = -3.25f;
        
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(SwordItem.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                new AttributeModifier(SwordItem.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .build();
    }
    
    /**
     * Create attributes for a shortsword.
     * Attack damage: less than sword (2 + tier bonus, vs sword's 3 + tier)
     * Attack speed: faster than sword (-2.0f vs sword's -2.4f)
     */
    public static ItemAttributeModifiers createShortswordAttributes(Tier tier) {
        int attackDamage = 2 + (int) tier.getAttackDamageBonus();
        float attackSpeed = -2.0f;
        
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(SwordItem.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                new AttributeModifier(SwordItem.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .build();
    }
    
    /**
     * Create attributes for a morningstar.
     * Attack damage: axe-like (6 + tier bonus)
     * Attack speed: axe-like (-3.2f)
     */
    public static ItemAttributeModifiers createMorningstarAttributes(Tier tier) {
        int attackDamage = 6 + (int) tier.getAttackDamageBonus();
        float attackSpeed = -3.2f;
        
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(SwordItem.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                new AttributeModifier(SwordItem.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .build();
    }
    
    /**
     * Create attributes for a quarterstaff (bashing/crushing).
     * Uses sword-like damage/speed while dealing crushing damage via tags.
     */
    public static ItemAttributeModifiers createQuarterstaffAttributes(Tier tier) {
        // Match sword profile: 3 base + tier bonus, speed -2.4
        int attackDamage = 3 + (int) tier.getAttackDamageBonus();
        float attackSpeed = -2.4f;
        
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(SwordItem.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                new AttributeModifier(SwordItem.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .build();
    }
}

