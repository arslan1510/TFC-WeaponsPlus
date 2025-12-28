package com.concinnity.tfcweaponsplus.datagen;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import com.concinnity.tfcweaponsplus.models.ComponentType;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils.ItemVariant;
import com.concinnity.tfcweaponsplus.utils.TFCUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Generates TFC anvil recipes for forging weapon components from ingots/sheets.
 *
 */
public class ModAnvilRecipeProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    private record AnvilSpec(ComponentType component, String ingredientTag, List<String> rules) {}

    private static final List<AnvilSpec> ANVIL_SPECS = List.of(
        new AnvilSpec(ComponentType.GUARD, "c:ingots/%s", List.of("hit_second_last", "hit_last")),
        new AnvilSpec(ComponentType.POMMEL, "c:ingots/%s", List.of("hit_second_last", "hit_last")),
        new AnvilSpec(ComponentType.HILT, "c:ingots/%s", List.of("hit_last", "draw_any", "shrink_not_last")),
        new AnvilSpec(ComponentType.SHORTSWORD_BLADE, "c:ingots/%s", List.of("hit_second_last", "hit_last")),
        new AnvilSpec(ComponentType.LONGSWORD_BLADE, "c:double_ingots/%s", List.of("bend_third_last", "bend_second_last", "hit_last")),
        new AnvilSpec(ComponentType.GREATSWORD_BLADE, "c:double_sheets/%s", List.of("bend_third_last", "bend_second_last", "hit_last")),
        new AnvilSpec(ComponentType.GREATAXE_HEAD, "c:sheets/%s", List.of("punch_last", "hit_second_last")),
        new AnvilSpec(ComponentType.GREATHAMMER_HEAD, "c:double_sheets/%s", List.of("punch_last", "hit_second_last")),
        new AnvilSpec(ComponentType.MORNINGSTAR_HEAD, "c:ingots/%s", List.of("hit_second_last", "hit_last"))
    );

    public ModAnvilRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.lookupProvider = lookupProvider;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return lookupProvider.thenCompose(provider -> {
            List<CompletableFuture<?>> allFutures = new ArrayList<>();

            metalStream().forEach(metal -> {
                String metalName = metal.getSerializedName();

                if (metalName.equals("copper")) {
                    allFutures.add(createGripAnvilRecipe(cache));
                }

                ANVIL_SPECS.forEach(spec -> allFutures.add(createAnvilRecipe(cache, spec, metal)));
            });

            return CompletableFuture.allOf(allFutures.toArray(CompletableFuture[]::new));
        });
    }

    private CompletableFuture<?> createGripAnvilRecipe(CachedOutput cache) {
        ItemVariant itemVariant = new ItemVariant(ComponentType.GRIP, Optional.empty());

        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "tfc:anvil");
        recipe.addProperty("apply_bonus", true);

        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("tag", "tfc:lumber");
        recipe.add("ingredient", ingredient);

        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", TFCWeaponsPlus.MOD_ID + ":" + itemVariant.getRegistryPath());
        recipe.add("result", result);

        recipe.addProperty("tier", 0);

        JsonArray rules = new JsonArray();
        rules.add("hit_last");
        rules.add("draw_any");
        rules.add("shrink_not_last");
        recipe.add("rules", rules);

        return saveRecipe(cache, recipe, "anvil/" + itemVariant.getRegistryPath());
    }

    private CompletableFuture<?> createAnvilRecipe(CachedOutput cache, AnvilSpec spec, Metal metal) {
        ItemVariant itemVariant = new ItemVariant(spec.component(), Optional.of(metal));
        String metalName = metal.getSerializedName();

        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "tfc:anvil");
        recipe.addProperty("apply_bonus", true);

        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("tag", String.format(spec.ingredientTag(), metalName));
        recipe.add("ingredient", ingredient);

        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", TFCWeaponsPlus.MOD_ID + ":" + itemVariant.getRegistryPath());
        recipe.add("result", result);

        recipe.addProperty("tier", metal.tier());

        JsonArray rules = new JsonArray();
        spec.rules().forEach(rules::add);
        recipe.add("rules", rules);

        return saveRecipe(cache, recipe, "anvil/" + itemVariant.getRegistryPath());
    }

    private CompletableFuture<?> saveRecipe(CachedOutput cache, JsonObject json, String recipePath) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, recipePath);
        Path path = this.output.getOutputFolder()
            .resolve("data/" + id.getNamespace() + "/recipe/" + id.getPath() + ".json");
        return DataProvider.saveStable(cache, json, path);
    }

    private static Stream<Metal> metalStream() {
        return Arrays.stream(Metal.values()).filter(TFCUtils::isValidMetal);
    }

    @Override
    public @NotNull String getName() {
        return "TFC Weapons Plus Anvil Recipes";
    }
}
