package com.concinnity.tfc_weapons_plus.client;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class TFCWeaponsPlusClientEvents {

    public static void init(final ModContainer modContainer, final IEventBus modBus) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modBus.addListener(TFCWeaponsPlusClientEvents::clientSetup);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            TFCWeaponsPlus.LOG.info("TFC Weapons Plus client setup");
        });
    }

    private TFCWeaponsPlusClientEvents() {}
}

