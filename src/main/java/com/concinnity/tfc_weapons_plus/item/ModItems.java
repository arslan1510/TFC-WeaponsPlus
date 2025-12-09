package com.concinnity.tfc_weapons_plus.item;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.item.weapon.WeaponItemFactory;
import com.concinnity.tfc_weapons_plus.util.NameUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import net.dries007.tfc.common.LevelTier;
import net.dries007.tfc.util.Metal;
import java.util.Arrays;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TFCWeaponsPlus.MOD_ID);

    public static final DeferredItem<Item> GRIP = ITEMS.register("wood/grip", () -> new ComponentItem(ComponentType.GRIP, "", new Item.Properties()));
    private static final Map<String, Map<String, DeferredItem<Item>>> REGISTRY = new HashMap<>();

    // Item cache for fast lookups (lazy-initialized after registration)
    private static final Map<String, Map<String, Item>> ITEM_CACHE = new HashMap<>();
    private static volatile boolean cacheInitialized = false;

    private record ItemDef(
        String type,
        String pathPrefix,
        boolean weapon,
        BiFunction<Metal, Item.Properties, Item> factory
    ) {
        String path(String metalName) {
            return pathPrefix + "/" + NameUtils.normalizeMetalName(metalName);
        }
    }

    /**
     * Public view of item definitions without factories, for external consumers (e.g., WeaponRegistry).
     */
    public record Definition(String type, String pathPrefix, boolean weapon) {
        public String path(String metalName) {
            return pathPrefix + "/" + NameUtils.normalizeMetalName(metalName);
        }
    }

    private static final List<ItemDef> DEFINITIONS = List.of(
        new ItemDef("guard", "metal/guard", false, (metal, props) -> new ComponentItem(ComponentType.GUARD, metal.getSerializedName(), new Item.Properties())),
        new ItemDef("pommel", "metal/pommel", false, (metal, props) -> new ComponentItem(ComponentType.POMMEL, metal.getSerializedName(), new Item.Properties())),
        new ItemDef("hilt", "metal/hilt", false, (metal, props) -> new ComponentItem(ComponentType.HILT, metal.getSerializedName(), new Item.Properties())),
        new ItemDef("longsword_blade", "metal/longsword_blade", false, (metal, props) -> new ComponentItem(ComponentType.LONGSWORD_BLADE, metal.getSerializedName(), new Item.Properties())),
        new ItemDef("greatsword_blade", "metal/greatsword_blade", false, (metal, props) -> new ComponentItem(ComponentType.GREATSWORD_BLADE, metal.getSerializedName(), new Item.Properties())),
        new ItemDef("shortsword_blade", "metal/shortsword_blade", false, (metal, props) -> new ComponentItem(ComponentType.SHORTSWORD_BLADE, metal.getSerializedName(), new Item.Properties())),
        new ItemDef("greataxe_head", "metal/greataxe_head", false, (metal, props) -> new ComponentItem(ComponentType.GREATAXE_HEAD, metal.getSerializedName(), new Item.Properties())),
        new ItemDef("greathammer_head", "metal/greathammer_head", false, (metal, props) -> new ComponentItem(ComponentType.GREATHAMMER_HEAD, metal.getSerializedName(), new Item.Properties())),
        new ItemDef("morningstar_head", "metal/morningstar_head", false, (metal, props) -> new ComponentItem(ComponentType.MORNINGSTAR_HEAD, metal.getSerializedName(), new Item.Properties())),

        new ItemDef("longsword", "metal/longsword", true, (metal, props) -> WeaponItemFactory.createWeapon("longsword", metal.getSerializedName(), props)),
        new ItemDef("greatsword", "metal/greatsword", true, (metal, props) -> WeaponItemFactory.createWeapon("greatsword", metal.getSerializedName(), props)),
        new ItemDef("shortsword", "metal/shortsword", true, (metal, props) -> WeaponItemFactory.createWeapon("shortsword", metal.getSerializedName(), props)),
        new ItemDef("greataxe", "metal/greataxe", true, (metal, props) -> WeaponItemFactory.createWeapon("greataxe", metal.getSerializedName(), props)),
        new ItemDef("greathammer", "metal/greathammer", true, (metal, props) -> WeaponItemFactory.createWeapon("greathammer", metal.getSerializedName(), props)),
        new ItemDef("morningstar", "metal/morningstar", true, (metal, props) -> WeaponItemFactory.createWeapon("morningstar", metal.getSerializedName(), props)),
        new ItemDef("quarterstaff", "metal/quarterstaff", true, (metal, props) -> WeaponItemFactory.createWeapon("quarterstaff", metal.getSerializedName(), props))
    );

    static {
        registerItems();
    }

    public static void registerItems() {
        metals().forEach(metal -> {
            String metalName = metal.getSerializedName();
            LevelTier tier = metal.toolTier();
            Item.Properties weaponProps = new Item.Properties().durability(tier.getUses());

            DEFINITIONS.forEach(def -> {
                Map<String, DeferredItem<Item>> byMetal = REGISTRY.computeIfAbsent(def.type(), k -> new HashMap<>());
                Item.Properties props = def.weapon ? weaponProps : new Item.Properties();
                DeferredItem<Item> holder = ITEMS.register(def.path(metalName), () -> def.factory.apply(metal, props));
                byMetal.put(metalName, holder);
            });
        });
    }

    /**
     * Initialize item cache after registration is complete.
     * Call this after mod setup.
     */
    public static void initializeCache() {
        if (cacheInitialized) return;

        synchronized (ITEM_CACHE) {
            if (cacheInitialized) return;

            REGISTRY.forEach((type, metalMap) -> {
                Map<String, Item> cachedItems = new HashMap<>();
                metalMap.forEach((metal, deferred) -> cachedItems.put(metal, deferred.get()));
                ITEM_CACHE.put(type, cachedItems);
            });

            cacheInitialized = true;
        }
    }

    public static Stream<Item> getAllItems() {
        Stream<Item> registered = REGISTRY.values().stream()
            .flatMap(map -> map.values().stream().map(DeferredItem::get));
        return Stream.concat(Stream.of(GRIP.get()), registered);
    }

    private static Stream<Metal> metals() {
        return Arrays.stream(Metal.values()).filter(m -> m.tier() > 0 && m.allParts());
    }

    // Direct getters (no Optional) - use cache if available
    public static Item getGuardForMetal(String metalName) {
        return getItem("guard", metalName);
    }

    public static Item getPommelForMetal(String metalName) {
        return getItem("pommel", metalName);
    }

    public static Item getHiltForMetal(String metalName) {
        return getItem("hilt", metalName);
    }

    public static Item getLongswordBladeForMetal(String metalName) {
        return getItem("longsword_blade", metalName);
    }

    public static Item getLongswordForMetal(String metalName) {
        return getItem("longsword", metalName);
    }

    public static Item getGreatswordBladeForMetal(String metalName) {
        return getItem("greatsword_blade", metalName);
    }

    public static Item getGreatswordForMetal(String metalName) {
        return getItem("greatsword", metalName);
    }

    public static Item getShortswordBladeForMetal(String metalName) {
        return getItem("shortsword_blade", metalName);
    }

    public static Item getShortswordForMetal(String metalName) {
        return getItem("shortsword", metalName);
    }

    public static Item getGreatAxeHeadForMetal(String metalName) {
        return getItem("greataxe_head", metalName);
    }

    public static Item getGreatAxeForMetal(String metalName) {
        return getItem("greataxe", metalName);
    }

    public static Item getGreatHammerHeadForMetal(String metalName) {
        return getItem("greathammer_head", metalName);
    }

    public static Item getGreatHammerForMetal(String metalName) {
        return getItem("greathammer", metalName);
    }

    public static Item getMorningstarHeadForMetal(String metalName) {
        return getItem("morningstar_head", metalName);
    }

    public static Item getMorningstarForMetal(String metalName) {
        return getItem("morningstar", metalName);
    }

    public static Item getQuarterstaffForMetal(String metalName) {
        return getItem("quarterstaff", metalName);
    }

    /**
     * Fast item lookup using cache if available, otherwise use deferred registry.
     */
    private static Item getItem(String type, String metalName) {
        if (cacheInitialized) {
            Map<String, Item> cachedItems = ITEM_CACHE.get(type);
            if (cachedItems != null) {
                Item item = cachedItems.get(metalName);
                if (item != null) return item;
            }
        }

        // Fallback to deferred registry
        Map<String, DeferredItem<Item>> typeMap = REGISTRY.get(type);
        if (typeMap == null) {
            throw new IllegalArgumentException("Unknown item type: " + type);
        }

        DeferredItem<Item> deferred = typeMap.get(metalName);
        if (deferred == null) {
            throw new IllegalArgumentException("No item found for type: " + type + ", metal: " + metalName);
        }

        return deferred.get();
    }

    /**
     * Public read-only definitions (type/pathPrefix/weapon flag) for other systems.
     */
    public static List<Definition> definitions() {
        return DEFINITIONS.stream()
            .map(d -> new Definition(d.type(), d.pathPrefix(), d.weapon()))
            .toList();
    }

    private ModItems() {}
}
