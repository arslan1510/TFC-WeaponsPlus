package com.concinnity.tfc_weapons_plus.providers;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.item.ModItems;

import net.dries007.tfc.util.Metal;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Generates language translations using data-driven approach
 */
public final class ModLanguageProvider extends LanguageProvider {
    
    private record TranslationEntry(
        String type,
        Function<String, String> getter,
        String suffix
    ) {}

    private static final List<TranslationEntry> COMPONENT_TRANSLATIONS = List.of(
        new TranslationEntry("guard", metal -> "Guard", "Guard"),
        new TranslationEntry("pommel", metal -> "Pommel", "Pommel"),
        new TranslationEntry("hilt", metal -> "Hilt", "Hilt"),
        new TranslationEntry("longsword_blade", metal -> "Longsword Blade", "Longsword Blade"),
        new TranslationEntry("greatsword_blade", metal -> "Greatsword Blade", "Greatsword Blade"),
        new TranslationEntry("shortsword_blade", metal -> "Shortsword Blade", "Shortsword Blade"),
        new TranslationEntry("greataxe_head", metal -> "Greataxe Head", "Greataxe Head"),
        new TranslationEntry("greathammer_head", metal -> "Greathammer Head", "Greathammer Head"),
        new TranslationEntry("morningstar_head", metal -> "Morningstar Head", "Morningstar Head")
    );

    private static final List<TranslationEntry> WEAPON_TRANSLATIONS = List.of(
        new TranslationEntry("longsword", metal -> "Longsword", "Longsword"),
        new TranslationEntry("greatsword", metal -> "Greatsword", "Greatsword"),
        new TranslationEntry("shortsword", metal -> "Shortsword", "Shortsword"),
        new TranslationEntry("greataxe", metal -> "Greataxe", "Greataxe"),
        new TranslationEntry("greathammer", metal -> "Greathammer", "Greathammer"),
        new TranslationEntry("morningstar", metal -> "Morningstar", "Morningstar"),
        new TranslationEntry("quarterstaff", metal -> "Quarterstaff", "Quarterstaff")
    );
    
    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, TFCWeaponsPlus.MOD_ID, locale);
    }
    
    @Override
    protected void addTranslations() {
        add(ModItems.GRIP.get(), "Grip");

        metalStream().forEach(metal -> {
            String metalName = metal.getSerializedName();
            String displayName = capitalize(metalName);

            // Component translations
            COMPONENT_TRANSLATIONS.forEach(entry -> {
                getItemForType(entry.type, metalName).ifPresent(item ->
                    add(item, displayName + " " + entry.suffix));
            });

            // Weapon translations
            WEAPON_TRANSLATIONS.forEach(entry -> {
                getItemForType(entry.type, metalName).ifPresent(item ->
                    add(item, displayName + " " + entry.suffix));
            });
        });

        add("itemGroup.tfc_weapons_plus", "TFC Weapons Plus");
    }

    private static Stream<Metal> metalStream() {
        return Arrays.stream(Metal.values())
            .filter(metal -> metal.tier() > 0 && metal.allParts());
    }

    private static String capitalize(String serializedName) {
        String[] parts = serializedName.split("_");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) result.append(" ");
            String part = parts[i];
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1));
                }
            }
        }
        return result.toString();
    }

    private java.util.Optional<net.minecraft.world.item.Item> getItemForType(String type, String metalName) {
        return switch (type) {
            case "guard" -> java.util.Optional.of(ModItems.getGuardForMetal(metalName));
            case "pommel" -> java.util.Optional.of(ModItems.getPommelForMetal(metalName));
            case "hilt" -> java.util.Optional.of(ModItems.getHiltForMetal(metalName));
            case "longsword_blade" -> java.util.Optional.of(ModItems.getLongswordBladeForMetal(metalName));
            case "greatsword_blade" -> java.util.Optional.of(ModItems.getGreatswordBladeForMetal(metalName));
            case "shortsword_blade" -> java.util.Optional.of(ModItems.getShortswordBladeForMetal(metalName));
            case "greataxe_head" -> java.util.Optional.of(ModItems.getGreatAxeHeadForMetal(metalName));
            case "greathammer_head" -> java.util.Optional.of(ModItems.getGreatHammerHeadForMetal(metalName));
            case "morningstar_head" -> java.util.Optional.of(ModItems.getMorningstarHeadForMetal(metalName));
            case "longsword" -> java.util.Optional.of(ModItems.getLongswordForMetal(metalName));
            case "greatsword" -> java.util.Optional.of(ModItems.getGreatswordForMetal(metalName));
            case "shortsword" -> java.util.Optional.of(ModItems.getShortswordForMetal(metalName));
            case "greataxe" -> java.util.Optional.of(ModItems.getGreatAxeForMetal(metalName));
            case "greathammer" -> java.util.Optional.of(ModItems.getGreatHammerForMetal(metalName));
            case "morningstar" -> java.util.Optional.of(ModItems.getMorningstarForMetal(metalName));
            case "quarterstaff" -> java.util.Optional.of(ModItems.getQuarterstaffForMetal(metalName));
            default -> java.util.Optional.empty();
        };
    }
}
