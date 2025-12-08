package com.concinnity.tfc_weapons_plus.datagen;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.integration.MetalHelper;
import com.concinnity.tfc_weapons_plus.item.ModItems;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Generates language files
 * Includes translations for metal variants
 */
public final class ModLanguageProvider extends LanguageProvider {
    
    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, TFCWeaponsPlus.MODID, locale);
    }
    
    @Override
    protected void addTranslations() {
        // Basic item names
        add(ModItems.GRIP.get(), "Grip");
        
        // Metal variant names for guard, pommel, and hilt
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                // Guard variants
                ModItems.getGuardForMetal(metalName).ifPresent(guard -> {
                    add(guard, props.name() + " Guard");
                });
                
                // Pommel variants
                ModItems.getPommelForMetal(metalName).ifPresent(pommel -> {
                    add(pommel, props.name() + " Pommel");
                });
                
                // Hilt variants (assembled from grip + guard + pommel)
                ModItems.getHiltForMetal(metalName).ifPresent(hilt -> {
                    add(hilt, props.name() + " Hilt");
                });
                
                // Longsword blade variants
                ModItems.getLongswordBladeForMetal(metalName).ifPresent(blade -> {
                    add(blade, props.name() + " Longsword Blade");
                });
                
                // Greatsword blade variants
                ModItems.getGreatswordBladeForMetal(metalName).ifPresent(blade -> {
                    add(blade, props.name() + " Greatsword Blade");
                });
                
                // Shortsword blade variants
                ModItems.getShortswordBladeForMetal(metalName).ifPresent(blade -> {
                    add(blade, props.name() + " Shortsword Blade");
                });
                
                // Greataxe head variants
                ModItems.getGreatAxeHeadForMetal(metalName).ifPresent(head -> {
                    add(head, props.name() + " Greataxe Head");
                });
                
                // Greathammer head variants
                ModItems.getGreatHammerHeadForMetal(metalName).ifPresent(head -> {
                    add(head, props.name() + " Greathammer Head");
                });
            });
        });
        
        // Generate translations for longswords (same metal for blade and hilt)
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                ModItems.getLongswordForMetal(metalName).ifPresent(longsword -> {
                    // Format: "{Metal} Longsword"
                    add(longsword, props.name() + " Longsword");
                });
            });
        });
        
        // Generate translations for greatswords (same metal for blade and hilt)
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                ModItems.getGreatswordForMetal(metalName).ifPresent(greatsword -> {
                    // Format: "{Metal} Greatsword"
                    add(greatsword, props.name() + " Greatsword");
                });
            });
        });
        
        // Generate translations for shortswords (same metal for blade and hilt)
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                ModItems.getShortswordForMetal(metalName).ifPresent(shortsword -> {
                    // Format: "{Metal} Shortsword"
                    add(shortsword, props.name() + " Shortsword");
                });
            });
        });
        
        // Generate translations for greataxes
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                ModItems.getGreatAxeForMetal(metalName).ifPresent(greataxe -> {
                    add(greataxe, props.name() + " Greataxe");
                });
            });
        });
        
        // Generate translations for greathammers
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            MetalHelper.getMetalProperties(metalName).ifPresent(props -> {
                ModItems.getGreatHammerForMetal(metalName).ifPresent(greathammer -> {
                    add(greathammer, props.name() + " Greathammer");
                });
            });
        });
        
        // Tooltips
        add("tooltip.tfc_weapons_plus.component_type", "Component Type: %s");
        add("tooltip.tfc_weapons_plus.material", "Material: %s");
        add("tooltip.tfc_weapons_plus.metal_tier", "Tier: %s");
        
        // Creative tab
        add("itemGroup.tfc_weapons_plus", "TFC Weapons Plus");
    }
}

