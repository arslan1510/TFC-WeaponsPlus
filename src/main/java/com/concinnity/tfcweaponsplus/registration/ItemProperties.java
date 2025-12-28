package com.concinnity.tfcweaponsplus.registration;

import com.concinnity.tfcweaponsplus.models.IItem;
import com.concinnity.tfcweaponsplus.models.WeaponType;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class ItemProperties {

    private ItemProperties() {}

    public static Properties buildProperties(ResourceUtils.ItemVariant variant) {
        Properties props = new Properties();

        variant.metal().ifPresent(m -> {
            Tier tier = m.toolTier();
            props.durability(tier.getUses());

            if (variant.item().getCategory() == IItem.ItemCategory.WEAPON) {
                props.component(DataComponents.ATTRIBUTE_MODIFIERS,
                        buildWeaponAttributes((WeaponType) variant.item(), tier));
            }
        });

        return props;
    }

    private static ItemAttributeModifiers buildWeaponAttributes(WeaponType weapon, Tier tier) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        createModifier("damage", weapon.getBaseDamage() + tier.getAttackDamageBonus() - 1.0),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        createModifier("speed", weapon.getBaseAttackSpeed()),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ENTITY_INTERACTION_RANGE,
                        createModifier("reach", weapon.getBaseReach()),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    private static AttributeModifier createModifier(String name, double value) {
        return new AttributeModifier(
                ResourceUtils.of(name),
                value,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}

