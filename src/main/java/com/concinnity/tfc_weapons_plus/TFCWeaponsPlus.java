package com.concinnity.tfc_weapons_plus;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.concinnity.tfc_weapons_plus.client.TFCWeaponsPlusClientEvents;
import com.concinnity.tfc_weapons_plus.client.TFCWeaponsPlusClientForgeEvents;
import com.concinnity.tfc_weapons_plus.item.ModItems;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

@Mod(TFCWeaponsPlus.MOD_ID)
public final class TFCWeaponsPlus {
    public static final String MOD_ID = "tfc_weapons_plus";
    public static final Logger LOG = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WEAPONS_PLUS_TAB =
        CREATIVE_MODE_TABS.register("weapons_plus", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tfc_weapons_plus"))
            .icon(TFCWeaponsPlus::pickIcon)
            .displayItems((parameters, output) -> ModItems.getAllItems().forEach(output::accept))
            .build());

    public TFCWeaponsPlus(ModContainer modContainer, IEventBus modBus, Dist dist) {
        LOG.info("Initializing TFC Weapons Plus mod");

        modBus.register(TFCWeaponsPlus.class);
        ModItems.ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        modBus.addListener(this::commonSetup);

        TFCWeaponsPlusForgeEvents.init(NeoForge.EVENT_BUS);

        if (dist == Dist.CLIENT) {
            TFCWeaponsPlusClientEvents.init(modContainer, modBus);
            TFCWeaponsPlusClientForgeEvents.init(NeoForge.EVENT_BUS);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOG.info("TFC Weapons Plus common setup");
            // Initialize item cache for fast lookups
            ModItems.initializeCache();
            LOG.info("Item cache initialized for fast lookups");
        });
    }

    private static ItemStack pickIcon() {
        // Direct item access (no Optional overhead)
        try {
            return new ItemStack(ModItems.getGreatswordForMetal("steel"));
        } catch (Exception e) {
            try {
                return new ItemStack(ModItems.getLongswordForMetal("steel"));
            } catch (Exception ex) {
                return new ItemStack(ModItems.GRIP.get());
            }
        }
    }

    @SubscribeEvent
    private static void onCreativeTabBuild(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            ModItems.getAllItems().forEach(event::accept);
        }
    }

    /**
     * Shorthand for {@code ResourceLocation.fromNamespaceAndPath(MOD_ID, path)}.
     */
    public static ResourceLocation location(final String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    /**
     * Helper for creating modid prepended lang keys.
     */
    public static String lang(final String langKey) {
        return MOD_ID + "." + langKey;
    }
}
