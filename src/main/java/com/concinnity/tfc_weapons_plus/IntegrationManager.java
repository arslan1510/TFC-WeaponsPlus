package com.concinnity.tfc_weapons_plus;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.concinnity.tfc_weapons_plus.integration.TFCIntegration;

import net.neoforged.fml.ModList;

/**
 * Manages integration with other mods
 * TFC is required at runtime (optional for data generation)
 */
public final class IntegrationManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TFC_MOD_ID = "tfc";

    public static void initialize() {
        // Check if TFC mod is loaded using NeoForge's mod list
        boolean tfcAvailable = ModList.get().isLoaded(TFC_MOD_ID);
        
        if (!tfcAvailable) {
            LOGGER.error("TerraFirmaCraft (TFC) is NOT installed!");
            LOGGER.error("This mod REQUIRES TFC to function properly.");
        } else {
            TFCIntegration.initialize();
            LOGGER.info("TerraFirmaCraft integration enabled");
        }
    }
}

