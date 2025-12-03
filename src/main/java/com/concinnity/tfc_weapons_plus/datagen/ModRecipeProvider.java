package com.concinnity.tfc_weapons_plus.datagen;

import com.concinnity.tfc_weapons_plus.integration.MetalHelper;
import com.concinnity.tfc_weapons_plus.item.ModItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Generates recipes for the mod
 */
public final class ModRecipeProvider extends RecipeProvider {
    
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }
    
    @Override
    protected void buildRecipes(RecipeOutput output) {
        generateHiltAssemblyRecipes(output);
        generateSwordAssemblyRecipes(output);
    }
    
    /**
     * Create hilt assembly recipe: hilt = grip + guard + pommel
     */
    private void createHiltAssemblyRecipe(RecipeOutput output, String metalName) {
        ModItems.getGuardForMetal(metalName).ifPresent(guard -> {
            ModItems.getPommelForMetal(metalName).ifPresent(pommel -> {
                ModItems.getHiltForMetal(metalName).ifPresent(hilt -> {
                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, hilt)
                        .pattern(" P ")
                        .pattern(" G ")
                        .pattern(" R ")
                        .define('P', pommel)
                        .define('G', guard)
                        .define('R', ModItems.GRIP.get())
                    .unlockedBy("has_grip", has(ModItems.GRIP.get()))
                    .unlockedBy("has_guard", has(guard))
                    .unlockedBy("has_pommel", has(pommel))
                    .save(output, "metal/hilt/assembly_" + normalizeMetalName(metalName));
                });
            });
        });
    }
    
    private void generateHiltAssemblyRecipes(RecipeOutput output) {
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            createHiltAssemblyRecipe(output, metalName);
        });
    }
    
    /**
     * Create sword assembly recipe: sword = hilt + blade (crafting table)
     */
    private void createSwordAssemblyRecipe(RecipeOutput output, String metalName) {
        ModItems.getHiltForMetal(metalName).ifPresent(hilt -> {
            String normalizedMetal = normalizeMetalName(metalName);
            String bladeId = "tfc:metal/sword_blade/" + normalizedMetal;
            String swordId = "tfc:metal/sword/" + normalizedMetal;
            
            // Use ResourceLocation to create ingredients from string IDs
            net.minecraft.resources.ResourceLocation bladeLoc = net.minecraft.resources.ResourceLocation.parse(bladeId);
            net.minecraft.resources.ResourceLocation swordLoc = net.minecraft.resources.ResourceLocation.parse(swordId);
            
            // Check if items exist in registry (for data generation)
            if (net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(bladeLoc) && 
                net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(swordLoc)) {
                net.minecraft.world.item.Item blade = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(bladeLoc);
                net.minecraft.world.item.Item sword = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(swordLoc);
                
                ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, sword)
                    .pattern("B")
                    .pattern("H")
                    .define('B', blade)
                    .define('H', hilt)
                    .unlockedBy("has_hilt", has(hilt))
                    .unlockedBy("has_blade", has(blade))
                    .save(output, "metal/sword/assembly_" + normalizeMetalName(metalName));
            }
        });
    }
    
    private String normalizeMetalName(String metalName) {
        return metalName.toLowerCase()
            .replace(" ", "_")
            .replace("-", "_");
    }
    
    private void generateSwordAssemblyRecipes(RecipeOutput output) {
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            createSwordAssemblyRecipe(output, metalName);
        });
    }
    
}

