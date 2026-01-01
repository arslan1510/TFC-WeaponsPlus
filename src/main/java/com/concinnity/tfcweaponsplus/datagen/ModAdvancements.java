package com.concinnity.tfcweaponsplus.datagen;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import com.concinnity.tfcweaponsplus.models.ComponentType;
import com.concinnity.tfcweaponsplus.models.WeaponType;
import com.concinnity.tfcweaponsplus.registration.ItemRegistry;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import mod.traister101.datagenutils.data.AdvancementSubProvider;
import mod.traister101.datagenutils.data.EnhancedAdvancementProvider;
import mod.traister101.datagenutils.data.util.AdvancementBuilder;
import mod.traister101.datagenutils.data.util.AdvancementOutput;
import mod.traister101.datagenutils.data.util.SimpleDisplayInfo;
import net.dries007.tfc.util.Metal;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModAdvancements implements AdvancementSubProvider {

    public static EnhancedAdvancementProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        return new EnhancedAdvancementProvider(output, lookupProvider, existingFileHelper, java.util.List.of(new ModAdvancements()));
    }

    @Override
    public void generate(AdvancementOutput output, HolderLookup.Provider registries) {
        var itemMap = ItemRegistry.getRegister().getEntries().stream()
                .collect(Collectors.toMap(h -> h.getId().getPath(), h -> h));

        Function<ResourceUtils.ItemVariant, Optional<Item>> itemLookup = variant ->
                Optional.ofNullable(itemMap.get(variant.getRegistryPath())).map(DeferredHolder::get);

        var icon = itemLookup.apply(new ResourceUtils.ItemVariant(WeaponType.GREATSWORD, Optional.of(Metal.BLACK_STEEL)));

        AdvancementBuilder builder = AdvancementBuilder.root()
                .display(SimpleDisplayInfo.builder()
                        .icon(icon.orElse(null))
                        .title("advancements.tfcweaponsplus.root.title")
                        .description("advancements.tfcweaponsplus.root.description")
                        .background(ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"))
                        .type(AdvancementType.TASK))
                .requirementsStrategy(AdvancementRequirements.Strategy.OR);

        itemMap.values().forEach(holder -> {
            ItemLike item = (ItemLike) holder.get();
            builder.addCriterion("has_" + holder.getId().getPath(),
                    InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(item).build()));
        });

        builder.save(output, location("root"));
    }

    private static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, path);
    }
}
