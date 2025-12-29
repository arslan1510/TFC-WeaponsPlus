package com.concinnity.tfcweaponsplus.utils;

import net.dries007.tfc.util.Metal;

public class TFCUtils {
    public static boolean isValidMetal(Metal metal) {
        try {
            metal.toolTier();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }


}
