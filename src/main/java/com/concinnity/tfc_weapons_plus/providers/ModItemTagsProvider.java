package com.concinnity.tfc_weapons_plus.providers;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.util.MetalData;
import com.concinnity.tfc_weapons_plus.util.WeaponRegistry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public final class ModItemTagsProvider implements DataProvider {
    private final PackOutput.PathProvider tagPathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.tagPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/items");
        this.lookupProvider = lookupProvider;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookupProvider.thenCompose(provider -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            var allWeapons = collectWeapons(weaponDef -> true);
            var swords = collectWeapons(def -> WeaponRegistry.SWORDS.contains(def.type()));
            var axes = collectWeapons(def -> WeaponRegistry.AXES.contains(def.type()));
            var slashingWeapons = collectWeapons(def -> WeaponRegistry.SLASHING.contains(def.type()));
            var crushingWeapons = collectWeapons(def -> WeaponRegistry.CRUSHING.contains(def.type()));
            var piercingWeapons = collectWeapons(def -> WeaponRegistry.PIERCING.contains(def.type()));

            if (!allWeapons.isEmpty()) {
                futures.add(createTagFileFromLocations(output, ResourceLocation.fromNamespaceAndPath("c", "tools"), allWeapons, tagPathProvider));
            }
            if (!swords.isEmpty()) {
                futures.add(createTagFileFromLocations(output, ResourceLocation.fromNamespaceAndPath("minecraft", "swords"), swords, tagPathProvider));
            }
            if (!axes.isEmpty()) {
                futures.add(createTagFileFromLocations(output, ResourceLocation.fromNamespaceAndPath("minecraft", "axes"), axes, tagPathProvider));
            }

            // TFC damage-type tags
            if (!slashingWeapons.isEmpty()) {
                futures.add(createTagFileFromLocations(output, ResourceLocation.fromNamespaceAndPath("tfc", "deals_slashing_damage"), slashingWeapons, tagPathProvider));
            }
            if (!crushingWeapons.isEmpty()) {
                futures.add(createTagFileFromLocations(output, ResourceLocation.fromNamespaceAndPath("tfc", "deals_crushing_damage"), crushingWeapons, tagPathProvider));
            }
            if (!piercingWeapons.isEmpty()) {
                futures.add(createTagFileFromLocations(output, ResourceLocation.fromNamespaceAndPath("tfc", "deals_piercing_damage"), piercingWeapons, tagPathProvider));
            }
            
            // TFC tool rack integration
            if (!allWeapons.isEmpty()) {
                futures.add(createTagFileFromLocations(output, ResourceLocation.fromNamespaceAndPath("tfc", "usable_on_tool_rack"), allWeapons, tagPathProvider));
            }

            MetalData.names().forEach(metalName -> {
                String normalizedMetal = com.concinnity.tfc_weapons_plus.util.NameUtils.normalizeMetalName(metalName);
                var metalWeapons = collectWeaponsForMetal(metalName);
                if (!metalWeapons.isEmpty()) {
                    futures.add(createTagFileFromLocations(output,
                        ResourceLocation.fromNamespaceAndPath("tfc", "tools/" + normalizedMetal),
                        metalWeapons, tagPathProvider));
                }
            });

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }

    private List<ResourceLocation> collectWeapons(Predicate<WeaponRegistry.ItemDef> predicate) {
        return MetalData.names()
            .flatMap(metalName -> WeaponRegistry.weapons()
                .filter(predicate)
                .map(def -> ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, def.path(metalName)))
            )
            .toList();
    }

    private List<ResourceLocation> collectWeaponsForMetal(String metalName) {
        return WeaponRegistry.weapons()
            .map(def -> ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, def.path(metalName)))
            .toList();
    }

    private CompletableFuture<?> createTagFileFromLocations(CachedOutput output, ResourceLocation tagId, List<ResourceLocation> itemLocations, PackOutput.PathProvider pathProvider) {
        JsonObject tag = new JsonObject();
        tag.addProperty("replace", false);

        JsonArray values = new JsonArray();
        itemLocations.forEach(location -> values.add(location.toString()));
        tag.add("values", values);

        var path = pathProvider.json(tagId);
        return DataProvider.saveStable(output, tag, path);
    }

    @Override
    public String getName() {
        return "Item Tags (Common & Minecraft)";
    }
}

