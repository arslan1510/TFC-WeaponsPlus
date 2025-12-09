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

    private record Meta(Size size, Weight weight, int heatUnits) {}

    private static final Map<String, Meta> METADATA = Map.ofEntries(
        Map.entry("guard", new Meta(Size.SMALL, Weight.LIGHT, 3)),
        Map.entry("pommel", new Meta(Size.SMALL, Weight.LIGHT, 3)),
        Map.entry("hilt", new Meta(Size.SMALL, Weight.LIGHT, 3)),
        Map.entry("longsword_blade", new Meta(Size.NORMAL, Weight.MEDIUM, 5)),
        Map.entry("greatsword_blade", new Meta(Size.NORMAL, Weight.MEDIUM, 10)),
        Map.entry("shortsword_blade", new Meta(Size.NORMAL, Weight.MEDIUM, 3)),
        Map.entry("greataxe_head", new Meta(Size.NORMAL, Weight.HEAVY, 5)),
        Map.entry("greathammer_head", new Meta(Size.NORMAL, Weight.HEAVY, 10)),
        Map.entry("morningstar_head", new Meta(Size.NORMAL, Weight.HEAVY, 3)),
        Map.entry("longsword", new Meta(Size.LARGE, Weight.HEAVY, 10)),
        Map.entry("greatsword", new Meta(Size.LARGE, Weight.HEAVY, 15)),
        Map.entry("shortsword", new Meta(Size.NORMAL, Weight.MEDIUM, 7)),
        Map.entry("greataxe", new Meta(Size.LARGE, Weight.VERY_HEAVY, 5)),
        Map.entry("greathammer", new Meta(Size.LARGE, Weight.VERY_HEAVY, 10)),
        Map.entry("morningstar", new Meta(Size.NORMAL, Weight.HEAVY, 3)),
        Map.entry("quarterstaff", new Meta(Size.NORMAL, Weight.HEAVY, 5))
    );

    /**
     * Weapon stats metadata stored in EnumMap for fast lookups
     */
    public record WeaponStatsMeta(float baseWeight, float baseDamage, float baseSpeed) {}

    private static final EnumMap<WeaponType, WeaponStatsMeta> WEAPON_STATS_BY_ENUM = new EnumMap<>(WeaponType.class);

    static {
        // Initialize EnumMap with weapon stats from WeaponType enum
        for (WeaponType type : WeaponType.values()) {
            WEAPON_STATS_BY_ENUM.put(type, new WeaponStatsMeta(
                type.getBaseWeight(),
                type.getBaseDamage(),
                type.getBaseSpeed()
            ));
        }
    }

    /**
     * Metal density multipliers based on TFC metal properties
     */
    public static float getMetalDensityMultiplier(String metalName) {
        return switch (metalName.toLowerCase()) {
            case "copper" -> 0.9f;
            case "bismuth_bronze" -> 0.95f;
            case "bronze" -> 1.0f;
            case "black_bronze" -> 1.05f;
            case "wrought_iron" -> 1.1f;
            case "steel" -> 1.2f;
            case "black_steel" -> 1.3f;
            case "blue_steel", "red_steel" -> 1.4f;
            default -> 1.0f;
        };
    }

    /**
     * Get weapon stats metadata for a weapon type - uses EnumMap for O(1) lookup
     */
    public static WeaponStatsMeta getWeaponStats(String weaponType) {
        try {
            WeaponType type = WeaponType.fromName(weaponType);
            return WEAPON_STATS_BY_ENUM.get(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No weapon stats found for type: " + weaponType, e);
        }
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
    public static final Set<String> AXES = Set.of("greataxe", "morningstar");
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
