package com.concinnity.tfc_weapons_plus;

import com.concinnity.tfc_weapons_plus.providers.ModItemModelProvider;
import com.concinnity.tfc_weapons_plus.providers.TFCItemSizeProvider;
import com.concinnity.tfc_weapons_plus.providers.ModItemTagsProvider;
import com.concinnity.tfc_weapons_plus.providers.ModLanguageProvider;
import com.concinnity.tfc_weapons_plus.providers.ModRecipeProvider;
import com.concinnity.tfc_weapons_plus.providers.TFCAnvilRecipeProvider;
import com.concinnity.tfc_weapons_plus.providers.TFCItemHeatProvider;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = TFCWeaponsPlus.MOD_ID)
public final class DataGenerators {

    @SubscribeEvent
    private static void gatherData(final GatherDataEvent event) {
        final var generator = event.getGenerator();
        final var packOutput = generator.getPackOutput();
        final var lookupProvider = event.getLookupProvider();
        final var existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new TFCItemSizeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new TFCItemHeatProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new TFCAnvilRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(packOutput, "en_us"));
    }

    private DataGenerators() {}
}

