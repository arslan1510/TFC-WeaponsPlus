package com.concinnity.tfcweaponsplus.item.client;

import com.concinnity.tfcweaponsplus.item.custom.AnimatedItem;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ItemModel extends GeoModel<AnimatedItem> {

    @Override
    public ResourceLocation getModelResource(AnimatedItem animatable) {
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(animatable);
        return ResourceUtils.getModelFromRegistryName(registryName);
    }

    @Override
    public ResourceLocation getTextureResource(AnimatedItem animatable) {
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(animatable);
        return ResourceUtils.getTextureFromRegistryName(registryName);
    }

    @Override
    public ResourceLocation getAnimationResource(AnimatedItem animatable) {
        return null;
    }
}
