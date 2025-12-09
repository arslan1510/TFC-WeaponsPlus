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
         * Uses exponential drop-off for tier bonuses to prevent overpowered high-tier weapons
         */
        public static WeaponStats from(Tier tier, String metalName, String weaponType) {
            WeaponRegistry.WeaponStatsMeta stats = WeaponRegistry.getWeaponStats(weaponType);
            float tierBonus = tier.getAttackDamageBonus();

            // Exponential drop-off for tier bonus: pow(tierBonus, 0.6) provides diminishing returns
            // Low tiers (copper ~2): 2^0.6 * 1.5 = ~2.3 damage
            // Mid tiers (steel ~6): 6^0.6 * 1.5 = ~4.3 damage
            // High tiers (blue steel ~13): 13^0.6 * 1.5 = ~6.5 damage
            float scaledTierBonus = (float)(Math.pow(tierBonus, 0.6) * 1.5);

            // Calculate final damage: baseDamage + tier scaling (no metal density)
            float attackDamage = stats.baseDamage() + scaledTierBonus;

            // Use base speed directly from weapon stats
            float attackSpeed = stats.baseSpeed();

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
