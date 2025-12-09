package com.concinnity.tfc_weapons_plus.util;

import com.concinnity.tfc_weapons_plus.item.ModItems;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Central registry for all modular weapon and component definitions.
 * Provides a single source of truth for sizes, weights, heat units, and lookups.
 * Uses lazy initialization and EnumMap for optimal performance.
 */
public final class WeaponRegistry {

    public record ItemDef(
        String type,                       // logical type id, e.g., "longsword"
        String pathPrefix,                 // resource path prefix, e.g., "metal/longsword"
        Function<String, Item> itemGetter, // Direct getter (no Optional)
        Size size,
        Weight weight,
        int heatUnits,
        boolean metalItem
    ) {
        public String path(String metalName) {
            return pathPrefix + "/" + NameUtils.normalizeMetalName(metalName);
        }

        public Item item(String metalName) {
            return itemGetter.apply(metalName);
        }
    }

    private static final Map<String, Function<String, Item>> ITEM_GETTERS = Map.ofEntries(
        Map.entry("guard", ModItems::getGuardForMetal),
        Map.entry("pommel", ModItems::getPommelForMetal),
        Map.entry("hilt", ModItems::getHiltForMetal),
        Map.entry("longsword_blade", ModItems::getLongswordBladeForMetal),
        Map.entry("greatsword_blade", ModItems::getGreatswordBladeForMetal),
        Map.entry("shortsword_blade", ModItems::getShortswordBladeForMetal),
        Map.entry("greataxe_head", ModItems::getGreatAxeHeadForMetal),
        Map.entry("greathammer_head", ModItems::getGreatHammerHeadForMetal),
        Map.entry("morningstar_head", ModItems::getMorningstarHeadForMetal),
        Map.entry("longsword", ModItems::getLongswordForMetal),
        Map.entry("greatsword", ModItems::getGreatswordForMetal),
        Map.entry("shortsword", ModItems::getShortswordForMetal),
        Map.entry("greataxe", ModItems::getGreatAxeForMetal),
        Map.entry("greathammer", ModItems::getGreatHammerForMetal),
        Map.entry("morningstar", ModItems::getMorningstarForMetal),
        Map.entry("quarterstaff", ModItems::getQuarterstaffForMetal)
    );

    /**
     * Metadata for items and weapons. Components have null combat stats.
     */
    private record Meta(
        Size size,
        Weight weight,
        int heatUnits,
        Float baseWeight,    // Combat stat (null for components)
        Float baseDamage,    // Combat stat (null for components)
        Float baseSpeed      // Combat stat (null for components)
    ) {
        // Convenience constructor for components (no combat stats)
        Meta(Size size, Weight weight, int heatUnits) {
            this(size, weight, heatUnits, null, null, null);
        }
    }

    private static final Map<String, Meta> METADATA = Map.ofEntries(
        // Small components - fits in small vessels (50 mb = 0.5 ingots each)
        Map.entry("guard", new Meta(Size.SMALL, Weight.LIGHT, 50)),
        Map.entry("pommel", new Meta(Size.SMALL, Weight.LIGHT, 50)),
        Map.entry("hilt", new Meta(Size.SMALL, Weight.LIGHT, 50)),
        // Blades/heads - intermediate crafting items (metal cost before assembly)
        Map.entry("shortsword_blade", new Meta(Size.LARGE, Weight.MEDIUM, 100)),      // 1 ingot
        Map.entry("longsword_blade", new Meta(Size.LARGE, Weight.HEAVY, 150)),        // 1.5 ingots
        Map.entry("greatsword_blade", new Meta(Size.VERY_LARGE, Weight.HEAVY, 250)),  // 2.5 ingots
        Map.entry("greataxe_head", new Meta(Size.LARGE, Weight.HEAVY, 150)),          // 1.5 ingots
        Map.entry("greathammer_head", new Meta(Size.VERY_LARGE, Weight.VERY_HEAVY, 350)), // 3.5 ingots
        Map.entry("morningstar_head", new Meta(Size.NORMAL, Weight.HEAVY, 100)),      // 1 ingot
        // Complete weapons - sized relative to TFC swords (VERY_LARGE + VERY_HEAVY)
        // Melting values match TFC pattern: sword = 200mb (2 ingots)
        // Includes combat stats: baseWeight, baseDamage, baseSpeed
        Map.entry("shortsword", new Meta(Size.LARGE, Weight.HEAVY, 150, 0.8f, 2.5f, -2.0f)),             // 1.5 ingots
        Map.entry("longsword", new Meta(Size.VERY_LARGE, Weight.VERY_HEAVY, 200, 1.0f, 3.0f, -2.4f)),    // 2 ingots (same as TFC sword)
        Map.entry("greatsword", new Meta(Size.HUGE, Weight.VERY_HEAVY, 300, 1.5f, 5.0f, -2.8f)),         // 3 ingots
        Map.entry("greataxe", new Meta(Size.VERY_LARGE, Weight.VERY_HEAVY, 200, 1.6f, 5.5f, -2.9f)),     // 2 ingots
        Map.entry("greathammer", new Meta(Size.HUGE, Weight.VERY_HEAVY, 400, 1.8f, 6.0f, -3.1f)),        // 4 ingots
        Map.entry("morningstar", new Meta(Size.LARGE, Weight.HEAVY, 150, 1.7f, 5.8f, -3.0f)),            // 1.5 ingots
        Map.entry("quarterstaff", new Meta(Size.VERY_LARGE, Weight.HEAVY, 100, 0.9f, 3.0f, -2.3f))       // 1 ingot (mostly wood)
    );

    /**
     * Weapon stats metadata
     */
    public record WeaponStatsMeta(float baseWeight, float baseDamage, float baseSpeed) {}

    /**
     * Get weapon stats metadata for a weapon type
     */
    public static WeaponStatsMeta getWeaponStats(String weaponType) {
        Meta meta = METADATA.get(weaponType);
        if (meta == null || meta.baseWeight == null) {
            throw new IllegalArgumentException("No weapon stats found for type: " + weaponType);
        }
        return new WeaponStatsMeta(meta.baseWeight, meta.baseDamage, meta.baseSpeed);
    }

    // Lazy-initialized item definitions list
    private static volatile List<ItemDef> cachedItemDefs;

    private static List<ItemDef> buildItemDefs() {
        return Stream.concat(
            Stream.of(new ItemDef("grip", "wood/grip", metalName -> ModItems.GRIP.get(), Size.SMALL, Weight.LIGHT, 0, false)),
            ModItems.definitions().stream()
                .map(def -> {
                    Meta meta = METADATA.get(def.type());
                    Function<String, Item> getter = ITEM_GETTERS.get(def.type());
                    if (meta == null || getter == null) {
                        return null;
                    }
                    return new ItemDef(def.type(), def.pathPrefix(), getter, meta.size(), meta.weight(), meta.heatUnits(), true);
                })
                .filter(Objects::nonNull)
        ).toList();
    }

    // Tag groupings
    public static final Set<String> SWORDS = Set.of("longsword", "greatsword", "shortsword");
    public static final Set<String> AXES = Set.of("greataxe");
    public static final Set<String> SLASHING = Set.of("longsword", "greatsword", "shortsword", "greataxe");
    public static final Set<String> CRUSHING = Set.of("greathammer", "morningstar", "quarterstaff");
    public static final Set<String> PIERCING = Set.of("shortsword");

    private static final Set<String> WEAPON_TYPES = Set.of(
        "longsword", "greatsword", "shortsword", "greataxe", "greathammer", "morningstar", "quarterstaff"
    );

    /**
     * Get all item definitions (lazy-initialized with double-checked locking)
     */
    public static Stream<ItemDef> all() {
        if (cachedItemDefs == null) {
            synchronized (WeaponRegistry.class) {
                if (cachedItemDefs == null) {
                    cachedItemDefs = buildItemDefs();
                }
            }
        }
        return cachedItemDefs.stream();
    }

    public static Stream<ItemDef> weapons() {
        return all().filter(def -> WEAPON_TYPES.contains(def.type()));
    }

    public static Stream<ItemDef> metalItems() {
        return all().filter(ItemDef::metalItem);
    }

    private WeaponRegistry() {}
}
