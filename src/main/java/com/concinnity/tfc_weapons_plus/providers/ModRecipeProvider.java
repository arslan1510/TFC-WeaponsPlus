package com.concinnity.tfc_weapons_plus.providers;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.item.ModItems;
import com.concinnity.tfc_weapons_plus.util.NameUtils;
import com.concinnity.tfc_weapons_plus.util.MetalData;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Generates recipes for the mod using data-driven approach.
 * Optimized to remove Optional overhead - items are guaranteed to exist after registration.
 */
public final class ModRecipeProvider extends RecipeProvider {

    private record BladeHiltRecipe(
        String weaponType,
        Function<String, Item> bladeGetter,
        Function<String, Item> weaponGetter
    ) {
        void generate(RecipeOutput output, String metalName) {
            Item hilt = ModItems.getHiltForMetal(metalName);
            Item blade = bladeGetter.apply(metalName);
            Item weapon = weaponGetter.apply(metalName);
            String normalized = NameUtils.normalizeMetalName(metalName);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, weapon, 1)
                .pattern("B").pattern("H")
                .define('B', blade).define('H', hilt)
                .unlockedBy("has_hilt", has(hilt))
                .unlockedBy("has_blade", has(blade))
                .save(output, recipeId("metal/" + weaponType + "/assembly_" + normalized));
        }
    }

    private record HeadGripRecipe(
        String weaponType,
        Function<String, Item> headGetter,
        Function<String, Item> weaponGetter
    ) {
        void generate(RecipeOutput output, String metalName) {
            Item head = headGetter.apply(metalName);
            Item weapon = weaponGetter.apply(metalName);
            String normalized = NameUtils.normalizeMetalName(metalName);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, weapon, 1)
                .pattern("  H").pattern(" G ").pattern("G  ")
                .define('H', head).define('G', ModItems.GRIP.get())
                .unlockedBy("has_head", has(head))
                .save(output, recipeId("metal/" + weaponType + "/assembly_" + normalized));
        }
    }

    private static final List<BladeHiltRecipe> BLADE_HILT_RECIPES = List.of(
        new BladeHiltRecipe("longsword", ModItems::getLongswordBladeForMetal, ModItems::getLongswordForMetal),
        new BladeHiltRecipe("greatsword", ModItems::getGreatswordBladeForMetal, ModItems::getGreatswordForMetal),
        new BladeHiltRecipe("shortsword", ModItems::getShortswordBladeForMetal, ModItems::getShortswordForMetal)
    );

    private static final List<HeadGripRecipe> HEAD_GRIP_RECIPES = List.of(
        new HeadGripRecipe("greataxe", ModItems::getGreatAxeHeadForMetal, ModItems::getGreatAxeForMetal),
        new HeadGripRecipe("greathammer", ModItems::getGreatHammerHeadForMetal, ModItems::getGreatHammerForMetal)
    );

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        generateHiltAssemblyRecipes(output);
        generateSwordAssemblyRecipes(output);
        MetalData.names().forEach(metalName -> {
            BLADE_HILT_RECIPES.forEach(recipe -> recipe.generate(output, metalName));
            HEAD_GRIP_RECIPES.forEach(recipe -> recipe.generate(output, metalName));
            generateMorningstarRecipe(output, metalName);
            generateQuarterstaffRecipe(output, metalName);
        });
    }

    private void generateHiltAssemblyRecipes(RecipeOutput output) {
        MetalData.names().forEach(metalName -> {
            Item guard = ModItems.getGuardForMetal(metalName);
            Item pommel = ModItems.getPommelForMetal(metalName);
            Item hilt = ModItems.getHiltForMetal(metalName);
            String normalized = NameUtils.normalizeMetalName(metalName);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, hilt, 1)
                .pattern(" P ").pattern(" G ").pattern(" R ")
                .define('P', pommel).define('G', guard).define('R', ModItems.GRIP.get())
                .unlockedBy("has_grip", has(ModItems.GRIP.get()))
                .unlockedBy("has_guard", has(guard))
                .unlockedBy("has_pommel", has(pommel))
                .save(output, recipeId("metal/hilt/assembly_" + normalized));
        });
    }

    private void generateSwordAssemblyRecipes(RecipeOutput output) {
        MetalData.names().forEach(metalName -> {
            Item hilt = ModItems.getHiltForMetal(metalName);
            String normalized = NameUtils.normalizeMetalName(metalName);
            String bladeId = "tfc:metal/sword_blade/" + normalized;
            String swordId = "tfc:metal/sword/" + normalized;
            var bladeLoc = ResourceLocation.parse(bladeId);
            var swordLoc = ResourceLocation.parse(swordId);

            if (net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(bladeLoc) &&
                net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(swordLoc)) {
                var blade = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(bladeLoc);
                var sword = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(swordLoc);
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, sword, 1)
                    .pattern("B").pattern("H")
                    .define('B', blade).define('H', hilt)
                    .unlockedBy("has_hilt", has(hilt))
                    .unlockedBy("has_blade", has(blade))
                    .save(output, recipeId("metal/sword/assembly_" + normalized));
            }
        });
    }

    private void generateMorningstarRecipe(RecipeOutput output, String metalName) {
        Item head = ModItems.getMorningstarHeadForMetal(metalName);
        Item weapon = ModItems.getMorningstarForMetal(metalName);
        String normalized = NameUtils.normalizeMetalName(metalName);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, weapon, 1)
            .pattern("H").pattern("H").pattern("R")
            .define('H', head).define('R', ModItems.GRIP.get())
            .unlockedBy("has_head", has(head))
            .save(output, recipeId("metal/morningstar/assembly_" + normalized));
    }

    private void generateQuarterstaffRecipe(RecipeOutput output, String metalName) {
        Item hilt = ModItems.getHiltForMetal(metalName);
        Item weapon = ModItems.getQuarterstaffForMetal(metalName);
        String normalized = NameUtils.normalizeMetalName(metalName);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, weapon, 1)
            .pattern(" L").pattern(" H").pattern("L ")
            .define('L', TFCTags.Items.LUMBER).define('H', hilt)
            .unlockedBy("has_hilt", has(hilt))
            .save(output, recipeId("metal/quarterstaff/assembly_" + normalized));
    }

    private static ResourceLocation recipeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, "crafting/" + path);
    }
}
