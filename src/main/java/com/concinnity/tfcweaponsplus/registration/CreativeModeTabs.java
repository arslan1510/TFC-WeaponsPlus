package com.concinnity.tfcweaponsplus.registration;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import com.concinnity.tfcweaponsplus.models.WeaponType;
import com.concinnity.tfcweaponsplus.models.ComponentType;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class CreativeModeTabs {
    private static final List<Metal> METAL_TIER_ORDER = List.of(
        Metal.COPPER, Metal.BRONZE, Metal.BISMUTH_BRONZE, Metal.BLACK_BRONZE,
        Metal.WROUGHT_IRON, Metal.STEEL, Metal.BLACK_STEEL,
        Metal.BLUE_STEEL, Metal.RED_STEEL
    );

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TFCWeaponsPlus.MOD_ID);

    public static final Supplier<CreativeModeTab> ITEMS_TAB = CREATIVE_MODE_TAB.register("tfcweaponsplus",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.tfcweaponsplus.items"))
                    .icon(() -> ItemRegistry.getRegister().getEntries().stream()
                        .filter(entry -> entry.getId().getPath().equals("weapon/greatsword/steel"))
                        .findFirst()
                        .map(entry -> entry.get().getDefaultInstance())
                        .orElse(net.minecraft.world.item.Items.IRON_SWORD.getDefaultInstance()))
                    .displayItems((parameters, output) -> populateCreativeTab(output))
                    .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }

    private static void populateCreativeTab(CreativeModeTab.Output output) {
        ItemRegistry.getRegister().getEntries().stream()
            .map(DeferredHolder::get)
            .forEach(output::accept);
    }
}