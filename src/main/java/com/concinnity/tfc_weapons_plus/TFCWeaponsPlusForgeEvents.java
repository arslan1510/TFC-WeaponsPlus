package com.concinnity.tfc_weapons_plus;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

public final class TFCWeaponsPlusForgeEvents {

    public static void init(final IEventBus eventBus) {
        eventBus.register(TFCWeaponsPlusForgeEvents.class);
    }

    @SubscribeEvent
    private static void onServerStarting(final ServerStartingEvent event) {
        TFCWeaponsPlus.LOG.info("TFC Weapons Plus server starting");
    }

    private TFCWeaponsPlusForgeEvents() {}
}

