package com.concinnity.tfcweaponsplus.datagen;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import com.concinnity.tfcweaponsplus.models.IItem;
import com.concinnity.tfcweaponsplus.models.WeaponType;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import com.concinnity.tfcweaponsplus.utils.TFCUtils;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils.ItemVariant;
import com.google.gson.JsonObject;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Generates TFC heating recipes for melting weapons back into liquid metal.
 */
public class ModHeatingRecipeProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public ModHeatingRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.lookupProvider = lookupProvider;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return lookupProvider.thenCompose(provider -> {
            List<CompletableFuture<?>> allFutures = new ArrayList<>();

            metalStream().forEach(metal -> {
                Arrays.stream(WeaponType.values()).forEach(weaponType -> {
                    allFutures.add(createMeltingRecipe(cache, weaponType, metal));
                });
            });

            return CompletableFuture.allOf(allFutures.toArray(CompletableFuture[]::new));
        });
    }

    private CompletableFuture<?> createMeltingRecipe(CachedOutput cache, IItem item, Metal metal) {
        ItemVariant itemVariant = new ItemVariant(item, Optional.of(metal));

        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "tfc:heating");

        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", TFCWeaponsPlus.MOD_ID + ":" + itemVariant.getRegistryPath());
        recipe.add("ingredient", ingredient);

        JsonObject resultFluid = new JsonObject();
        resultFluid.addProperty("id", "tfc:metal/" + metal.getSerializedName());
        resultFluid.addProperty("amount", itemVariant.item().getFluidAmount());
        recipe.add("result_fluid", resultFluid);
        recipe.addProperty("temperature", TFCUtils.getMeltTemperature(metal));
        recipe.addProperty("use_durability", true);

        return saveRecipe(cache, recipe, "heating/" + itemVariant.getRegistryPath());
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
        return "TFC Weapons Plus Heating Recipes";
    }
}
