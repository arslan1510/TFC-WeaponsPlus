package com.concinnity.tfcweaponsplus.item.client;

import com.concinnity.tfcweaponsplus.item.custom.AnimatedItem;
import com.concinnity.tfcweaponsplus.utils.TFCUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.FastBoneFilterGeoLayer;

import java.util.List;

public class ItemRenderer extends GeoItemRenderer<AnimatedItem> {

    public ItemRenderer(){
        super(new ItemModel());
        
        addRenderLayer(new FastBoneFilterGeoLayer<>(this, () -> List.of("1", "2", "3", "1A2", "1A3", "2A3", "1A2A3")) {
            @Override
            protected void checkAndApply(GeoBone bone, AnimatedItem animatable, float partialTick) {
                String registryName = BuiltInRegistries.ITEM.getKey(animatable).getPath();
                String metal = registryName.substring(registryName.lastIndexOf('/') + 1);
                int tier = TFCUtils.getMetalTier(metal);

                // "1A2" contains "1" (Tier 1) and "2" (Tier 2)
                boolean visible = bone.getName().contains(String.valueOf(tier));
                bone.setHidden(!visible);
            }
        });
    }
}
