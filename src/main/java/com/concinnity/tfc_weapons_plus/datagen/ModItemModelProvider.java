package com.concinnity.tfc_weapons_plus.datagen;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.integration.MetalHelper;
import com.concinnity.tfc_weapons_plus.item.ModItems;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Generates item models using functional programming patterns
 * Includes metal variants for guard and pommel
 */
public final class ModItemModelProvider extends ItemModelProvider {
    
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TFCWeaponsPlus.MODID, existingFileHelper);
    }
    
    @Override
    protected void registerModels() {
        // Generate models for basic components
        // Grip model path must match item ID: item/wood/grip
        withExistingParent("item/wood/grip", mcLoc("item/generated"))
            .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/wood/grip"));
        
        // Generate models for metal variants of guard and pommel
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            String normalizedMetal = normalizeMetalName(metalName);
            
            // Guard variants - Model path must match item ID: item/metal/guard/{metal}
            // TFC format: namespace:item/path/to/texture
            ModItems.getGuardForMetal(metalName).ifPresent(guard -> {
                withExistingParent("item/metal/guard/" + normalizedMetal, mcLoc("item/generated"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/guard/" + normalizedMetal));
            });
            
            // Pommel variants - Model path must match item ID: item/metal/pommel/{metal}
            // TFC format: namespace:item/path/to/texture
            ModItems.getPommelForMetal(metalName).ifPresent(pommel -> {
                withExistingParent("item/metal/pommel/" + normalizedMetal, mcLoc("item/generated"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/pommel/" + normalizedMetal));
            });
            
            // Hilt models - Model path must match item ID: item/metal/hilt/{metal}
            // The combined texture has grip, guard, and pommel positioned correctly
            withExistingParent("item/metal/hilt/" + normalizedMetal, mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/hilt/" + normalizedMetal));
        });
    }
    
    private String normalizeMetalName(String metalName) {
        return metalName.toLowerCase()
            .replace(" ", "_")
            .replace("-", "_");
    }
}

