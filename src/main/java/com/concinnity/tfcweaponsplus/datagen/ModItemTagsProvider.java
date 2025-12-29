package com.concinnity.tfcweaponsplus.datagen;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import com.concinnity.tfcweaponsplus.models.WeaponType;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import com.concinnity.tfcweaponsplus.utils.TFCUtils;
import net.dries007.tfc.util.Metal;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class ModItemTagsProvider extends ItemTagsProvider {
    private static final TagKey<Item> C_TOOLS = createTag("c", "tools");
    private static final TagKey<Item> C_TOOLS_MELEE = createTag("c", "tools/meele_weapon");
    private static final TagKey<Item> TFC_TOOL_RACK = createTag("tfc", "usable_on_tool_rack");
    private static final TagKey<Item> TFC_SLASHING = createTag("tfc", "deals_slashing_damage");
    private static final TagKey<Item> TFC_CRUSHING = createTag("tfc", "deals_crushing_damage");
    private static final TagKey<Item> TFC_PIERCING = createTag("tfc", "deals_piercing_damage");

    public ModItemTagsProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()), TFCWeaponsPlus.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull Provider provider) {
        List<ResourceUtils.ItemVariant> allWeapons = ResourceUtils.generateItemVariants()
                .filter(variant -> variant.item() instanceof WeaponType)
                .toList();

        addGeneralWeaponTags(allWeapons);
        addWeaponCategoryTags(allWeapons);
        addDamageTypeTags(allWeapons);
        addMetalSpecificTags(allWeapons);
    }

    private void addGeneralWeaponTags(List<ResourceUtils.ItemVariant> weapons) {
        TagsProvider.TagAppender<Item> toolsTag = tag(C_TOOLS);
        TagsProvider.TagAppender<Item> toolRackTag = tag(TFC_TOOL_RACK);
        TagsProvider.TagAppender<Item> toolsMeleeTag = tag(C_TOOLS_MELEE);

        weapons.forEach(variant -> {
            ResourceLocation itemId = createResourceLocation(variant);
            toolsTag.addOptional(itemId);
            toolsMeleeTag.addOptional(itemId);
            toolRackTag.addOptional(itemId);
        });
    }

    private void addWeaponCategoryTags(List<ResourceUtils.ItemVariant> weapons) {
        addFilteredWeaponsToTag(weapons, ItemTags.SWORDS,
            variant -> matchesWeaponCategory(variant, WeaponType.WeaponCategory.SWORD));

        addFilteredWeaponsToTag(weapons, ItemTags.AXES,
            variant -> matchesWeaponCategory(variant, WeaponType.WeaponCategory.AXE));
    }

    private void addDamageTypeTags(List<ResourceUtils.ItemVariant> weapons) {
        addFilteredWeaponsToTag(weapons, TFC_SLASHING,
            variant -> matchesDamageType(variant, WeaponType.DamageType.SLASHING));

        addFilteredWeaponsToTag(weapons, TFC_CRUSHING,
            variant -> matchesDamageType(variant, WeaponType.DamageType.CRUSHING));

        addFilteredWeaponsToTag(weapons, TFC_PIERCING,
            variant -> matchesDamageType(variant, WeaponType.DamageType.PIERCING));
    }

    private void addMetalSpecificTags(List<ResourceUtils.ItemVariant> weapons) {
        Arrays.stream(Metal.values())
                .filter(TFCUtils::isValidMetal)
                .forEach(metal -> {
                    TagKey<Item> metalToolTag = createTag("tfc", "tools/" + metal.getSerializedName());
                    addFilteredWeaponsToTag(weapons, metalToolTag,
                        variant -> variant.metal().isPresent() && variant.metal().get() == metal);
                });
    }

    private void addFilteredWeaponsToTag(List<ResourceUtils.ItemVariant> weapons, TagKey<Item> tagKey,
                                         Predicate<ResourceUtils.ItemVariant> filter) {
        TagsProvider.TagAppender<Item> tagAppender = tag(tagKey);
        weapons.stream()
                .filter(filter)
                .map(this::createResourceLocation)
                .forEach(tagAppender::addOptional);
    }

    private boolean matchesWeaponCategory(ResourceUtils.ItemVariant variant, WeaponType.WeaponCategory category) {
        return variant.item() instanceof WeaponType weaponType
            && weaponType.getWeaponCategory() == category;
    }

    private boolean matchesDamageType(ResourceUtils.ItemVariant variant, WeaponType.DamageType damageType) {
        return variant.item() instanceof WeaponType weaponType
            && weaponType.getDamageType() == damageType;
    }

    private ResourceLocation createResourceLocation(ResourceUtils.ItemVariant variant) {
        return ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, variant.getRegistryPath());
    }

    private static TagKey<Item> createTag(String namespace, String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
