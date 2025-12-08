package com.concinnity.tfc_weapons_plus.datagen;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.integration.MetalHelper;
import com.concinnity.tfc_weapons_plus.item.ModItems;
import com.concinnity.tfc_weapons_plus.util.NameUtils;

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
            
            var greataxeHeadFutures = MetalHelper.getAllMetalNames()
                .flatMap(metalName -> generateGreatAxeHeadRecipes(metalName, output))
                .toList();
            
            var greathammerHeadFutures = MetalHelper.getAllMetalNames()
                .flatMap(metalName -> generateGreatHammerHeadRecipes(metalName, output))
                .toList();
            
            var morningstarHeadFutures = MetalHelper.getAllMetalNames()
                .flatMap(metalName -> generateMorningstarHeadRecipes(metalName, output))
                .toList();
            
            var allFutures = Stream.of(componentFutures.stream(), heatingFutures.stream(), greataxeHeadFutures.stream(), greathammerHeadFutures.stream(), morningstarHeadFutures.stream())
                .flatMap(s -> s)
                .toList();
            
            return CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]));
        });
    }
    
    private Stream<CompletableFuture<?>> generateComponentAnvilRecipes(String metalName, CachedOutput output) {
        String normalizedMetal = NameUtils.normalizeMetalName(metalName);
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
        
        // Longsword blade from double ingot - matches TFC sword blade rules exactly
        // Check if longsword blade item exists (will be registered separately)
        String longswordBladeId = TFCWeaponsPlus.MODID + ":metal/longsword_blade/" + normalizedMetal;
        ResourceLocation longswordBladeLoc = ResourceLocation.parse(longswordBladeId);
        if (BuiltInRegistries.ITEM.containsKey(longswordBladeLoc)) {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createAnvilRecipeWithTag(
                    output,
                    "metal/longsword_blade/" + normalizedMetal,
                    "c:double_ingots/" + normalizedMetal,
                    longswordBladeId,
                    props.tier(),
                    // TFC sword blade sequence: bend third-to-last, bend second-to-last, hit last
                    List.of("bend_third_last", "bend_second_last", "hit_last")
                ));
            });
        }
        
        // Greatsword blade from double sheet - requires more material than regular sword
        // Check if greatsword blade item exists (will be registered separately)
        String greatswordBladeId = TFCWeaponsPlus.MODID + ":metal/greatsword_blade/" + normalizedMetal;
        ResourceLocation greatswordBladeLoc = ResourceLocation.parse(greatswordBladeId);
        if (BuiltInRegistries.ITEM.containsKey(greatswordBladeLoc)) {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createAnvilRecipeWithTag(
                    output,
                    "metal/greatsword_blade/" + normalizedMetal,
                    "c:double_sheets/" + normalizedMetal,
                    greatswordBladeId,
                    props.tier(),
                    // TFC sword blade sequence: bend third-to-last, bend second-to-last, hit last
                    List.of("bend_third_last", "bend_second_last", "hit_last")
                ));
            });
        }
        
        // Shortsword blade from single ingot - smaller weapon, less material
        // Check if shortsword blade item exists (will be registered separately)
        String shortswordBladeId = TFCWeaponsPlus.MODID + ":metal/shortsword_blade/" + normalizedMetal;
        ResourceLocation shortswordBladeLoc = ResourceLocation.parse(shortswordBladeId);
        if (BuiltInRegistries.ITEM.containsKey(shortswordBladeLoc)) {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createAnvilRecipeWithTag(
                    output,
                    "metal/shortsword_blade/" + normalizedMetal,
                    "c:ingots/" + normalizedMetal,
                    shortswordBladeId,
                    props.tier(),
                    // Simpler sequence for smaller blade: hit second-to-last, hit last
                    List.of("hit_second_last", "hit_last")
                ));
            });
        }
        
        return futures.stream();
    }
    
    private Stream<CompletableFuture<?>> generateGreatAxeHeadRecipes(String metalName, CachedOutput output) {
        String normalizedMetal = NameUtils.normalizeMetalName(metalName);
        List<CompletableFuture<?>> futures = new java.util.ArrayList<>();
        
        // Greataxe head from single sheet
        ModItems.getGreatAxeHeadForMetal(metalName).ifPresent(head -> {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createAnvilRecipeWithTag(
                    output,
                    "metal/greataxe_head/" + normalizedMetal,
                    "c:sheets/" + normalizedMetal,
                    BuiltInRegistries.ITEM.getKey(head).toString(),
                    props.tier(),
                    List.of("punch_last", "hit_second_last")
                ));
            });
        });
        
        return futures.stream();
    }
    
    private Stream<CompletableFuture<?>> generateGreatHammerHeadRecipes(String metalName, CachedOutput output) {
        String normalizedMetal = NameUtils.normalizeMetalName(metalName);
        List<CompletableFuture<?>> futures = new java.util.ArrayList<>();
        
        // Greathammer head from double sheet
        ModItems.getGreatHammerHeadForMetal(metalName).ifPresent(head -> {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createAnvilRecipeWithTag(
                    output,
                    "metal/greathammer_head/" + normalizedMetal,
                    "c:double_sheets/" + normalizedMetal,
                    BuiltInRegistries.ITEM.getKey(head).toString(),
                    props.tier(),
                    List.of("punch_last", "hit_second_last")
                ));
            });
        });
        
        return futures.stream();
    }
    
    private Stream<CompletableFuture<?>> generateMorningstarHeadRecipes(String metalName, CachedOutput output) {
        String normalizedMetal = NameUtils.normalizeMetalName(metalName);
        List<CompletableFuture<?>> futures = new java.util.ArrayList<>();
        
        // Morningstar head from single ingot
        ModItems.getMorningstarHeadForMetal(metalName).ifPresent(head -> {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createAnvilRecipeWithTag(
                    output,
                    "metal/morningstar_head/" + normalizedMetal,
                    "c:ingots/" + normalizedMetal,
                    BuiltInRegistries.ITEM.getKey(head).toString(),
                    props.tier(),
                    List.of("hit_second_last", "hit_last")
                ));
            });
        });
        
        return futures.stream();
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
        String normalizedMetal = NameUtils.normalizeMetalName(metalName);
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
        
        // Longsword blade heating recipe - double ingot = 200 units
        String longswordBladeId = TFCWeaponsPlus.MODID + ":metal/longsword_blade/" + normalizedMetal;
        ResourceLocation longswordBladeLoc = ResourceLocation.parse(longswordBladeId);
        if (BuiltInRegistries.ITEM.containsKey(longswordBladeLoc)) {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createHeatingRecipe(
                    output,
                    "metal/longsword_blade/" + normalizedMetal,
                    longswordBladeId,
                    props.meltingPoint(),
                    200, // Double ingot = 200 units (same as TFC sword blade)
                    normalizedMetal
                ));
            });
        }
        
        // Greatsword blade heating recipe - double sheet = 400 units
        String greatswordBladeId = TFCWeaponsPlus.MODID + ":metal/greatsword_blade/" + normalizedMetal;
        ResourceLocation greatswordBladeLoc = ResourceLocation.parse(greatswordBladeId);
        if (BuiltInRegistries.ITEM.containsKey(greatswordBladeLoc)) {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createHeatingRecipe(
                    output,
                    "metal/greatsword_blade/" + normalizedMetal,
                    greatswordBladeId,
                    props.meltingPoint(),
                    400, // Double sheet = 400 units (more than double ingot)
                    normalizedMetal
                ));
            });
        }
        
        // Shortsword blade heating recipe - single ingot = 100 units
        String shortswordBladeId = TFCWeaponsPlus.MODID + ":metal/shortsword_blade/" + normalizedMetal;
        ResourceLocation shortswordBladeLoc = ResourceLocation.parse(shortswordBladeId);
        if (BuiltInRegistries.ITEM.containsKey(shortswordBladeLoc)) {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createHeatingRecipe(
                    output,
                    "metal/shortsword_blade/" + normalizedMetal,
                    shortswordBladeId,
                    props.meltingPoint(),
                    100, // Single ingot = 100 units (same as guard/pommel)
                    normalizedMetal
                ));
            });
        }
        
        // Greataxe head heating recipe - single sheet = 200 units
        String greataxeHeadId = TFCWeaponsPlus.MODID + ":metal/greataxe_head/" + normalizedMetal;
        ResourceLocation greataxeHeadLoc = ResourceLocation.parse(greataxeHeadId);
        if (BuiltInRegistries.ITEM.containsKey(greataxeHeadLoc)) {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createHeatingRecipe(
                    output,
                    "metal/greataxe_head/" + normalizedMetal,
                    greataxeHeadId,
                    props.meltingPoint(),
                    200, // Single sheet = 200 units
                    normalizedMetal
                ));
            });
        }
        
        // Greathammer head heating recipe - double sheet = 400 units
        String greathammerHeadId = TFCWeaponsPlus.MODID + ":metal/greathammer_head/" + normalizedMetal;
        ResourceLocation greathammerHeadLoc = ResourceLocation.parse(greathammerHeadId);
        if (BuiltInRegistries.ITEM.containsKey(greathammerHeadLoc)) {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createHeatingRecipe(
                    output,
                    "metal/greathammer_head/" + normalizedMetal,
                    greathammerHeadId,
                    props.meltingPoint(),
                    400, // Double sheet = 400 units
                    normalizedMetal
                ));
            });
        }
        
        // Morningstar head heating recipe - single ingot = 100 units
        String morningstarHeadId = TFCWeaponsPlus.MODID + ":metal/morningstar_head/" + normalizedMetal;
        ResourceLocation morningstarHeadLoc = ResourceLocation.parse(morningstarHeadId);
        if (BuiltInRegistries.ITEM.containsKey(morningstarHeadLoc)) {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                futures.add(createHeatingRecipe(
                    output,
                    "metal/morningstar_head/" + normalizedMetal,
                    morningstarHeadId,
                    props.meltingPoint(),
                    100, // Single ingot = 100 units (same as guard/pommel)
                    normalizedMetal
                ));
            });
        }
        
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
