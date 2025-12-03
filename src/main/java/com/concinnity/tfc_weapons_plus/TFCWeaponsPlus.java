package com.concinnity.tfc_weapons_plus;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.concinnity.tfc_weapons_plus.datagen.ModDataGenerators;
import com.concinnity.tfc_weapons_plus.item.ModItems;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

@Mod(TFCWeaponsPlus.MODID)
public final class TFCWeaponsPlus {
    public static final String MODID = "tfc_weapons_plus";
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MODULAR_WEAPON_TAB = 
        CREATIVE_MODE_TABS.register("modular_weapon", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tfc_weapons_plus"))
            .icon(() -> ModItems.getPommelForMetal("copper")
                .map(ItemStack::new)
                .orElseGet(() -> ModItems.getHiltForMetal("copper")
                    .map(ItemStack::new)
                    .orElseGet(() -> new ItemStack(ModItems.GRIP.get()))))
            .displayItems((parameters, output) -> {
                ModItems.getAllItems().forEach(output::accept);
            })
            .build());
    
    public TFCWeaponsPlus(IEventBus modEventBus) {
        LOGGER.info("Initializing TFC Weapons Plus mod");
        
        // Register items
        ModItems.ITEMS.register(modEventBus);
        
        // Register creative tabs
        CREATIVE_MODE_TABS.register(modEventBus);
        
        // Register event listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(ModDataGenerators::gatherData);
        
        // Register server events
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("TFC Weapons Plus common setup");
            // Initialize TFC integration
            IntegrationManager.initialize();
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Items are already added to our custom tab via displayItems
        // Optionally also add to combat tab
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            ModItems.getAllItems().forEach(event::accept);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("TFC Weapons Plus server starting");
    }
}

