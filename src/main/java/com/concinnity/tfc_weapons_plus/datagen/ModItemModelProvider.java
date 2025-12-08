package com.concinnity.tfc_weapons_plus.datagen;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.integration.MetalHelper;
import com.concinnity.tfc_weapons_plus.item.ModItems;
import com.concinnity.tfc_weapons_plus.util.NameUtils;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Generates item models using functional programming patterns
 * Includes metal variants for guard and pommel
 * All components use custom 3D parent models for proper appearance
 */
public final class ModItemModelProvider extends ItemModelProvider {
    
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TFCWeaponsPlus.MODID, existingFileHelper);
    }
    
    @Override
    protected void registerModels() {
        // Generate models for basic components
        // Grip model - uses custom 3D parent model
        // Model path must match item ID: item/wood/grip
        withExistingParent("item/wood/grip", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/grip"))
            .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/wood/grip"));
        
        // Generate models for metal variants of guard and pommel
        // Each variant uses its own per-metal texture file
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            String normalizedMetal = NameUtils.normalizeMetalName(metalName);

            // Guard variants - uses custom 3D parent model with per-metal texture
            // Model path must match item ID: item/metal/guard/{metal}
            ModItems.getGuardForMetal(metalName).ifPresent(guard -> {
                withExistingParent("item/metal/guard/" + normalizedMetal, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/guard"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/guard/" + normalizedMetal));
            });

            // Pommel variants - uses custom 3D parent model with per-metal texture
            // Model path must match item ID: item/metal/pommel/{metal}
            ModItems.getPommelForMetal(metalName).ifPresent(pommel -> {
                withExistingParent("item/metal/pommel/" + normalizedMetal, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/pommel"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/pommel/" + normalizedMetal));
            });
            
            // Hilt models - uses custom 3D parent model
            // Model path must match item ID: item/metal/hilt/{metal}
            // Texture is generated at build-time by combining guard + grip + pommel
            withExistingParent("item/metal/hilt/" + normalizedMetal, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/hilt"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/hilt/" + normalizedMetal));
            
            // Longsword blade models - use per-metal textures
            // Model path must match item ID: item/metal/longsword_blade/{metal}
            ModItems.getLongswordBladeForMetal(metalName).ifPresent(blade -> {
                withExistingParent("item/metal/longsword_blade/" + normalizedMetal, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/longsword_blade"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/longsword_blade/" + normalizedMetal));
            });

            // Greatsword blade models - use per-metal textures
            // Model path must match item ID: item/metal/greatsword_blade/{metal}
            ModItems.getGreatswordBladeForMetal(metalName).ifPresent(blade -> {
                withExistingParent("item/metal/greatsword_blade/" + normalizedMetal, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/greatsword_blade"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/greatsword_blade/" + normalizedMetal));
            });

            // Shortsword blade models - use per-metal textures
            // Model path must match item ID: item/metal/shortsword_blade/{metal}
            ModItems.getShortswordBladeForMetal(metalName).ifPresent(blade -> {
                withExistingParent("item/metal/shortsword_blade/" + normalizedMetal, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/shortsword_blade"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/shortsword_blade/" + normalizedMetal));
            });

            // Greataxe head models - uses custom 3D parent model with per-metal texture
            ModItems.getGreatAxeHeadForMetal(metalName).ifPresent(head -> {
                withExistingParent("item/metal/greataxe_head/" + normalizedMetal, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/greataxe_head"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/greataxe_head/" + normalizedMetal));
            });
            
            // Greathammer head models - uses custom 3D parent model with per-metal texture
            ModItems.getGreatHammerHeadForMetal(metalName).ifPresent(head -> {
                withExistingParent("item/metal/greathammer_head/" + normalizedMetal, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/greathammer_head"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/greathammer_head/" + normalizedMetal));
            });
        });
        
        // Generate models for longswords (same metal for blade and hilt)
        // Uses custom 3D parent model for proper sizing when held
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            String normalizedMetal = NameUtils.normalizeMetalName(metalName);
            String modelPath = "item/metal/longsword/" + normalizedMetal;
            
            ModItems.getLongswordForMetal(metalName).ifPresent(longsword -> {
                withExistingParent(modelPath, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/longsword"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/longsword/" + normalizedMetal));
            });
        });
        
        // Generate models for greatswords (same metal for blade and hilt)
        // Uses custom 3D parent model for proper sizing when held
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            String normalizedMetal = NameUtils.normalizeMetalName(metalName);
            String modelPath = "item/metal/greatsword/" + normalizedMetal;
            
            ModItems.getGreatswordForMetal(metalName).ifPresent(greatsword -> {
                withExistingParent(modelPath, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/greatsword"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/greatsword/" + normalizedMetal));
            });
        });
        
        // Generate models for shortswords (same metal for blade and hilt)
        // Uses custom 3D parent model for proper sizing when held
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            String normalizedMetal = NameUtils.normalizeMetalName(metalName);
            String modelPath = "item/metal/shortsword/" + normalizedMetal;
            
            ModItems.getShortswordForMetal(metalName).ifPresent(shortsword -> {
                withExistingParent(modelPath, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/shortsword"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/shortsword/" + normalizedMetal));
            });
        });
        
        // Generate models for greataxes
        // Uses custom 3D parent model for proper sizing when held
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            String normalizedMetal = NameUtils.normalizeMetalName(metalName);
            String modelPath = "item/metal/greataxe/" + normalizedMetal;

            ModItems.getGreatAxeForMetal(metalName).ifPresent(greataxe -> {
                withExistingParent(modelPath, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/greataxe"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/greataxe/" + normalizedMetal));
            });
        });
        
        // Generate models for greathammers
        // Uses custom 3D parent model for proper sizing when held
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            String normalizedMetal = NameUtils.normalizeMetalName(metalName);
            String modelPath = "item/metal/greathammer/" + normalizedMetal;

            ModItems.getGreatHammerForMetal(metalName).ifPresent(greathammer -> {
                withExistingParent(modelPath, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/greathammer"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MODID, "item/metal/greathammer/" + normalizedMetal));
            });
        });
    }
}