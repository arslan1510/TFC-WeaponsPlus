package com.concinnity.tfc_weapons_plus.util;

/**
 * Utility class for normalizing names for use in resource paths and identifiers
 */
public final class NameUtils {
    
    /**
     * Normalize metal name for use in resource paths
     * Converts to lowercase and replaces spaces and hyphens with underscores
     * 
     * @param metalName the metal name to normalize
     * @return normalized metal name
     */
    public static String normalizeMetalName(String metalName) {
        return metalName.toLowerCase()
            .replace(" ", "_")
            .replace("-", "_");
    }
    
    /**
     * Normalize wood name for use in resource paths
     * Converts to lowercase and replaces spaces and hyphens with underscores
     * 
     * @param woodName the wood name to normalize
     * @return normalized wood name
     */
    public static String normalizeWoodName(String woodName) {
        return woodName.toLowerCase()
            .replace(" ", "_")
            .replace("-", "_");
    }
    
    private NameUtils() {
        // Utility class
    }
}

