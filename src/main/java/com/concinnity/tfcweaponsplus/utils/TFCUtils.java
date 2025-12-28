package com.concinnity.tfcweaponsplus.utils;

import com.concinnity.tfcweaponsplus.models.ComponentType;
import com.concinnity.tfcweaponsplus.models.IItem;
import com.concinnity.tfcweaponsplus.models.WeaponType;
import net.dries007.tfc.util.Metal;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class TFCUtils {
    public static boolean isValidMetal(Metal metal) {
        try {
            metal.toolTier();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Get melt temperature for a metal. These values match TFC's BuiltinFluidHeat.
     * Used during datagen since FluidHeat.MANAGER is not initialized yet.
     */
    public static float getMeltTemperature(Metal metal) {
        return switch (metal) {
            case BISMUTH -> 270f;
            case BISMUTH_BRONZE -> 985f;
            case BLACK_BRONZE -> 1070f;
            case BRONZE -> 950f;
            case BRASS -> 930f;
            case COPPER -> 1080f;
            case GOLD -> 1060f;
            case NICKEL -> 1453f;
            case ROSE_GOLD -> 960f;
            case SILVER -> 961f;
            case TIN -> 230f;
            case ZINC -> 420f;
            case STERLING_SILVER -> 950f;
            case WROUGHT_IRON -> 1535f;
            case CAST_IRON -> 1535f;
            case PIG_IRON -> 1535f;
            case STEEL -> 1540f;
            case BLACK_STEEL -> 1485f;
            case BLUE_STEEL -> 1540f;
            case RED_STEEL -> 1540f;
            case WEAK_STEEL -> 1540f;
            case WEAK_BLUE_STEEL -> 1540f;
            case WEAK_RED_STEEL -> 1540f;
            case HIGH_CARBON_STEEL -> 1540f;
            case HIGH_CARBON_BLACK_STEEL -> 1540f;
            case HIGH_CARBON_BLUE_STEEL -> 1540f;
            case HIGH_CARBON_RED_STEEL -> 1540f;
            case UNKNOWN -> 400f;
        };
    }


}
