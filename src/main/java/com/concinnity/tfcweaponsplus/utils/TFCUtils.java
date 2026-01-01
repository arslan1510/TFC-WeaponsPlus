package com.concinnity.tfcweaponsplus.utils;

import net.dries007.tfc.util.Metal;

import java.util.Arrays;

public class TFCUtils {

    public static boolean isValidMetal(Metal metal) {
        try {
            return metal.toolTier() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static int getMetalTier(String metalName) {
        try {
            Metal metal = Metal.valueOf(metalName.toUpperCase());
            
            if (!isValidMetal(metal)) {

                return 1;
            }

            int tfcTier = metal.tier();
            
            if (tfcTier >= 4) return 3; // Steel and above
            if (tfcTier == 3) return 2; // Iron
            return 1; // Bronze and below
            
        } catch (IllegalArgumentException | NullPointerException e) {
            return 1;
        }
    }
}
