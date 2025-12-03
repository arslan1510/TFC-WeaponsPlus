package com.concinnity.tfc_weapons_plus.integration;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;

/**
 * Integration with TerraFirmaCraft mod
 * TFC is required at runtime (optional for data generation)
 */
public final class TFCIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean initialized = false;
    private static final String TFC_NAMESPACE = "tfc";
    
    /**
     * Initialize TFC integration
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        LOGGER.info("TFC integration initialized");
        LOGGER.info("Available TFC metals: {}", MetalHelper.getAllMetalNames().collect(java.util.stream.Collectors.joining(", ")));
        
        initialized = true;
    }
    
    public static Optional<Item> getTFCMetalIngot(String metalName) {
        return getTFCItem(metalName, "ingot");
    }
    
    public static Optional<Item> getTFCMetalSheet(String metalName) {
        return getTFCItem(metalName, "sheet");
    }
    
    /**
     * Get TFC item by metal name and item type
     */
    private static Optional<Item> getTFCItem(String metalName, String itemType) {
        try {
            String normalizedMetal = normalizeMetalName(metalName);
            ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(TFC_NAMESPACE, normalizedMetal + "_" + itemType);
            
            if (BuiltInRegistries.ITEM.containsKey(itemId)) {
                Item item = BuiltInRegistries.ITEM.get(itemId);
                return Optional.of(item);
            } else {
                LOGGER.debug("TFC item not found: {}", itemId);
                return Optional.empty();
            }
        } catch (Exception e) {
            LOGGER.error("Error getting TFC item: {}:{}", metalName, itemType, e);
            return Optional.empty();
        }
    }
    
    public static Optional<Ingredient> getTFCMetalIngotIngredient(String metalName) {
        return getTFCMetalIngot(metalName)
            .map(Ingredient::of);
    }
    
    public static Optional<Ingredient> getTFCMetalSheetIngredient(String metalName) {
        return getTFCMetalSheet(metalName)
            .map(Ingredient::of);
    }
    
    /**
     * Get TFC wood lumber item by wood type
     * Path: tfc:wood/lumber/{wood_type}
     */
    public static Optional<Item> getTFCWoodLumber(String woodType) {
        return getTFCItemByPath("wood/lumber/" + normalizeWoodName(woodType));
    }
    
    public static Optional<Ingredient> getTFCWoodLumberIngredient(String woodType) {
        return getTFCWoodLumber(woodType)
            .map(Ingredient::of);
    }
    
    public static Optional<Ingredient> getTFCWoodLumberIngredientAny() {
        return getTFCWoodLumber("oak")
            .map(Ingredient::of);
    }
    
    public static Optional<Item> getTFCSwordBlade(String metalName) {
        return getTFCItemByPath("metal/sword_blade/" + normalizeMetalName(metalName));
    }
    
    public static Optional<Item> getTFCSword(String metalName) {
        return getTFCItemByPath("metal/sword/" + normalizeMetalName(metalName));
    }
    
    public static Optional<Ingredient> getTFCSwordBladeIngredient(String metalName) {
        return getTFCSwordBlade(metalName)
            .map(Ingredient::of);
    }
    
    public static Optional<Ingredient> getTFCSwordIngredient(String metalName) {
        return getTFCSword(metalName)
            .map(Ingredient::of);
    }
    
    public static Optional<Item> getTFCItemByPath(String itemPath) {
        try {
            ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(TFC_NAMESPACE, itemPath);
            
            if (BuiltInRegistries.ITEM.containsKey(itemId)) {
                Item item = BuiltInRegistries.ITEM.get(itemId);
                return Optional.of(item);
            } else {
                LOGGER.debug("TFC item not found: {}", itemId);
                return Optional.empty();
            }
        } catch (Exception e) {
            LOGGER.error("Error getting TFC item by path: {}", itemPath, e);
            return Optional.empty();
        }
    }
    
    private static String normalizeMetalName(String metalName) {
        return metalName.toLowerCase()
            .replace(" ", "_")
            .replace("-", "_");
    }
    
    private static String normalizeWoodName(String woodName) {
        return woodName.toLowerCase()
            .replace(" ", "_")
            .replace("-", "_");
    }
    
    private TFCIntegration() {}
}

