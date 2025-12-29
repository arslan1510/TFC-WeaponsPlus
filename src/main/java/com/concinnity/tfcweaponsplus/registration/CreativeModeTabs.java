package com.concinnity.tfcweaponsplus.registration;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CreativeModeTabs {

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