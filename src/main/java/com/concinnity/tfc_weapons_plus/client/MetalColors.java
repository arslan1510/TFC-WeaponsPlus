package com.concinnity.tfc_weapons_plus.client;

import java.util.Map;

/**
 * Metal color definitions for client-side tinting
 * Colors extracted from TerraFirmaCraft metal ingot textures
 */
public final class MetalColors {

    private static final Map<String, Integer> METAL_COLORS = Map.ofEntries(
        Map.entry("copper", 0xC08471),
        Map.entry("bronze", 0xBEA080),
        Map.entry("bismuth_bronze", 0x7CA38A),
        Map.entry("black_bronze", 0x7E5C7F),
        Map.entry("wrought_iron", 0x888888),
        Map.entry("steel", 0x67787C),
        Map.entry("black_steel", 0x575757),
        Map.entry("blue_steel", 0x6976A1),
        Map.entry("red_steel", 0xCE6A6F)
    );

    /**
     * Get the color for a specific metal
     * @param metalName The metal name (lowercase)
     * @return RGB color as integer, or white (0xFFFFFF) if not found
     */
    public static int getColor(String metalName) {
        return METAL_COLORS.getOrDefault(metalName.toLowerCase(), 0xFFFFFF);
    }

    private MetalColors() {
        // Utility class
    }
}
