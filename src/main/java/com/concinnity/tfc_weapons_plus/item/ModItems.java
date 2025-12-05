package com.concinnity.tfc_weapons_plus.item;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.integration.MetalHelper;
import com.concinnity.tfc_weapons_plus.item.weapon.GreatswordItem;
import com.concinnity.tfc_weapons_plus.item.weapon.LongswordItem;
import com.concinnity.tfc_weapons_plus.item.weapon.WarAxeItem;
import com.concinnity.tfc_weapons_plus.util.NameUtils;

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
    
    // Longsword blade variants (9 metals)
    public static final Map<String, DeferredItem<Item>> LONGSWORD_BLADE_VARIANTS = new HashMap<>();
    
    // Longsword items (9 metals - blade and hilt must match)
    // Key format: metal name
    public static final Map<String, DeferredItem<Item>> LONGSWORD_VARIANTS = new HashMap<>();
    
    // Greatsword blade variants (9 metals)
    public static final Map<String, DeferredItem<Item>> GREATSWORD_BLADE_VARIANTS = new HashMap<>();
    
    // Greatsword items (9 metals - blade and hilt must match)
    // Key format: metal name
    public static final Map<String, DeferredItem<Item>> GREATSWORD_VARIANTS = new HashMap<>();
    
    // Waraxe head component variants (9 metals)
    public static final Map<String, DeferredItem<Item>> WARAXE_HEAD_VARIANTS = new HashMap<>();
    
    // Waraxe items (9 metals)
    public static final Map<String, DeferredItem<Item>> WARAXE_VARIANTS = new HashMap<>();
    
    static {
        // Register metal variants for guard, pommel, and hilt
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            String normalizedMetal = NameUtils.normalizeMetalName(metalName);
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
            
            // Register longsword blade for this metal
            String longswordBladeId = "metal/longsword_blade/" + normalizedMetal;
            LONGSWORD_BLADE_VARIANTS.put(metalName, ITEMS.register(longswordBladeId, 
                () -> new MetalComponentItem(ComponentType.LONGSWORD_BLADE, metalName, new Item.Properties())));
            
            // Register longsword for this metal (blade and hilt must match)
            // Use weapon properties with durability matching TFC sword
            String longswordId = "metal/longsword/" + normalizedMetal;
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                Item.Properties weaponProps = new Item.Properties()
                    .durability(props.durability());
                
                LONGSWORD_VARIANTS.put(metalName, ITEMS.register(longswordId, 
                    () -> new LongswordItem(metalName, weaponProps)));
            });
            
            // Register greatsword blade for this metal
            String greatswordBladeId = "metal/greatsword_blade/" + normalizedMetal;
            GREATSWORD_BLADE_VARIANTS.put(metalName, ITEMS.register(greatswordBladeId, 
                () -> new MetalComponentItem(ComponentType.GREATSWORD_BLADE, metalName, new Item.Properties())));
            
            // Register greatsword for this metal (blade and hilt must match)
            // Use weapon properties with durability matching TFC sword
            String greatswordId = "metal/greatsword/" + normalizedMetal;
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                Item.Properties weaponProps = new Item.Properties()
                    .durability(props.durability());
                
                GREATSWORD_VARIANTS.put(metalName, ITEMS.register(greatswordId, 
                    () -> new GreatswordItem(metalName, weaponProps)));
            });
            
            // Register waraxe head for this metal (single sheet requirement handled in recipes)
            String waraxeHeadId = "metal/waraxe_head/" + normalizedMetal;
            WARAXE_HEAD_VARIANTS.put(metalName, ITEMS.register(waraxeHeadId,
                () -> new MetalComponentItem(ComponentType.WARAXE_HEAD, metalName, new Item.Properties())));
            
            // Register waraxe item for this metal
            String waraxeId = "metal/waraxe/" + normalizedMetal;
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                Item.Properties weaponProps = new Item.Properties()
                    .durability(props.durability());
                
                WARAXE_VARIANTS.put(metalName, ITEMS.register(waraxeId,
                    () -> new WarAxeItem(metalName, weaponProps)));
            });
        });
    }
    
    /**
     * Get all registered items as a stream for functional operations
     */
    public static Stream<Item> getAllItems() {
        return Stream.of(
            Stream.of(GRIP.get()),
            GUARD_VARIANTS.values().stream().map(DeferredItem::get),
            POMMEL_VARIANTS.values().stream().map(DeferredItem::get),
            HILT_VARIANTS.values().stream().map(DeferredItem::get),
            LONGSWORD_BLADE_VARIANTS.values().stream().map(DeferredItem::get),
            LONGSWORD_VARIANTS.values().stream().map(DeferredItem::get),
            GREATSWORD_BLADE_VARIANTS.values().stream().map(DeferredItem::get),
            GREATSWORD_VARIANTS.values().stream().map(DeferredItem::get),
            WARAXE_HEAD_VARIANTS.values().stream().map(DeferredItem::get),
            WARAXE_VARIANTS.values().stream().map(DeferredItem::get)
        ).flatMap(s -> s);
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
     * Get longsword blade item for a specific metal
     */
    public static Optional<Item> getLongswordBladeForMetal(String metalName) {
        return Optional.ofNullable(LONGSWORD_BLADE_VARIANTS.get(metalName))
            .map(DeferredItem::get);
    }
    
    /**
     * Get longsword item for a specific metal (blade and hilt are the same metal)
     */
    public static Optional<Item> getLongswordForMetal(String metalName) {
        return Optional.ofNullable(LONGSWORD_VARIANTS.get(metalName))
            .map(DeferredItem::get);
    }
    
    /**
     * Get greatsword blade item for a specific metal
     */
    public static Optional<Item> getGreatswordBladeForMetal(String metalName) {
        return Optional.ofNullable(GREATSWORD_BLADE_VARIANTS.get(metalName))
            .map(DeferredItem::get);
    }
    
    /**
     * Get greatsword item for a specific metal (blade and hilt are the same metal)
     */
    public static Optional<Item> getGreatswordForMetal(String metalName) {
        return Optional.ofNullable(GREATSWORD_VARIANTS.get(metalName))
            .map(DeferredItem::get);
    }
    
    /**
     * Get waraxe head item for a specific metal
     */
    public static Optional<Item> getWarAxeHeadForMetal(String metalName) {
        return Optional.ofNullable(WARAXE_HEAD_VARIANTS.get(metalName))
            .map(DeferredItem::get);
    }
    
    /**
     * Get waraxe item for a specific metal
     */
    public static Optional<Item> getWarAxeForMetal(String metalName) {
        return Optional.ofNullable(WARAXE_VARIANTS.get(metalName))
            .map(DeferredItem::get);
    }
    
    private ModItems() {
        // Utility class
    }
}

