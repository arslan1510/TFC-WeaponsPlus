package com.concinnity.tfcweaponsplus;

import com.concinnity.tfcweaponsplus.registration.CreativeModeTabs;
import com.concinnity.tfcweaponsplus.registration.ItemRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;


@Mod(TFCWeaponsPlus.MOD_ID)
public final class TFCWeaponsPlus {
    public static final String MOD_ID = "tfcweaponsplus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TFCWeaponsPlus(IEventBus modEventBus, ModContainer modContainer){
        modEventBus.addListener(this::commonSetup);

        ItemRegistry.registerAll();
        ItemRegistry.getRegister().register(modEventBus);

        CreativeModeTabs.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }
}
