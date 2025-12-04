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
                    generateItemHeat(metalName, ModItems.getPommelForMetal(metalName), output)
                ))
                .filter(future -> future != null)
                .toList();
            
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
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

