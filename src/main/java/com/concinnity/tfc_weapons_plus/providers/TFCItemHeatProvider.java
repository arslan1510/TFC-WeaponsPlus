package com.concinnity.tfc_weapons_plus.providers;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.util.WeaponRegistry;
import com.concinnity.tfc_weapons_plus.util.data.FluidHeatData;
import com.concinnity.tfc_weapons_plus.util.data.ItemHeatHelper;
import com.google.gson.JsonObject;
import net.dries007.tfc.util.Metal;
import net.dries007.tfc.util.data.FluidHeat;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Optimized TFC item heat provider.
 * Removed Optional overhead for faster generation.
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
            var futures = java.util.Arrays.stream(Metal.values())
                .filter(metal -> metal.tier() > 0 && metal.allParts())
                .map(metal -> heatFuturesForMetal(output, metal))
                .toArray(CompletableFuture[]::new);

            return CompletableFuture.allOf(futures);
        });
    }

    private CompletableFuture<?> heatFuturesForMetal(CachedOutput output, Metal metal) {
        final String metalName = metal.getSerializedName();
        final FluidHeat fluidHeat = FluidHeatData.getFluidHeat(metal);

        var itemFutures = WeaponRegistry.metalItems()
            .map(def -> {
                // Direct item access, no Optional
                String itemPath = def.path(metalName);
                var heatDef = ItemHeatHelper.heat(ingredient(itemPath), fluidHeat, def.heatUnits());
                return addItemHeat(output, itemPath, heatDef);
            })
            .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(itemFutures);
    }

    private CompletableFuture<?> addItemHeat(CachedOutput cachedOutput, String itemPath, net.dries007.tfc.common.component.heat.HeatDefinition definition) {
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, itemPath);

        JsonObject json = new JsonObject();
        JsonObject ingredientJson = new JsonObject();
        ingredientJson.addProperty("item", itemId.toString());
        json.add("ingredient", ingredientJson);
        json.addProperty("heat_capacity", definition.heatCapacity());
        json.addProperty("forging_temperature", definition.forgingTemperature());
        json.addProperty("welding_temperature", definition.weldingTemperature());

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("tfc", "item_heat/" + itemPath);
        Path path = this.output.getOutputFolder().resolve("data/" + id.getNamespace() + "/" + id.getPath() + ".json");
        return DataProvider.saveStable(cachedOutput, json, path);
    }

    private static Ingredient ingredient(String itemPath) {
        return Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, itemPath)));
    }

    @Override
    public String getName() {
        return "TFC Item Heat Data";
    }
}
