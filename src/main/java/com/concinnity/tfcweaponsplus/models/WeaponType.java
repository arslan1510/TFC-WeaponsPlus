package com.concinnity.tfcweaponsplus.models;

import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;

public enum WeaponType implements IItem {
    GREATSWORD(Size.HUGE, Weight.VERY_HEAVY, -2.8, 10.0, 3.0, WeaponCategory.SWORD, DamageType.SLASHING, 400),
    GREATAXE(Size.HUGE, Weight.VERY_HEAVY, -2.7, 12.0, 2.5, WeaponCategory.AXE, DamageType.SLASHING, 400),
    GREATHAMMER(Size.HUGE, Weight.VERY_HEAVY, -2.9, 13.0, 2.5, WeaponCategory.HAMMER, DamageType.CRUSHING, 400),
    LONGSWORD(Size.LARGE, Weight.HEAVY, -2.4, 7.0, 2.5, WeaponCategory.SWORD, DamageType.SLASHING, 200),
    SWORD(Size.LARGE, Weight.MEDIUM, -2.0, 5.0, 2.0, WeaponCategory.SWORD, DamageType.SLASHING, 100);

    // SHORTSWORD(Size.LARGE, Weight.MEDIUM, -2.0, 5.0, 2.0, WeaponCategory.SWORD, DamageType.SLASHING, 100),
    // MORNINGSTAR(Size.LARGE, Weight.HEAVY, -2.6, 8.0, 2.5, WeaponCategory.MACE, DamageType.CRUSHING, 100),
    // QUARTERSTAFF(Size.LARGE, Weight.MEDIUM, -2.0, 4.0, 3.0, WeaponCategory.STAFF, DamageType.PIERCING, 200),
    
    private final Size size;
    private final Weight weight;
    private final double baseAttackSpeed;
    private final double baseDamage;
    private final double baseReach;
    private final WeaponCategory category;
    private final DamageType damageType;
    private final int fluidAmount;

    WeaponType(Size size, Weight weight, double attackSpeed, double damage, double reach, WeaponCategory category, DamageType damageType, int fluidAmount) {
        this.size = size;
        this.weight = weight;
        this.baseAttackSpeed = attackSpeed;
        this.baseDamage = damage;
        this.baseReach = reach;
        this.category = category;
        this.damageType = damageType;
        this.fluidAmount = fluidAmount;
    }

    public enum WeaponCategory {
        SWORD, AXE, HAMMER, MACE, STAFF
    }

    public enum DamageType {
        SLASHING, CRUSHING, PIERCING
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }

    @Override
    public Size getSize() {
        return size;
    }

    @Override
    public Weight getWeight() {
        return weight;
    }

    @Override
    public ItemCategory getCategory() {
        return ItemCategory.WEAPON;
    }

    public double getBaseAttackSpeed() {
        return baseAttackSpeed;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public double getBaseReach() {
        return baseReach;
    }

    public WeaponCategory getWeaponCategory() {
        return category;
    }

    public DamageType getDamageType() {
        return damageType;
    }

    @Override
    public int getFluidAmount() {
        return fluidAmount;
    }
}