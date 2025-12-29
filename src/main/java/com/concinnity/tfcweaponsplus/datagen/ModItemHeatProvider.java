package com.concinnity.tfcweaponsplus.datagen;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import com.concinnity.tfcweaponsplus.registration.ItemRegistry;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import mod.traister101.datagenutils.data.tfc.ItemHeatProvider;
import mod.traister101.datagenutils.data.util.tfc.TFCFluidHeat;
import net.dries007.tfc.util.Metal;
import net.dries007.tfc.util.data.FluidHeat;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ModItemHeatProvider extends ItemHeatProvider {

    public ModItemHeatProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, TFCWeaponsPlus.MOD_ID, lookup);
    }

    @Override
    protected void addData(HolderLookup.@NotNull Provider provider) {
        Map<String, DeferredHolder<Item, ? extends Item>> itemMap = ItemRegistry.getRegister().getEntries().stream()
                .collect(Collectors.toMap(
                        holder -> holder.getId().getPath(),
                        holder -> holder
                ));

        ResourceUtils.generateItemVariants()
                .filter(variant -> variant.metal().isPresent())
                .forEach(variant -> {
                    var metal = variant.metal().orElseThrow();
                    var amount = variant.item().getFluidAmount();
                    var path = variant.getRegistryPath();

                    Optional.ofNullable(itemMap.get(path))
                            .ifPresentOrElse(itemHolder -> getFluidHeat(metal).ifPresentOrElse(
                                    heat -> addAndMelt(path, Ingredient.of(itemHolder.get()), heat, amount),
                                    () -> TFCWeaponsPlus.LOGGER.warn("Could not find TFCFluidHeat for metal: {}", metal.name())
                            ), () -> TFCWeaponsPlus.LOGGER.warn("Could not find registered item for variant: {}", path));
                });
    }

    private Optional<FluidHeat> getFluidHeat(Metal metal) {
        try {
            var field = TFCFluidHeat.class.getField(metal.name());
            return Optional.of((FluidHeat) field.get(null));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
    }
}
