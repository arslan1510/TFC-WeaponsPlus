package com.concinnity.tfc_weapons_plus.item.weapon;

import com.concinnity.tfc_weapons_plus.util.WeaponRegistry;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class WeaponAttributes {

    /**
     * Weapon stats record with builder for attribute modifiers.
     * Cleaner, more maintainable approach using Java 21 features.
     */
    public record WeaponStats(float damage, float speed) {
        /**
         * Convert weapon stats to ItemAttributeModifiers
         */
        public ItemAttributeModifiers toModifiers() {
            return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(SwordItem.BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                    new AttributeModifier(SwordItem.BASE_ATTACK_SPEED_ID, speed, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND)
                .build();
        }

        /**
         * Create WeaponStats from WeaponRegistry data
         */
        public static WeaponStats from(Tier tier, String metalName, String weaponType) {
            WeaponRegistry.WeaponStatsMeta stats = WeaponRegistry.getWeaponStats(weaponType);
            float densityMultiplier = WeaponRegistry.getMetalDensityMultiplier(metalName);
            float tierBonus = tier.getAttackDamageBonus();

            // Calculate final damage: baseDamage * weightMod + tierBonus
            float totalWeight = stats.baseWeight() * densityMultiplier;
            float weightDamageMod = 1.0f + (totalWeight - 1.0f) * 2.0f;
            float attackDamage = stats.baseDamage() * weightDamageMod + tierBonus;

            // Calculate final speed: baseSpeed - weightPenalty (clamped)
            float weightSpeedPenalty = (stats.baseWeight() - 1.0f) * 1.0f;
            float attackSpeed = Math.max(-3.5f, Math.min(-1.5f, stats.baseSpeed() - weightSpeedPenalty));

            return new WeaponStats(attackDamage, attackSpeed);
        }
    }

    /**
     * Create attribute modifiers for a weapon based on metal properties and weapon type
     */
    public static ItemAttributeModifiers createAttributes(Tier tier, String metalName, String weaponType) {
        return WeaponStats.from(tier, metalName, weaponType).toModifiers();
    }

    private WeaponAttributes() {}
}
