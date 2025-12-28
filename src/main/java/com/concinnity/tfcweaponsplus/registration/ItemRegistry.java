
package com.concinnity.tfcweaponsplus.registration;

import com.concinnity.tfcweaponsplus.item.custom.AnimatedItem;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, "tfcweaponsplus");

    public static void registerAll() {
        ResourceUtils.generateItemVariants().forEach(variant -> {
            String registryName = variant.getRegistryPath();

            ITEMS.register(registryName, () -> new AnimatedItem(
                    ItemProperties.buildProperties(variant)
            ));
        });
    }

    public static DeferredRegister<Item> getRegister() {
        return ITEMS;
    }

}