package com.concinnity.tfc_weapons_plus.item;

/**
 * Component types for weapon crafting.
 * Using enum instead of sealed interface for better performance and simpler switch statements.
 */
public enum ComponentType {
    GRIP("grip", false),
    GUARD("guard", true),
    POMMEL("pommel", true),
    HILT("hilt", true),
    LONGSWORD_BLADE("longsword_blade", true),
    GREATSWORD_BLADE("greatsword_blade", true),
    SHORTSWORD_BLADE("shortsword_blade", true),
    GREATAXE_HEAD("greataxe_head", true),
    GREATHAMMER_HEAD("greathammer_head", true),
    MORNINGSTAR_HEAD("morningstar_head", true);

    private final String name;
    private final boolean metallic;

    ComponentType(String name, boolean metallic) {
        this.name = name;
        this.metallic = metallic;
    }

    public String getName() {
        return name;
    }

    public boolean isMetallic() {
        return metallic;
    }
}
