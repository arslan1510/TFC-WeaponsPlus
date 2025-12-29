package com.concinnity.tfcweaponsplus.datagen;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import com.concinnity.tfcweaponsplus.registration.ItemRegistry;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import mod.traister101.datagenutils.data.tfc.ItemSizeProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ModItemSizeProvider extends ItemSizeProvider {

    public ModItemSizeProvider(PackOutput output, CompletableFuture<Provider> lookup) {
        super(output, TFCWeaponsPlus.MOD_ID, lookup);
    }

    @Override
    protected void addData(@NotNull Provider provider) {
        Map<String, DeferredHolder<Item, ? extends Item>> itemMap = ItemRegistry.getRegister().getEntries().stream()
                .collect(Collectors.toMap(
                        holder -> holder.getId().getPath(),
                        holder -> holder
                ));

        ResourceUtils.generateItemVariants().forEach(variant -> {
            var path = variant.getRegistryPath();

            Optional.ofNullable(itemMap.get(path))
                    .ifPresentOrElse(itemHolder -> {
                        var size = variant.item().getSize();
                        var weight = variant.item().getWeight();
                        add(path, size(itemHolder.get(), size, weight));
                    }, () -> TFCWeaponsPlus.LOGGER.warn("Could not find registered item for variant: {}", path));
        });
    }
}
