package com.concinnity.tfc_weapons_plus.integration;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Helper class for working with TFC metals
 * Uses modern Java 21 features like records and pattern matching
 */
public final class MetalHelper {
    
    /**
     * Record representing metal properties
     */
    public record MetalProperties(
        String name,
        int durability,
        float efficiency,
        float attackDamage,
        int tier,
        int meltingPoint
    ) {
        public static MetalProperties of(String name, int durability, float efficiency, float attackDamage, int tier, int meltingPoint) {
            return new MetalProperties(name, durability, efficiency, attackDamage, tier, meltingPoint);
        }
    }

    // Metal properties map - using functional approach
    // Heating temperatures from TFC sword blade recipes: copper=1080, bronze=950, bismuth_bronze=985, black_bronze=1070, wrought_iron=1535, steel=1540, black_steel=1485, blue_steel=1540, red_steel=1540
    private static final Map<String, MetalProperties> METAL_PROPERTIES = Map.ofEntries(
        Map.entry("copper", MetalProperties.of("Copper", 200, 4.0f, 2.0f, 1, 1080)),
        Map.entry("bronze", MetalProperties.of("Bronze", 400, 5.0f, 3.0f, 2, 950)),
        Map.entry("bismuth_bronze", MetalProperties.of("Bismuth Bronze", 400, 5.0f, 3.0f, 2, 985)),
        Map.entry("black_bronze", MetalProperties.of("Black Bronze", 400, 5.0f, 3.0f, 2, 1070)),
        Map.entry("wrought_iron", MetalProperties.of("Wrought Iron", 600, 6.0f, 4.0f, 3, 1535)),
        Map.entry("steel", MetalProperties.of("Steel", 1000, 7.0f, 5.0f, 4, 1540)),
        Map.entry("black_steel", MetalProperties.of("Black Steel", 1500, 8.0f, 6.0f, 5, 1485)),
        Map.entry("blue_steel", MetalProperties.of("Blue Steel", 2000, 9.0f, 7.0f, 6, 1540)),
        Map.entry("red_steel", MetalProperties.of("Red Steel", 2000, 9.0f, 7.0f, 6, 1540))
    );
    
    /**
     * Get metal properties by name
     */
    public static Optional<MetalProperties> getMetalProperties(String metalName) {
        return Optional.ofNullable(METAL_PROPERTIES.get(metalName.toLowerCase()));
    }
    
    /**
     * Get all available metal names as a stream
     */
    public static Stream<String> getAllMetalNames() {
        return METAL_PROPERTIES.keySet().stream();
    }
    
    private MetalHelper() {
        // Utility class
    }
}

