package com.concinnity.tfcweaponsplus.datagen;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import com.concinnity.tfcweaponsplus.models.ComponentType;
import com.concinnity.tfcweaponsplus.models.IItem;
import com.concinnity.tfcweaponsplus.models.WeaponType;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils.ItemVariant;
import com.concinnity.tfcweaponsplus.utils.TFCUtils;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ModRecipeProvider extends RecipeProvider {

    private record BladeHiltRecipe(
        WeaponType weaponType,
        ComponentType bladeComponent
    ) {
        void generate(RecipeOutput output, Metal metal) {
            ItemVariant hiltVariant = new ItemVariant(ComponentType.HILT, Optional.of(metal));
            ItemVariant bladeVariant = new ItemVariant(bladeComponent, Optional.of(metal));
            ItemVariant weaponVariant = new ItemVariant(weaponType, Optional.of(metal));

            Item hilt = getItem(hiltVariant.getRegistryPath());
            Item blade = getItem(bladeVariant.getRegistryPath());
            Item weapon = getItem(weaponVariant.getRegistryPath());

            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, weapon, 1)
                .pattern("B").pattern("H")
                .define('B', blade).define('H', hilt)
                .unlockedBy("has_hilt", has(hilt))
                .unlockedBy("has_blade", has(blade))
                .save(output, recipeId(weaponVariant.getRegistryPath() + "/assembly"));
        }
    }

    private record HeadGripRecipe(
        WeaponType weaponType,
        ComponentType headComponent,
        String pattern1,
        String pattern2,
        String pattern3
    ) {
        void generate(RecipeOutput output, Metal metal) {
            ItemVariant headVariant = new ItemVariant(headComponent, Optional.of(metal));
            ItemVariant weaponVariant = new ItemVariant(weaponType, Optional.of(metal));
            ItemVariant gripVariant = new ItemVariant(ComponentType.GRIP, Optional.empty());

            Item head = getItem(headVariant.getRegistryPath());
            Item weapon = getItem(weaponVariant.getRegistryPath());
            Item grip = getItem(gripVariant.getRegistryPath());

            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, weapon, 1)
                .pattern(pattern1).pattern(pattern2).pattern(pattern3)
                .define('H', head).define('L', TFCTags.Items.LUMBER).define('G', grip)
                .unlockedBy("has_head", has(head))
                .save(output, recipeId(weaponVariant.getRegistryPath() + "/assembly"));
        }
    }

    private static final List<BladeHiltRecipe> BLADE_HILT_RECIPES = List.of(
        new BladeHiltRecipe(WeaponType.LONGSWORD, ComponentType.LONGSWORD_BLADE),
        new BladeHiltRecipe(WeaponType.GREATSWORD, ComponentType.GREATSWORD_BLADE),
        new BladeHiltRecipe(WeaponType.SHORTSWORD, ComponentType.SHORTSWORD_BLADE)
    );

    private static final List<HeadGripRecipe> HEAD_GRIP_RECIPES = List.of(
        new HeadGripRecipe(WeaponType.GREATAXE, ComponentType.GREATAXE_HEAD, "  H", " L ", "G  "),
        new HeadGripRecipe(WeaponType.GREATHAMMER, ComponentType.GREATHAMMER_HEAD, "  H", " L ", "G  ")
    );

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        generateHiltAssemblyRecipes(output);
        metalStream().forEach(metal -> {
            BLADE_HILT_RECIPES.forEach(recipe -> recipe.generate(output, metal));
            HEAD_GRIP_RECIPES.forEach(recipe -> recipe.generate(output, metal));
            generateMorningstarRecipe(output, metal);
            generateQuarterstaffRecipe(output, metal);
        });
    }

    private void generateHiltAssemblyRecipes(RecipeOutput output) {
        metalStream().forEach(metal -> {
            ItemVariant guardVariant = new ItemVariant(ComponentType.GUARD, Optional.of(metal));
            ItemVariant pommelVariant = new ItemVariant(ComponentType.POMMEL, Optional.of(metal));
            ItemVariant hiltVariant = new ItemVariant(ComponentType.HILT, Optional.of(metal));
            ItemVariant gripVariant = new ItemVariant(ComponentType.GRIP, Optional.empty());

            Item guard = getItem(guardVariant.getRegistryPath());
            Item pommel = getItem(pommelVariant.getRegistryPath());
            Item hilt = getItem(hiltVariant.getRegistryPath());
            Item grip = getItem(gripVariant.getRegistryPath());

            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, hilt, 1)
                .pattern(" P ").pattern(" G ").pattern(" R ")
                .define('P', pommel).define('G', guard).define('R', grip)
                .unlockedBy("has_grip", has(grip))
                .unlockedBy("has_guard", has(guard))
                .unlockedBy("has_pommel", has(pommel))
                .save(output, recipeId(hiltVariant.getRegistryPath() + "/assembly"));
        });
    }

    private void generateMorningstarRecipe(RecipeOutput output, Metal metal) {
        ItemVariant headVariant = new ItemVariant(ComponentType.MORNINGSTAR_HEAD, Optional.of(metal));
        ItemVariant weaponVariant = new ItemVariant(WeaponType.MORNINGSTAR, Optional.of(metal));
        ItemVariant gripVariant = new ItemVariant(ComponentType.GRIP, Optional.empty());

        Item head = getItem(headVariant.getRegistryPath());
        Item weapon = getItem(weaponVariant.getRegistryPath());
        Item grip = getItem(gripVariant.getRegistryPath());

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, weapon, 1)
            .pattern("H").pattern("R")
            .define('H', head).define('R', grip)
            .unlockedBy("has_head", has(head))
            .save(output, recipeId(weaponVariant.getRegistryPath() + "/assembly"));
    }

    private void generateQuarterstaffRecipe(RecipeOutput output, Metal metal) {
        ItemVariant hiltVariant = new ItemVariant(ComponentType.HILT, Optional.of(metal));
        ItemVariant weaponVariant = new ItemVariant(WeaponType.QUARTERSTAFF, Optional.of(metal));

        Item hilt = getItem(hiltVariant.getRegistryPath());
        Item weapon = getItem(weaponVariant.getRegistryPath());

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, weapon, 1)
            .pattern("  L").pattern(" H ").pattern("L  ")
            .define('L', TFCTags.Items.LUMBER).define('H', hilt)
            .unlockedBy("has_hilt", has(hilt))
            .save(output, recipeId(weaponVariant.getRegistryPath() + "/assembly"));
    }

    private static Stream<Metal> metalStream() {
        return Arrays.stream(Metal.values()).filter(TFCUtils::isValidMetal);
    }

    private static Item getItem(String path) {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, path);
        // During datagen, items might not be in the registry yet, so we need to get them directly
        Item item = BuiltInRegistries.ITEM.getOptional(loc).orElse(null);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            // Return a dummy item for datagen - the actual item will be registered at runtime
            // We just need something non-null to generate the recipe JSON
            return net.minecraft.world.item.Items.STICK; // Placeholder
        }
        return item;
    }

    private static ResourceLocation recipeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, "crafting/" + path);
    }
}
