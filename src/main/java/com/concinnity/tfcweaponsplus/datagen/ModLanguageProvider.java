package com.concinnity.tfcweaponsplus.datagen;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import com.concinnity.tfcweaponsplus.registration.ItemRegistry;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import mod.traister101.datagenutils.data.EnhancedLanguageProvider;
import mod.traister101.datagenutils.data.util.LanguageTranslation;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

public class ModLanguageProvider extends EnhancedLanguageProvider {

    public ModLanguageProvider(PackOutput output, ExtraLanguageProvider... extraLanguageProviders) {
        super(output, TFCWeaponsPlus.MOD_ID, "en_us", extraLanguageProviders);
    }

    @Override
    protected void addTranslations() {
        ResourceUtils.generateItemVariants().forEach(variant -> {
            String key = "item.%s.%s".formatted(TFCWeaponsPlus.MOD_ID, variant.getTranslationPath());
            
            String itemName = capitalize(variant.item().getSerializedName());
            String displayName = variant.metal()
                    .map(m -> "%s %s".formatted(capitalize(m.getSerializedName()), itemName))
                    .orElse(itemName);

            add(LanguageTranslation.of(key, displayName));
        });

        add(LanguageTranslation.of("itemGroup.tfcweaponsplus", "TFC Weapons Plus"));
        add(LanguageTranslation.of("creativetab.tfcweaponsplus.items", "TFC Weapons Plus"));
    }

    @Override
    protected @NotNull Stream<KnownRegistryContents<?>> knownRegistryContents() {
        return Stream.of(KnownRegistryContents.item(ItemRegistry.getRegister()));
    }

    private static String capitalize(String text) {
        return Arrays.stream(text.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }
}
