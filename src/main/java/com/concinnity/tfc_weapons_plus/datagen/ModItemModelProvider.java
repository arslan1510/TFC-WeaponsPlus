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
        // Using item/handheld for 3D appearance (like tools/weapons)
        withExistingParent("item/wood/grip", mcLoc("item/handheld"))
            .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/wood/grip"));
        
        // Generate models for metal variants of guard and pommel
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            String normalizedMetal = normalizeMetalName(metalName);
            
            // Guard variants - Model path must match item ID: item/metal/guard/{metal}
            // Using item/handheld for 3D appearance
            // TFC format: namespace:item/path/to/texture
            ModItems.getGuardForMetal(metalName).ifPresent(guard -> {
                withExistingParent("item/metal/guard/" + normalizedMetal, mcLoc("item/handheld"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/guard/" + normalizedMetal));
            });
            
            // Pommel variants - Model path must match item ID: item/metal/pommel/{metal}
            // Using item/handheld for 3D appearance
            // TFC format: namespace:item/path/to/texture
            ModItems.getPommelForMetal(metalName).ifPresent(pommel -> {
                withExistingParent("item/metal/pommel/" + normalizedMetal, mcLoc("item/handheld"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/pommel/" + normalizedMetal));
            });
            
            // Hilt models - Model path must match item ID: item/metal/hilt/{metal}
            // Using item/handheld for 3D appearance
            // Uses combined texture with pommel (top), grip (middle), guard (bottom)
            withExistingParent("item/metal/hilt/" + normalizedMetal, mcLoc("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/hilt/" + normalizedMetal));
            
            // Longsword blade models - Model path must match item ID: item/metal/longsword_blade/{metal}
            // Using item/handheld for 3D appearance
            ModItems.getLongswordBladeForMetal(metalName).ifPresent(blade -> {
                withExistingParent("item/metal/longsword_blade/" + normalizedMetal, mcLoc("item/handheld"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/longsword_blade/" + normalizedMetal));
            });
            
            // Greatsword blade models - Model path must match item ID: item/metal/greatsword_blade/{metal}
            // Using item/handheld for 3D appearance
            ModItems.getGreatswordBladeForMetal(metalName).ifPresent(blade -> {
                withExistingParent("item/metal/greatsword_blade/" + normalizedMetal, mcLoc("item/handheld"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/greatsword_blade/" + normalizedMetal));
            });
        });
        
        // Generate models for longswords (same metal for blade and hilt)
        // Use local template_longsword model for proper sizing when held
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            String normalizedMetal = normalizeMetalName(metalName);
            String modelPath = "item/metal/longsword/" + normalizedMetal;
            
            ModItems.getLongswordForMetal(metalName).ifPresent(longsword -> {
                withExistingParent(modelPath, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/template_longsword"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/longsword/" + normalizedMetal));
            });
        });
        
        // Generate models for greatswords (same metal for blade and hilt)
        // Use local template_greatsword model for proper sizing when held
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            String normalizedMetal = normalizeMetalName(metalName);
            String modelPath = "item/metal/greatsword/" + normalizedMetal;
            
            ModItems.getGreatswordForMetal(metalName).ifPresent(greatsword -> {
                withExistingParent(modelPath, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/template_greatsword"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/greatsword/" + normalizedMetal));
            });
        });
    }
    
    private String normalizeMetalName(String metalName) {
        return metalName.toLowerCase()
            .replace(" ", "_")
            .replace("-", "_");
    }
}

