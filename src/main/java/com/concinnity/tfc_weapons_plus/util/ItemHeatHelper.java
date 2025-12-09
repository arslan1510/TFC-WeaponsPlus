package com.concinnity.tfc_weapons_plus.util.data;

import net.dries007.tfc.common.component.heat.HeatDefinition;
import net.dries007.tfc.util.data.FluidHeat;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Helper methods for computing HeatDefinitions from fluid heat data.
 */
public final class ItemHeatHelper {

    /**
     * Build a HeatDefinition using the same calculation as the template helper:
     * heat_capacity = (specificHeat / HEAT_CAPACITY) * (units / 100)
     * forging = meltTemp * 0.6, welding = meltTemp * 0.8.
     */
    public static HeatDefinition heat(final Ingredient ingredient, final FluidHeat fluidHeat, final int units) {
        float heatCapacity = (fluidHeat.specificHeatCapacity() / FluidHeatData.HEAT_CAPACITY) * (units / 100f);
        float forgingTemp = fluidHeat.meltTemperature() * 0.6f;
        float weldingTemp = fluidHeat.meltTemperature() * 0.8f;
        return new HeatDefinition(ingredient, heatCapacity, forgingTemp, weldingTemp);
    }

    private ItemHeatHelper() {}
}

