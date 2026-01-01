package com.concinnity.tfcweaponsplus.models;

import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;

public enum ComponentType implements IItem {
    HILT(Size.SMALL, Weight.LIGHT, 100),
    POMMEL(Size.SMALL, Weight.LIGHT, 100),
    GUARD(Size.SMALL, Weight.LIGHT, 100),
    GRIP(Size.SMALL, Weight.LIGHT, 0),
    GREATSWORD_BLADE(Size.HUGE, Weight.VERY_HEAVY, 400),
    LONGSWORD_BLADE(Size.LARGE, Weight.HEAVY, 200),
    GREATAXE_HEAD(Size.HUGE, Weight.VERY_HEAVY, 200),
    GREATHAMMER_HEAD(Size.HUGE, Weight.VERY_HEAVY, 400),
    SWORD_BLADE(Size.LARGE, Weight.MEDIUM, 100);
   // SHORTSWORD_BLADE(Size.LARGE, Weight.MEDIUM, 100),
    //MORNINGSTAR_HEAD(Size.LARGE, Weight.HEAVY, 100);

    private final Size size;
    private final Weight weight;
    private final int fluidAmount;

    ComponentType(Size size, Weight weight, int fluidAmount) {
        this.size = size;
        this.weight = weight;
        this.fluidAmount = fluidAmount;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }

    @Override
    public Size getSize() { return size; }

    @Override
    public Weight getWeight() { return weight; }

    @Override
    public ItemCategory getCategory() { return ItemCategory.COMPONENT; }

    @Override
    public int getFluidAmount() { return fluidAmount; }
}