
package com.concinnity.tfcweaponsplus.datagen;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ModItemSizeProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public ModItemSizeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.lookupProvider = lookupProvider;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return lookupProvider.thenCompose(provider -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();

            generateItemSizes().forEach(entry -> {
                Path path = output.getOutputFolder()
                        .resolve("data/tfc/item_sizes/%s/%s.json".formatted(TFCWeaponsPlus.MOD_ID, entry.name()));
                futures.add(DataProvider.saveStable(cache, entry.json(), path));
            });

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    private Stream<ItemSizeEntry> generateItemSizes() {
        return ResourceUtils.generateItemVariants().map(this::createEntry);
    }

    private ItemSizeEntry createEntry(ResourceUtils.ItemVariant variant) {
        String name = variant.metal()
                .map(m -> "%s/%s/%s".formatted(
                        variant.item().getCategory().getSerializedName(),
                        m.getSerializedName(),
                        variant.item().getSerializedName()))
                .orElseGet(() -> "%s/%s".formatted(
                        variant.item().getCategory().getSerializedName(),
                        variant.item().getSerializedName()));

        JsonObject json = new JsonObject();
        json.addProperty("size", variant.item().getSize().name().toLowerCase());
        json.addProperty("weight", variant.item().getWeight().name().toLowerCase());

        return new ItemSizeEntry(name, json);
    }

    @Override
    public @NotNull String getName() {
        return "TFC Weapons Plus Item Sizes";
    }

    private record ItemSizeEntry(String name, JsonObject json) {}
}