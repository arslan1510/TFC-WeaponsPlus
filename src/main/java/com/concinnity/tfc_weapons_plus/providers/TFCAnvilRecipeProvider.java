package com.concinnity.tfc_weapons_plus.providers;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.item.ModItems;
import com.concinnity.tfc_weapons_plus.util.NameUtils;
import com.concinnity.tfc_weapons_plus.util.MetalData;
import com.concinnity.tfc_weapons_plus.util.WeaponRegistry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Generates TFC anvil recipes for blacksmithing components
 */
public final class TFCAnvilRecipeProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    private record AnvilSpec(String type, String ingredientFormat, List<String> rules) {}
    private record HeatingSpec(String type, String ingredientId) {}

    private static final List<AnvilSpec> ANVIL_SPECS = List.of(
        new AnvilSpec("guard", "c:ingots/%s", List.of("hit_second_last", "hit_last")),
        new AnvilSpec("pommel", "c:ingots/%s", List.of("hit_second_last", "hit_last")),
        new AnvilSpec("longsword_blade", "c:double_ingots/%s", List.of("bend_third_last", "bend_second_last", "hit_last")),
        new AnvilSpec("greatsword_blade", "c:double_sheets/%s", List.of("bend_third_last", "bend_second_last", "hit_last")),
        new AnvilSpec("shortsword_blade", "c:ingots/%s", List.of("hit_second_last", "hit_last")),
        new AnvilSpec("greataxe_head", "c:sheets/%s", List.of("punch_last", "hit_second_last")),
        new AnvilSpec("greathammer_head", "c:double_sheets/%s", List.of("punch_last", "hit_second_last")),
        new AnvilSpec("morningstar_head", "c:ingots/%s", List.of("hit_second_last", "hit_last"))
    );

    private static final List<HeatingSpec> HEATING_SPECS = List.of(
        new HeatingSpec("longsword_blade", "tfc:double_ingot/%s"),
        new HeatingSpec("greatsword_blade", "tfc:double_sheet/%s"),
        new HeatingSpec("shortsword_blade", "tfc:ingot/%s"),
        new HeatingSpec("greataxe_head", "tfc:sheet/%s"),
        new HeatingSpec("greathammer_head", "tfc:double_sheet/%s"),
        new HeatingSpec("morningstar_head", "tfc:ingot/%s")
    );
    
    public TFCAnvilRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.lookupProvider = lookupProvider;
    }
    
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookupProvider.thenCompose(provider -> {
            var metalNames = MetalData.names().toList();
            var defsByType = WeaponRegistry.all()
                .collect(java.util.stream.Collectors.toMap(WeaponRegistry.ItemDef::type, def -> def));

            var allFutures = metalNames.stream()
                .flatMap(metalName -> Stream.concat(
                    generateComponentAnvilRecipes(metalName, output, defsByType),
                    generateHeatingRecipes(metalName, output, defsByType)
                ))
                .toArray(CompletableFuture[]::new);

            return CompletableFuture.allOf(allFutures);
        });
    }
    
    private Stream<CompletableFuture<?>> generateComponentAnvilRecipes(String metalName, CachedOutput output, Map<String, WeaponRegistry.ItemDef> defsByType) {
        String normalizedMetal = NameUtils.normalizeMetalName(metalName);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        
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
        
        ANVIL_SPECS.forEach(spec -> {
            var def = defsByType.get(spec.type());
            if (def == null) {
                return;
            }
            var item = def.item(metalName);
            var props = propsFor(metalName);
            futures.add(createAnvilRecipeWithTag(
                output,
                def.path(metalName),
                spec.ingredientFormat().formatted(normalizedMetal),
                BuiltInRegistries.ITEM.getKey(item).toString(),
                props.tier(),
                spec.rules()
            ));
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
    
    private Stream<CompletableFuture<?>> generateHeatingRecipes(String metalName, CachedOutput output, Map<String, WeaponRegistry.ItemDef> defsByType) {
        String normalizedMetal = NameUtils.normalizeMetalName(metalName);
        List<CompletableFuture<?>> futures = new ArrayList<>();

        HEATING_SPECS.forEach(spec -> {
            var def = defsByType.get(spec.type());
            if (def == null) {
                return;
            }
            var item = def.item(metalName);
            futures.add(
                createHeatingRecipe(output, def.path(metalName), item, spec.ingredientId().formatted(normalizedMetal))
            );
        });
        
        return futures.stream();
    }
    
    private CompletableFuture<?> createHeatingRecipe(CachedOutput output, String recipeName, net.minecraft.world.item.Item resultItem, String ingredientId) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "tfc:heating");
        recipe.addProperty("result", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(resultItem).toString());
        
        // Ingredient using ID string
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("ingredient", ingredientId);
        recipe.add("ingredient", ingredient);
        
        // Melting point pulled from FluidHeat data via TFC helper (handled in runtime)
        recipe.addProperty("temperature", 600);
        recipe.addProperty("duration", 600);
        
        return saveRecipe(output, recipe, "heating/" + recipeName);
    }
    
    private CompletableFuture<?> saveRecipe(CachedOutput output, JsonObject json, String recipePath) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, recipePath);
        Path path = this.output.getOutputFolder().resolve("data/" + id.getNamespace() + "/recipe/" + id.getPath() + ".json");
        return DataProvider.saveStable(output, json, path);
    }

    private MetalData.Props propsFor(String metalName) {
        return MetalData.dataProps(MetalData.fromName(metalName));
    }
    
    @Override
    public String getName() {
        return "TFC Anvil Recipes";
    }
}

