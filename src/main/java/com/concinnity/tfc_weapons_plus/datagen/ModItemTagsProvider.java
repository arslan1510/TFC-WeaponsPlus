package com.concinnity.tfc_weapons_plus.datagen;

import com.concinnity.tfc_weapons_plus.integration.MetalHelper;
import com.concinnity.tfc_weapons_plus.item.ModItems;
import com.concinnity.tfc_weapons_plus.util.NameUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generates item tags for metal variants
 * Creates tags in both common (c:) and TFC (tfc:) namespaces
 */
public final class ModItemTagsProvider implements DataProvider {
    private final PackOutput.PathProvider commonTagPathProvider;
    private final PackOutput.PathProvider tfcTagPathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.commonTagPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/items");
        this.tfcTagPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/items");
        this.lookupProvider = lookupProvider;
    }
    
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookupProvider.thenCompose(provider -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            
            // Generate tags for each metal variant
            MetalHelper.getAllMetalNames().forEach(metalName -> {
                String normalizedMetal = NameUtils.normalizeMetalName(metalName);
                
                // Common namespace tags for cross-mod compatibility
                ModItems.getGuardForMetal(metalName).ifPresent(guard -> {
                    ResourceLocation commonTagId = ResourceLocation.fromNamespaceAndPath("c", "guard/" + metalName);
                    futures.add(createTagFile(output, commonTagId, guard, commonTagPathProvider));
                    
                    // TFC metal tag so guard can be melted
                    ResourceLocation tfcMetalTagId = ResourceLocation.fromNamespaceAndPath("tfc", "metal/" + normalizedMetal);
                    futures.add(createTagFile(output, tfcMetalTagId, guard, tfcTagPathProvider));
                });
                
                ModItems.getPommelForMetal(metalName).ifPresent(pommel -> {
                    ResourceLocation commonTagId = ResourceLocation.fromNamespaceAndPath("c", "pommel/" + metalName);
                    futures.add(createTagFile(output, commonTagId, pommel, commonTagPathProvider));
                    
                    // TFC metal tag so pommel can be melted
                    ResourceLocation tfcMetalTagId = ResourceLocation.fromNamespaceAndPath("tfc", "metal/" + normalizedMetal);
                    futures.add(createTagFile(output, tfcMetalTagId, pommel, tfcTagPathProvider));
                });
                
                // Longsword blade tags
                ModItems.getLongswordBladeForMetal(metalName).ifPresent(blade -> {
                    // TFC metal tag so longsword blade can be melted
                    ResourceLocation tfcMetalTagId = ResourceLocation.fromNamespaceAndPath("tfc", "metal/" + normalizedMetal);
                    futures.add(createTagFile(output, tfcMetalTagId, blade, tfcTagPathProvider));
                });
                
                // Greatsword blade tags
                ModItems.getGreatswordBladeForMetal(metalName).ifPresent(blade -> {
                    // TFC metal tag so greatsword blade can be melted
                    ResourceLocation tfcMetalTagId = ResourceLocation.fromNamespaceAndPath("tfc", "metal/" + normalizedMetal);
                    futures.add(createTagFile(output, tfcMetalTagId, blade, tfcTagPathProvider));
                });
                
                // Waraxe head tags
                ModItems.getWarAxeHeadForMetal(metalName).ifPresent(head -> {
                    ResourceLocation tfcMetalTagId = ResourceLocation.fromNamespaceAndPath("tfc", "metal/" + normalizedMetal);
                    futures.add(createTagFile(output, tfcMetalTagId, head, tfcTagPathProvider));
                });
            });
            
            // General common tags
            var allGuards = MetalHelper.getAllMetalNames()
                .map(ModItems::getGuardForMetal)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();
            
            if (!allGuards.isEmpty()) {
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("c", "guards"), allGuards, commonTagPathProvider));
            }
            
            var allPommels = MetalHelper.getAllMetalNames()
                .map(ModItems::getPommelForMetal)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();
            
            if (!allPommels.isEmpty()) {
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("c", "pommels"), allPommels, commonTagPathProvider));
            }
            
            // Collect all longswords for general tags
            var allLongswords = MetalHelper.getAllMetalNames()
                .map(ModItems::getLongswordForMetal)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();
            
            if (!allLongswords.isEmpty()) {
                // Add all longswords to common tool tags (these will be merged with existing tags)
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("c", "tools"), allLongswords, commonTagPathProvider));
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("c", "tools/swords"), allLongswords, commonTagPathProvider));
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("c", "tools/melee_weapons"), allLongswords, commonTagPathProvider));
                
                // Add all longswords to minecraft:swords tag (TFC's deals_slashing_damage references this)
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("minecraft", "swords"), allLongswords, commonTagPathProvider));
                
                // Add all longswords to TFC slashing damage tag
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("tfc", "deals_slashing_damage"), allLongswords, tfcTagPathProvider));
            }
            
            // Collect all greatswords for general tags
            var allGreatswords = MetalHelper.getAllMetalNames()
                .map(ModItems::getGreatswordForMetal)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();
            
            if (!allGreatswords.isEmpty()) {
                // Add all greatswords to common tool tags (these will be merged with existing tags)
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("c", "tools"), allGreatswords, commonTagPathProvider));
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("c", "tools/swords"), allGreatswords, commonTagPathProvider));
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("c", "tools/melee_weapons"), allGreatswords, commonTagPathProvider));
                
                // Add all greatswords to minecraft:swords tag (TFC's deals_slashing_damage references this)
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("minecraft", "swords"), allGreatswords, commonTagPathProvider));
                
                // Add all greatswords to TFC slashing damage tag
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("tfc", "deals_slashing_damage"), allGreatswords, tfcTagPathProvider));
            }
            
            // Collect all waraxes for general tags
            var allWarAxes = MetalHelper.getAllMetalNames()
                .map(ModItems::getWarAxeForMetal)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();
            
            if (!allWarAxes.isEmpty()) {
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("c", "tools"), allWarAxes, commonTagPathProvider));
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("c", "tools/axes"), allWarAxes, commonTagPathProvider));
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("c", "tools/melee_weapons"), allWarAxes, commonTagPathProvider));
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("minecraft", "axes"), allWarAxes, commonTagPathProvider));
                futures.add(createTagFile(output, ResourceLocation.fromNamespaceAndPath("tfc", "deals_slashing_damage"), allWarAxes, tfcTagPathProvider));
            }
            
            // Add longswords to TFC metal-specific tool tags (like tfc:tools/red_steel)
            // This matches how TFC includes swords in their metal tool tags
            MetalHelper.getAllMetalNames().forEach(metalName -> {
                String normalizedMetal = NameUtils.normalizeMetalName(metalName);
                ModItems.getLongswordForMetal(metalName).ifPresent(longsword -> {
                    // TFC tool tag for this metal (e.g., tfc:tools/red_steel)
                    ResourceLocation tfcToolTagId = ResourceLocation.fromNamespaceAndPath("tfc", "tools/" + normalizedMetal);
                    futures.add(createTagFile(output, tfcToolTagId, longsword, tfcTagPathProvider));
                });
                
                // Add greatswords to TFC metal-specific tool tags
                ModItems.getGreatswordForMetal(metalName).ifPresent(greatsword -> {
                    // TFC tool tag for this metal (e.g., tfc:tools/red_steel)
                    ResourceLocation tfcToolTagId = ResourceLocation.fromNamespaceAndPath("tfc", "tools/" + normalizedMetal);
                    futures.add(createTagFile(output, tfcToolTagId, greatsword, tfcTagPathProvider));
                });
                
                // Add waraxes to TFC metal-specific tool tags
                ModItems.getWarAxeForMetal(metalName).ifPresent(waraxe -> {
                    ResourceLocation tfcToolTagId = ResourceLocation.fromNamespaceAndPath("tfc", "tools/" + normalizedMetal);
                    futures.add(createTagFile(output, tfcToolTagId, waraxe, tfcTagPathProvider));
                });
            });
            
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }
    
    private CompletableFuture<?> createTagFile(CachedOutput output, ResourceLocation tagId, net.minecraft.world.item.Item item, PackOutput.PathProvider pathProvider) {
        JsonObject tag = new JsonObject();
        tag.addProperty("replace", false);
        
        JsonArray values = new JsonArray();
        values.add(BuiltInRegistries.ITEM.getKey(item).toString());
        tag.add("values", values);
        
        var path = pathProvider.json(tagId);
        return DataProvider.saveStable(output, tag, path);
    }
    
    private CompletableFuture<?> createTagFile(CachedOutput output, ResourceLocation tagId, List<net.minecraft.world.item.Item> items, PackOutput.PathProvider pathProvider) {
        JsonObject tag = new JsonObject();
        tag.addProperty("replace", false);
        
        JsonArray values = new JsonArray();
        items.forEach(item -> values.add(BuiltInRegistries.ITEM.getKey(item).toString()));
        tag.add("values", values);
        
        var path = pathProvider.json(tagId);
        return DataProvider.saveStable(output, tag, path);
    }
    
    @Override
    public String getName() {
        return "Item Tags (Common & TFC)";
    }
}
