package com.concinnity.tfc_weapons_plus.datagen;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.integration.MetalHelper;
import com.concinnity.tfc_weapons_plus.item.ModItems;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Generates TFC item_heat data files for guard and pommel items
 * Required for items used in heating recipes
 */
public final class TFCItemHeatProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    
    public TFCItemHeatProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.lookupProvider = lookupProvider;
    }
    
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookupProvider.thenCompose(provider -> {
            var futures = MetalHelper.getAllMetalNames()
                .flatMap(metalName -> Stream.of(
                    generateItemHeat(metalName, ModItems.getGuardForMetal(metalName), output),
                    generateItemHeat(metalName, ModItems.getPommelForMetal(metalName), output),
                    generateLongswordBladeHeat(metalName, output),
                    generateGreatswordBladeHeat(metalName, output)
                ))
                .filter(future -> future != null)
                .toList();
            
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }
    
    private CompletableFuture<?> generateLongswordBladeHeat(String metalName, CachedOutput output) {
        String normalizedMetal = normalizeMetalName(metalName);
        String bladeId = TFCWeaponsPlus.MODID + ":metal/longsword_blade/" + normalizedMetal;
        ResourceLocation bladeLoc = ResourceLocation.parse(bladeId);
        
        // Check if longsword blade item exists in registry
        if (!BuiltInRegistries.ITEM.containsKey(bladeLoc)) {
            return CompletableFuture.completedFuture(null);
        }
        
        return MetalHelper.getMetalProperties(metalName)
            .map(props -> {
                // Create item_heat JSON for longsword blade
                // Longsword blade uses double_ingots, so heat capacity should be 2x ingot (200 units / 35 = 5.714)
                JsonObject heatData = new JsonObject();
                JsonObject ingredientObj = new JsonObject();
                ingredientObj.addProperty("item", bladeId);
                heatData.add("ingredient", ingredientObj);
                heatData.addProperty("heat_capacity", 5.714f); // Double ingot (200 units / 35 = 5.714)
                heatData.addProperty("forging_temperature", (float) props.meltingPoint());
                heatData.addProperty("welding_temperature", (float) props.meltingPoint());
                
                // Save to TFC namespace: data/tfc/item_heat/{item_path}.json
                String itemPath = bladeId.replace(TFCWeaponsPlus.MODID + ":", "");
                return saveItemHeat(output, heatData, itemPath);
            })
            .orElse(CompletableFuture.completedFuture(null));
    }
    
    private CompletableFuture<?> generateGreatswordBladeHeat(String metalName, CachedOutput output) {
        String normalizedMetal = normalizeMetalName(metalName);
        String bladeId = TFCWeaponsPlus.MODID + ":metal/greatsword_blade/" + normalizedMetal;
        ResourceLocation bladeLoc = ResourceLocation.parse(bladeId);
        
        // Check if greatsword blade item exists in registry
        if (!BuiltInRegistries.ITEM.containsKey(bladeLoc)) {
            return CompletableFuture.completedFuture(null);
        }
        
        return MetalHelper.getMetalProperties(metalName)
            .map(props -> {
                // Create item_heat JSON for greatsword blade
                // Greatsword blade uses double_sheets, so heat capacity should be 4x ingot (400 units / 35 = 11.428)
                JsonObject heatData = new JsonObject();
                JsonObject ingredientObj = new JsonObject();
                ingredientObj.addProperty("item", bladeId);
                heatData.add("ingredient", ingredientObj);
                heatData.addProperty("heat_capacity", 11.428f); // Double sheet (400 units / 35 = 11.428)
                heatData.addProperty("forging_temperature", (float) props.meltingPoint());
                heatData.addProperty("welding_temperature", (float) props.meltingPoint());
                
                // Save to TFC namespace: data/tfc/item_heat/{item_path}.json
                String itemPath = bladeId.replace(TFCWeaponsPlus.MODID + ":", "");
                return saveItemHeat(output, heatData, itemPath);
            })
            .orElse(CompletableFuture.completedFuture(null));
    }
    
    private String normalizeMetalName(String metalName) {
        return metalName.toLowerCase()
            .replace(" ", "_")
            .replace("-", "_");
    }
    
    private CompletableFuture<?> generateItemHeat(String metalName, java.util.Optional<net.minecraft.world.item.Item> itemOpt, CachedOutput output) {
        if (itemOpt.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        net.minecraft.world.item.Item item = itemOpt.get();
        return MetalHelper.getMetalProperties(metalName)
            .map(props -> {
                String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
                
                // Create item_heat JSON
                JsonObject heatData = new JsonObject();
                // Ingredient can be a string (item ID) or object with "item" property
                com.google.gson.JsonObject ingredientObj = new com.google.gson.JsonObject();
                ingredientObj.addProperty("item", itemId);
                heatData.add("ingredient", ingredientObj);
                heatData.addProperty("heat_capacity", 2.857f); // Same as TFC ingots (100 units / 35 = 2.857)
                heatData.addProperty("forging_temperature", (float) props.meltingPoint());
                heatData.addProperty("welding_temperature", (float) props.meltingPoint());
                
                // Save to TFC namespace: data/tfc/item_heat/{item_path}.json
                String itemPath = itemId.replace(TFCWeaponsPlus.MODID + ":", "");
                return saveItemHeat(output, heatData, itemPath);
            })
            .orElse(CompletableFuture.completedFuture(null));
    }
    
    
    private CompletableFuture<?> saveItemHeat(CachedOutput output, JsonObject json, String itemPath) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("tfc", "item_heat/" + itemPath);
        Path path = this.output.getOutputFolder().resolve("data/" + id.getNamespace() + "/" + id.getPath() + ".json");
        return DataProvider.saveStable(output, json, path);
    }
    
    @Override
    public String getName() {
        return "TFC Item Heat Data";
    }
}

