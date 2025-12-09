package com.concinnity.tfc_weapons_plus.util;

import net.dries007.tfc.common.LevelTier;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.Metal;
import net.dries007.tfc.util.data.FluidHeat;

import java.util.stream.Stream;

public final class MetalData {

    public record Props(
        String name,
        int durability,
        float efficiency,
        float attackDamage,
        int tier,
        int meltingPoint
    ) {}

    public static Stream<Metal> stream() {
        return Stream.of(Metal.values())
            .filter(metal -> metal.tier() > 0 && metal.allParts());
    }

    public static Stream<String> names() {
        return stream().map(Metal::getSerializedName);
    }

    public static Metal fromName(String metalName) {
        return Metal.valueOf(metalName.toUpperCase());
    }

    public static Props dataProps(Metal metal) {
        LevelTier tier = metal.toolTier();
        return new Props(
            capitalize(metal.getSerializedName()),
            tier.getUses(),
            tier.getSpeed(),
            tier.getAttackDamageBonus(),
            tier.level(),
            meltingPointFromTier(tier)
        );
    }

    private static int meltingPointFromTier(LevelTier tier) {
        return switch (tier.level()) {
            case 1 -> 1083;  // Copper
            case 2 -> 950;   // Bronze tier
            case 3 -> 1538;  // Wrought iron
            case 4 -> 1540;  // Steel
            case 5 -> 1545;  // Black steel
            case 6 -> 1550;  // Blue/red steel
            default -> throw new IllegalArgumentException("Unknown tier: " + tier.level());
        };
    }

    private static String capitalize(String serializedName) {
        String[] parts = serializedName.split("_");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) result.append(" ");
            String part = parts[i];
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1));
                }
            }
        }
        return result.toString();
    }

    private MetalData() {}
}

