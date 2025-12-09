package com.concinnity.tfc_weapons_plus.item.weapon;

import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Factory for creating weapon items based on weapon type.
 * Uses immutable Map for fast, extensible weapon creation.
 */
public final class WeaponItemFactory {

    @FunctionalInterface
    private interface WeaponConstructor extends BiFunction<String, Item.Properties, Item> {}

    // Immutable map provides fast lookup
    private static final Map<String, WeaponConstructor> CONSTRUCTORS = Map.ofEntries(
        // Sword-type weapons use WeaponItem with specific attribute creators
        Map.entry("longsword", (m, p) -> new WeaponItem(m, p, (t, metal) -> WeaponAttributes.createAttributes(t, metal, "longsword"))),
        Map.entry("greatsword", (m, p) -> new WeaponItem(m, p, (t, metal) -> WeaponAttributes.createAttributes(t, metal, "greatsword"))),
        Map.entry("shortsword", (m, p) -> new WeaponItem(m, p, (t, metal) -> WeaponAttributes.createAttributes(t, metal, "shortsword"))),
        Map.entry("morningstar", (m, p) -> new WeaponItem(m, p, (t, metal) -> WeaponAttributes.createAttributes(t, metal, "morningstar"))),
        Map.entry("quarterstaff", (m, p) -> new WeaponItem(m, p, (t, metal) -> WeaponAttributes.createAttributes(t, metal, "quarterstaff"))),
        // Specialized weapon classes
        Map.entry("greataxe", GreatAxeItem::new),
        Map.entry("greathammer", GreatHammerItem::new)
    );

    /**
     * Create a weapon item based on type string
     */
    public static Item createWeapon(String weaponType, String metal, Item.Properties properties) {
        WeaponConstructor constructor = CONSTRUCTORS.get(weaponType);
        if (constructor == null) {
            throw new IllegalArgumentException("Unknown weapon type: " + weaponType);
        }
        return constructor.apply(metal, properties);
    }

    private WeaponItemFactory() {}
}
