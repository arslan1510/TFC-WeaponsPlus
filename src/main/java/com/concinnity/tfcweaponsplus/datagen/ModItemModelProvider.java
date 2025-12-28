package com.concinnity.tfcweaponsplus.datagen;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import com.concinnity.tfcweaponsplus.models.ComponentType;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TFCWeaponsPlus.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ResourceUtils.generateItemVariants().forEach(variant -> {

            if (variant.item() == ComponentType.GRIP) {
                return;
            }

            String registryPath = "item/" + variant.getRegistryPath();

            String parentPath = "item/%s/%s".formatted(
                    variant.item().getCategory().getSerializedName(),
                    variant.item().getSerializedName()
            );

            withExistingParent(registryPath, modLoc(parentPath));
        });
    }
}
