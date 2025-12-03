package com.concinnity.tfc_weapons_plus.datagen;

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
 * Generates TFC welding recipes for sword assembly (hilt + blade)
 * These recipes override TFC's default rod + blade recipes
 */
public final class TFCWeldingRecipeProvider implements DataProvider {
    private static final String TFC_NAMESPACE = "tfc";
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    
    public TFCWeldingRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.lookupProvider = lookupProvider;
    }
    
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookupProvider.thenCompose(provider -> {
            var futures = MetalHelper.getAllMetalNames()
                .map(metalName -> generateWeldingRecipe(metalName, output))
                .toList();
            
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }
    
    /**
     * Generate a welding recipe for a specific metal type
     * Recipe: hilt (same metal) + blade (same metal) = sword (same metal)
     */
    private CompletableFuture<?> generateWeldingRecipe(String metalName, CachedOutput output) {
        String normalizedMetal = normalizeMetalName(metalName);
        
        // Get hilt item for this metal
        return ModItems.getHiltForMetal(metalName)
            .map(hilt -> {
                // Create welding recipe JSON
                JsonObject recipe = new JsonObject();
                recipe.addProperty("type", "tfc:welding");
                recipe.addProperty("bonus", "copy_best");
                
                // First input: hilt from our mod
                JsonObject firstInput = new JsonObject();
                firstInput.addProperty("item", BuiltInRegistries.ITEM.getKey(hilt).toString());
                recipe.add("first_input", firstInput);
                
                // Second input: sword blade from TFC (same metal type)
                JsonObject secondInput = new JsonObject();
                secondInput.addProperty("item", "tfc:metal/sword_blade/" + normalizedMetal);
                recipe.add("second_input", secondInput);
                
                // Result: sword from TFC (same metal type)
                JsonObject result = new JsonObject();
                result.addProperty("count", 1);
                result.addProperty("id", "tfc:metal/sword/" + normalizedMetal);
                recipe.add("result", result);
                
                // Save to TFC namespace to override default recipes
                // Path: data/tfc/recipe/welding/metal/sword/{metal}.json
                return saveRecipeToTFCNamespace(output, recipe, "welding/metal/sword/" + normalizedMetal);
            })
            .orElse(CompletableFuture.completedFuture(null));
    }
    
    private String normalizeMetalName(String metalName) {
        return metalName.toLowerCase()
            .replace(" ", "_")
            .replace("-", "_");
    }
    
    /**
     * Save recipe to TFC namespace (to override TFC's default recipes)
     */
    private CompletableFuture<?> saveRecipeToTFCNamespace(CachedOutput output, JsonObject json, String recipePath) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TFC_NAMESPACE, recipePath);
        Path path = this.output.getOutputFolder().resolve("data/" + id.getNamespace() + "/recipe/" + id.getPath() + ".json");
        return DataProvider.saveStable(output, json, path);
    }
    
    @Override
    public String getName() {
        return "TFC Welding Recipes (Sword Assembly)";
    }
}

