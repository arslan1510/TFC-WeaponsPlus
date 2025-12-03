package com.concinnity.tfc_weapons_plus.item;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.integration.MetalHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Central registry for all mod items
 * Uses functional programming patterns
 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TFCWeaponsPlus.MODID);
    
    // Basic component items
    public static final DeferredItem<Item> GRIP = ITEMS.register("wood/grip", 
        () -> new GripItem(new Item.Properties()));
    
    // Metal variant items for guard, pommel, and hilt
    // Maps: metal name -> item
    public static final Map<String, DeferredItem<Item>> GUARD_VARIANTS = new HashMap<>();
    public static final Map<String, DeferredItem<Item>> POMMEL_VARIANTS = new HashMap<>();
    public static final Map<String, DeferredItem<Item>> HILT_VARIANTS = new HashMap<>();
    
    static {
        // Register metal variants for guard, pommel, and hilt
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            String normalizedMetal = normalizeMetalName(metalName);
            String guardId = "metal/guard/" + normalizedMetal;
            String pommelId = "metal/pommel/" + normalizedMetal;
            String hiltId = "metal/hilt/" + normalizedMetal;
            
            GUARD_VARIANTS.put(metalName, ITEMS.register(guardId, 
                () -> new MetalComponentItem(ComponentType.GUARD, metalName, new Item.Properties())));
            
            POMMEL_VARIANTS.put(metalName, ITEMS.register(pommelId, 
                () -> new MetalComponentItem(ComponentType.POMMEL, metalName, new Item.Properties())));
            
            // Hilt is assembled from grip + guard + pommel, but has metal-specific visual
            HILT_VARIANTS.put(metalName, ITEMS.register(hiltId, 
                () -> new MetalComponentItem(ComponentType.HILT, metalName, new Item.Properties())));
        });
    }
    
    /**
     * Get all registered items as a stream for functional operations
     * Note: Blades are provided by TFC, not this mod
     */
    public static Stream<Item> getAllItems() {
        return Stream.concat(
            Stream.of(GRIP.get()),
            Stream.concat(
                GUARD_VARIANTS.values().stream().map(DeferredItem::get),
                Stream.concat(
                    POMMEL_VARIANTS.values().stream().map(DeferredItem::get),
                    HILT_VARIANTS.values().stream().map(DeferredItem::get)
                )
            )
        );
    }
    
    /**
     * Get guard item for a specific metal
     */
    public static Optional<Item> getGuardForMetal(String metalName) {
        return Optional.ofNullable(GUARD_VARIANTS.get(metalName))
            .map(DeferredItem::get);
    }
    
    /**
     * Get pommel item for a specific metal
     */
    public static Optional<Item> getPommelForMetal(String metalName) {
        return Optional.ofNullable(POMMEL_VARIANTS.get(metalName))
            .map(DeferredItem::get);
    }
    
    /**
     * Get hilt item for a specific metal
     */
    public static Optional<Item> getHiltForMetal(String metalName) {
        return Optional.ofNullable(HILT_VARIANTS.get(metalName))
            .map(DeferredItem::get);
    }
    
    /**
     * Normalize metal name for use in resource paths
     */
    private static String normalizeMetalName(String metalName) {
        return metalName.toLowerCase()
            .replace(" ", "_")
            .replace("-", "_");
    }
    
    private ModItems() {
        // Utility class
    }
}

