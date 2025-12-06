package com.concinnity.tfc_weapons_plus.client;

import com.concinnity.tfc_weapons_plus.TFCWeaponsPlus;
import com.concinnity.tfc_weapons_plus.integration.MetalHelper;
import com.concinnity.tfc_weapons_plus.item.ModItems;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Client-side setup for TFC Weapons Plus
 * Registers color handlers for metal components
 */
@EventBusSubscriber(modid = TFCWeaponsPlus.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Register color handlers for all metal components
        MetalHelper.getAllMetalNames().forEach(metalName -> {
            // Register pommel color
            ModItems.getPommelForMetal(metalName).ifPresent(item -> {
                event.register((stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        return MetalColors.getColor(metalName);
                    }
                    return 0xFFFFFF;
                }, item);
            });

            // Register guard color
            ModItems.getGuardForMetal(metalName).ifPresent(item -> {
                event.register((stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        return MetalColors.getColor(metalName);
                    }
                    return 0xFFFFFF;
                }, item);
            });

            // Register longsword blade color
            ModItems.getLongswordBladeForMetal(metalName).ifPresent(item -> {
                event.register((stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        return MetalColors.getColor(metalName);
                    }
                    return 0xFFFFFF;
                }, item);
            });

            // Register greatsword blade color
            ModItems.getGreatswordBladeForMetal(metalName).ifPresent(item -> {
                event.register((stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        return MetalColors.getColor(metalName);
                    }
                    return 0xFFFFFF;
                }, item);
            });

            // Register greataxe head color
            ModItems.getGreatAxeHeadForMetal(metalName).ifPresent(item -> {
                event.register((stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        return MetalColors.getColor(metalName);
                    }
                    return 0xFFFFFF;
                }, item);
            });
        });
    }
}
