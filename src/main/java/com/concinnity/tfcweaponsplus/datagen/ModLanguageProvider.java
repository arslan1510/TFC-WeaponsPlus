
package com.concinnity.tfcweaponsplus.datagen;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import com.concinnity.tfcweaponsplus.models.ComponentType;
import com.concinnity.tfcweaponsplus.models.IItem;
import com.concinnity.tfcweaponsplus.models.WeaponType;

import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import com.concinnity.tfcweaponsplus.utils.TFCUtils;
import net.dries007.tfc.util.Metal;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, TFCWeaponsPlus.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        generateTranslations().forEach(entry -> add(entry.key(), entry.value()));
        add("itemGroup.tfcweaponsplus", "TFC Weapons Plus");
        add("creativetab.tfcweaponsplus.items", "TFC Weapons Plus");
    }

    private Stream<TranslationEntry> generateTranslations() {
        return ResourceUtils.generateItemVariants().map(this::createEntry);
    }

    private TranslationEntry createEntry(ResourceUtils.ItemVariant variant) {
        String key = "item.%s.%s".formatted(TFCWeaponsPlus.MOD_ID, variant.getTranslationPath());

        String itemName = capitalize(variant.item().getSerializedName());
        String displayName = variant.metal()
                .map(m -> "%s %s".formatted(capitalize(m.getSerializedName()), itemName))
                .orElse(itemName);

        return new TranslationEntry(key, displayName);
    }

    private static String capitalize(String text) {
        return Arrays.stream(text.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }

    private record TranslationEntry(String key, String value) {}
}