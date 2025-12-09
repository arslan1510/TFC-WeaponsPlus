package com.concinnity.tfc_weapons_plus.providers;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.item.ModItems;
import com.concinnity.tfc_weapons_plus.util.NameUtils;
import net.dries007.tfc.util.Metal;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Generates item models using data-driven approach
 */
public final class ModItemModelProvider extends ItemModelProvider {
    private static final UncheckedModelFile ITEM_GENERATED = new UncheckedModelFile("item/generated");

    private record ComponentModel(String type, Function<String, Item> getter) {}
    private record WeaponModel(String type, Function<String, Item> getter) {}

    private static final List<ComponentModel> COMPONENT_MODELS = List.of(
        new ComponentModel("guard", ModItems::getGuardForMetal),
        new ComponentModel("pommel", ModItems::getPommelForMetal),
        new ComponentModel("longsword_blade", ModItems::getLongswordBladeForMetal),
        new ComponentModel("greatsword_blade", ModItems::getGreatswordBladeForMetal),
        new ComponentModel("shortsword_blade", ModItems::getShortswordBladeForMetal),
        new ComponentModel("greataxe_head", ModItems::getGreatAxeHeadForMetal),
        new ComponentModel("greathammer_head", ModItems::getGreatHammerHeadForMetal),
        new ComponentModel("morningstar_head", ModItems::getMorningstarHeadForMetal)
    );

    private static final List<WeaponModel> WEAPON_MODELS = List.of(
        new WeaponModel("longsword", ModItems::getLongswordForMetal),
        new WeaponModel("greatsword", ModItems::getGreatswordForMetal),
        new WeaponModel("shortsword", ModItems::getShortswordForMetal),
        new WeaponModel("greataxe", ModItems::getGreatAxeForMetal),
        new WeaponModel("greathammer", ModItems::getGreatHammerForMetal),
        new WeaponModel("morningstar", ModItems::getMorningstarForMetal),
        new WeaponModel("quarterstaff", ModItems::getQuarterstaffForMetal)
    );

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TFCWeaponsPlus.MOD_ID, existingFileHelper);
    }
    
    @Override
    protected void registerModels() {
        // Grip model - use custom 3D parent model
        withExistingParent("item/wood/grip", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, "item/wood/grip/grip"));

        metals().forEach(metal -> {
            String metalName = metal.getSerializedName();
            String normalized = NameUtils.normalizeMetalName(metalName);

            // Hilt model (special case - no getter check needed)
            basicItemWithParent("item/metal/hilt/" + normalized, "item/metal/hilt/hilt", "metal/hilt/" + normalized);

            // Component models
            COMPONENT_MODELS.forEach(model ->
                basicItemWithParent("item/metal/" + model.type + "/" + normalized,
                    "item/metal/" + model.type + "/" + model.type, "metal/" + model.type + "/" + normalized));

            // Weapon models
            WEAPON_MODELS.forEach(model ->
                basicItemWithParent("item/metal/" + model.type + "/" + normalized,
                    "item/metal/" + model.type + "/" + model.type, "metal/" + model.type + "/" + normalized));
        });
    }

    private static Stream<Metal> metals() {
        return Arrays.stream(Metal.values()).filter(m -> m.tier() > 0 && m.allParts());
    }

    private ItemModelBuilder basicItemWithParent(String modelPath, String parentPath, String textureMetalPath) {
        return withExistingParent(modelPath, ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, parentPath))
            .texture("layer0", ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, "item/" + textureMetalPath));
    }
}
