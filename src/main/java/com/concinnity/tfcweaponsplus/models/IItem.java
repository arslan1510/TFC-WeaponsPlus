package com.concinnity.tfcweaponsplus.models;

import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;

public interface IItem {
    String getSerializedName();
    Size getSize();
    Weight getWeight();
    ItemCategory getCategory();

    /**
     * @return The amount of fluid (in mB) this item produces when melted.
     * 100mB = 1 ingot, 200mB = 1 double ingot or sheet, 400mB = 1 double sheet
     */
    int getFluidAmount();

    enum ItemCategory {
        WEAPON,
        COMPONENT;

        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}