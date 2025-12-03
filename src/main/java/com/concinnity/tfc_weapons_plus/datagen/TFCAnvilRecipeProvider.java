package com.concinnity.tfc_weapons_plus.datagen;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.integration.MetalHelper;
import com.concinnity.tfc_weapons_plus.item.ModItems;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Generates TFC anvil recipes for blacksmithing components
 */
public final class TFCAnvilRecipeProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    
    public TFCAnvilRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.lookupProvider = lookupProvider;
    }
    
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookupProvider.thenCompose(provider -> {
            var componentFutures = MetalHelper.getAllMetalNames()
                .flatMap(metalName -> generateComponentAnvilRecipes(metalName, output))
                .toList();
            
            var heatingFutures = MetalHelper.getAllMetalNames()
                .flatMap(metalName -> generateHeatingRecipes(metalName, output))
                .toList();
            
            var allFutures = Stream.concat(componentFutures.stream(), heatingFutures.stream())
                .toList();
            
            return CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]));
        });
    }
    
    private Stream<CompletableFuture<?>> generateComponentAnvilRecipes(String metalName, CachedOutput output) {
        String normalizedMetal = normalizeMetalName(metalName);
        List<CompletableFuture<?>> futures = new java.util.ArrayList<>();
        
        // Grip from any wood lumber (using tag) - only generate once, not per metal
        // Wood items typically don't have tier requirements, use tier 0
        if (metalName.equals("copper")) { // Only generate grip recipe once
            futures.add(createAnvilRecipeWithTag(
                output,
                "wood/grip",
                "tfc:lumber",
                BuiltInRegistries.ITEM.getKey(ModItems.GRIP.get()).toString(),
                0, // No tier requirement for wood
                List.of("hit_last", "draw_any", "shrink_not_last")
            ));
        }
        
        // Guard from metal ingot - TFC uses tags: c:ingots/{metal}
        // Use simpler rule combination like TFC's saw_blade recipes
        ModItems.getGuardForMetal(metalName).ifPresent(guard -> {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createAnvilRecipeWithTag(
                    output,
                    "metal/guard/" + normalizedMetal,
                    "c:ingots/" + normalizedMetal,
                    BuiltInRegistries.ITEM.getKey(guard).toString(),
                    props.tier(),
                    List.of("hit_second_last", "hit_last")
                ));
            });
        });
        
        // Pommel from metal ingot - TFC uses tags: c:ingots/{metal}
        // Use simpler rule combination like TFC's saw_blade recipes
        ModItems.getPommelForMetal(metalName).ifPresent(pommel -> {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createAnvilRecipeWithTag(
                    output,
                    "metal/pommel/" + normalizedMetal,
                    "c:ingots/" + normalizedMetal,
                    BuiltInRegistries.ITEM.getKey(pommel).toString(),
                    props.tier(),
                    List.of("hit_second_last", "hit_last")
                ));
            });
        });
        
        return futures.stream();
    }
    
    private String normalizeMetalName(String metalName) {
        return metalName.toLowerCase()
            .replace(" ", "_")
            .replace("-", "_");
    }
    
    private CompletableFuture<?> createAnvilRecipeWithStrings(
        CachedOutput output,
        String recipeName,
        String inputId,
        String resultId,
        int tier,
        List<String> rules
    ) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "tfc:anvil");
        recipe.addProperty("apply_bonus", true);
        
        // Ingredient - TFC uses object format: {"item":"..."} or {"tag":"..."}
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", inputId);
        recipe.add("ingredient", ingredient);
        
        // Result is an object with count and id (order matters - count first)
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", resultId);
        recipe.add("result", result);
        
        // Tier requirement
        recipe.addProperty("tier", tier);
        
        // Rules
        JsonArray rulesArray = new JsonArray();
        rules.forEach(rulesArray::add);
        recipe.add("rules", rulesArray);
        
        // Save to mod namespace: data/tfc_weapons_plus/recipe/anvil/{recipeName}.json
        return saveRecipe(output, recipe, "anvil/" + recipeName);
    }
    
    private CompletableFuture<?> createAnvilRecipeWithTag(
        CachedOutput output,
        String recipeName,
        String tagId,
        String resultId,
        int tier,
        List<String> rules
    ) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "tfc:anvil");
        recipe.addProperty("apply_bonus", true);
        
        // Ingredient using tag
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("tag", tagId);
        recipe.add("ingredient", ingredient);
        
        // Result is an object with count and id (order matters - count first)
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", resultId);
        recipe.add("result", result);
        
        // Tier requirement
        recipe.addProperty("tier", tier);
        
        // Rules
        JsonArray rulesArray = new JsonArray();
        rules.forEach(rulesArray::add);
        recipe.add("rules", rulesArray);
        
        // Save to mod namespace: data/tfc_weapons_plus/recipe/anvil/{recipeName}.json
        return saveRecipe(output, recipe, "anvil/" + recipeName);
    }
    
    private Stream<CompletableFuture<?>> generateHeatingRecipes(String metalName, CachedOutput output) {
        String normalizedMetal = normalizeMetalName(metalName);
        List<CompletableFuture<?>> futures = new java.util.ArrayList<>();
        
        // Guard heating recipe - ingot = 100 units
        ModItems.getGuardForMetal(metalName).ifPresent(guard -> {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createHeatingRecipe(
                    output,
                    "metal/guard/" + normalizedMetal,
                    BuiltInRegistries.ITEM.getKey(guard).toString(),
                    props.meltingPoint(),
                    100, // Ingot = 100 units
                    normalizedMetal
                ));
            });
        });
        
        // Pommel heating recipe - ingot = 100 units
        ModItems.getPommelForMetal(metalName).ifPresent(pommel -> {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createHeatingRecipe(
                    output,
                    "metal/pommel/" + normalizedMetal,
                    BuiltInRegistries.ITEM.getKey(pommel).toString(),
                    props.meltingPoint(),
                    100, // Ingot = 100 units
                    normalizedMetal
                ));
            });
        });
        
        return futures.stream();
    }
    
    private CompletableFuture<?> createHeatingRecipe(
        CachedOutput output,
        String itemName,
        String itemId,
        int temperature,
        int amount,
        String metalName
    ) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "tfc:heating");
        
        // Ingredient
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", itemId);
        recipe.add("ingredient", ingredient);
        
        // Temperature
        recipe.addProperty("temperature", (float) temperature);
        
        // Result fluid - wrought_iron melts into cast_iron (matching TFC sword blade behavior)
        JsonObject resultFluid = new JsonObject();
        String resultMetal = metalName.equals("wrought_iron") ? "cast_iron" : metalName;
        resultFluid.addProperty("id", "tfc:metal/" + resultMetal);
        resultFluid.addProperty("amount", amount);
        recipe.add("result_fluid", resultFluid);
        
        // Save to mod namespace: data/tfc_weapons_plus/recipe/heating/{itemName}.json
        return saveRecipe(output, recipe, "heating/" + itemName);
    }
    
    private CompletableFuture<?> saveRecipe(CachedOutput output, JsonObject json, String recipePath) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, recipePath);
        Path path = this.output.getOutputFolder().resolve("data/" + id.getNamespace() + "/recipe/" + id.getPath() + ".json");
        return DataProvider.saveStable(output, json, path);
    }
    
    @Override
    public String getName() {
        return "TFC Anvil Recipes";
    }
}
