package com.concinnity.tfc_weapons_plus.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Main data generator entry point
 * Uses modern Java functional patterns
 */
public final class ModDataGenerators {
    
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();
        
        // Add providers
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new TFCAnvilRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new TFCItemHeatProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(packOutput, "en_us"));
    }
}

