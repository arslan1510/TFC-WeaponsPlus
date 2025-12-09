package com.concinnity.tfc_weapons_plus.providers;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.item.ModItems;
import com.concinnity.tfc_weapons_plus.util.WeaponRegistry;
import com.google.gson.JsonObject;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public final class TFCItemSizeProvider implements DataProvider {
    private final PackOutput output;
    private final List<SizeEntry> entries = new ArrayList<>();
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public TFCItemSizeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.lookupProvider = lookupProvider;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return lookupProvider.thenCompose(provider -> {
            buildEntries();
            var futures = entries.stream()
                .map(entry -> saveSize(cachedOutput, entry))
                .toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(futures);
        });
    }

    private void buildEntries() {
        entries.clear();

        entries.add(new SizeEntry("wood/grip", ModItems.GRIP.get(), Size.SMALL, Weight.LIGHT));

        metals().forEach(metal -> {
            String metalName = metal.getSerializedName();
            WeaponRegistry.metalItems().forEach(def -> {
                var item = def.item(metalName);
                entries.add(new SizeEntry(def.path(metalName), item, def.size(), def.weight()));
            });
        });
    }

    private CompletableFuture<?> saveSize(CachedOutput cachedOutput, SizeEntry entry) {
        JsonObject root = new JsonObject();
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, entry.id()).toString());
        root.add("ingredient", ingredient);
        root.addProperty("size", entry.size().getSerializedName());
        root.addProperty("weight", entry.weight().getSerializedName());

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("tfc", "item_size/" + entry.id());
        Path path = output.getOutputFolder().resolve("data/" + id.getNamespace() + "/" + id.getPath() + ".json");
        return DataProvider.saveStable(cachedOutput, root, path);
    }

    private static java.util.stream.Stream<Metal> metals() {
        return Arrays.stream(Metal.values()).filter(m -> m.tier() > 0 && m.allParts());
    }

    @Override
    public String getName() {
        return "TFC Item Size Data";
    }

    private record SizeEntry(String id, net.minecraft.world.item.Item item, Size size, Weight weight) {}
}

