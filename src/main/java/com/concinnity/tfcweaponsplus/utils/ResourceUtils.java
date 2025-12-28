package com.concinnity.tfcweaponsplus.utils;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import com.concinnity.tfcweaponsplus.models.ComponentType;
import com.concinnity.tfcweaponsplus.models.IItem;
import com.concinnity.tfcweaponsplus.models.WeaponType;
import net.dries007.tfc.util.Metal;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class ResourceUtils {
    private ResourceUtils() {}

    public static ResourceLocation of(String path) {
        return ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, path);
    }

    public static ResourceLocation getModelFromRegistryName(ResourceLocation registryName) {
        RegistryNameParts parts = parseRegistryName(registryName);
        return of("geo/item/%s/%s.geo.json".formatted(
                parts.category,
                parts.itemName
        ));
    }
    public static ResourceLocation getTextureFromRegistryName(ResourceLocation registryName) {
        RegistryNameParts parts = parseRegistryName(registryName);
        return of("textures/item/%s/%s/%s.png".formatted(
                parts.category,
                parts.itemName,
                parts.variant
        ));
    }

    private static RegistryNameParts parseRegistryName(ResourceLocation registryName) {
        String[] parts = registryName.getPath().split("/");
        return switch (parts.length) {
            case 3 -> new RegistryNameParts(parts[0], parts[1], parts[2]);
            case 2 -> new RegistryNameParts(parts[0], parts[1], "");
            default -> throw new IllegalArgumentException("Invalid registry name format: " + registryName);
        };
    }

    private record RegistryNameParts(String category, String itemName, String variant) {}

    public static Stream<ItemVariant> generateItemVariants() {
        return Stream.concat(
                Arrays.stream(WeaponType.values()),
                Arrays.stream(ComponentType.values())
        ).flatMap(item ->
                item == ComponentType.GRIP
                        ? Stream.of(new ItemVariant(item, Optional.empty()))
                        : Arrays.stream(Metal.values())
                        .filter(TFCUtils::isValidMetal)
                        .map(metal -> new ItemVariant(item, Optional.of(metal)))
        );
    }

    public record ItemVariant(IItem item, Optional<Metal> metal) {

        public String getPath(String separator) {
            return metal
                    .map(m -> "%s%s%s%s%s".formatted(
                            item.getCategory().getSerializedName(),
                            separator,
                            item.getSerializedName(),
                            separator,
                            m.getSerializedName()))
                    .orElseGet(() -> "%s%s%s".formatted(
                            item.getCategory().getSerializedName(),
                            separator,
                            item.getSerializedName()));
        }

        public String getRegistryPath() {
            return getPath("/");
        }

        public String getTranslationPath() {
            return getPath(".");
        }
    }

}
