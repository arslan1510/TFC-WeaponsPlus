package com.concinnity.tfcweaponsplus.datagen;

import com.concinnity.tfcweaponsplus.TFCWeaponsPlus;
import com.concinnity.tfcweaponsplus.models.ComponentType;
import com.concinnity.tfcweaponsplus.models.WeaponType;
import com.concinnity.tfcweaponsplus.registration.ItemRegistry;
import com.concinnity.tfcweaponsplus.utils.ResourceUtils;
import com.concinnity.tfcweaponsplus.utils.TFCUtils;
import mod.traister101.datagenutils.data.EnhancedRecipeProvider;
import mod.traister101.datagenutils.data.recipe.CraftingRecipeBuilder;
import mod.traister101.datagenutils.data.recipe.tfc.AnvilRecipeBuilder;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.component.forge.ForgeRule;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModRecipeProvider extends EnhancedRecipeProvider {

    private record AnvilSpec(ComponentType component, String ingredientTagFormat, List<ForgeRule> rules) {}

    private static final List<AnvilSpec> ANVIL_SPECS = List.of(
            new AnvilSpec(ComponentType.GUARD, "c:ingots/%s", List.of(ForgeRule.HIT_SECOND_LAST, ForgeRule.HIT_LAST)),
            new AnvilSpec(ComponentType.POMMEL, "c:ingots/%s", List.of(ForgeRule.HIT_SECOND_LAST, ForgeRule.HIT_LAST)),
            new AnvilSpec(ComponentType.HILT, "c:ingots/%s", List.of(ForgeRule.HIT_LAST, ForgeRule.DRAW_ANY, ForgeRule.SHRINK_NOT_LAST)),
            new AnvilSpec(ComponentType.SHORTSWORD_BLADE, "c:ingots/%s", List.of(ForgeRule.HIT_SECOND_LAST, ForgeRule.HIT_LAST)),
            new AnvilSpec(ComponentType.LONGSWORD_BLADE, "c:double_ingots/%s", List.of(ForgeRule.BEND_THIRD_LAST, ForgeRule.BEND_SECOND_LAST, ForgeRule.HIT_LAST)),
            new AnvilSpec(ComponentType.GREATSWORD_BLADE, "c:double_sheets/%s", List.of(ForgeRule.BEND_THIRD_LAST, ForgeRule.BEND_SECOND_LAST, ForgeRule.HIT_LAST)),
            new AnvilSpec(ComponentType.GREATAXE_HEAD, "c:sheets/%s", List.of(ForgeRule.PUNCH_LAST, ForgeRule.HIT_SECOND_LAST)),
            new AnvilSpec(ComponentType.GREATHAMMER_HEAD, "c:double_sheets/%s", List.of(ForgeRule.PUNCH_LAST, ForgeRule.HIT_SECOND_LAST)),
            new AnvilSpec(ComponentType.MORNINGSTAR_HEAD, "c:ingots/%s", List.of(ForgeRule.HIT_SECOND_LAST, ForgeRule.HIT_LAST)),
            new AnvilSpec(ComponentType.SWORD_BLADE, "c:ingots/%s", List.of(ForgeRule.HIT_SECOND_LAST, ForgeRule.HIT_LAST))
    );

    public ModRecipeProvider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> registries,
                             final AdditionalRecipeProvider... additionalRecipeProviders) {
        super(output, registries, additionalRecipeProviders);
    }

    private record BladeHiltRecipe(WeaponType weaponType, ComponentType bladeComponent) {
        void generate(RecipeOutput output, Metal metal, Function<ResourceUtils.ItemVariant, Optional<Item>> itemLookup) {
            var hilt = itemLookup.apply(new ResourceUtils.ItemVariant(ComponentType.HILT, Optional.of(metal)));
            var blade = itemLookup.apply(new ResourceUtils.ItemVariant(bladeComponent, Optional.of(metal)));
            var weaponVariant = new ResourceUtils.ItemVariant(weaponType, Optional.of(metal));
            var weapon = itemLookup.apply(weaponVariant);

            if (hilt.isPresent() && blade.isPresent() && weapon.isPresent()) {
                CraftingRecipeBuilder.shaped(weapon.get(), 1)
                    .pattern("B")
                    .pattern("H")
                    .define('B', blade.get()).define('H', hilt.get())
                    .unlockedBy("has_hilt", has(hilt.get()))
                    .unlockedBy("has_blade", has(blade.get()))
                    .save(output, recipeId("crafting/" + weaponVariant.getRegistryPath() + "/assembly"));
            }
        }
    }

    private record HeadGripRecipe(WeaponType weaponType, ComponentType headComponent, String... pattern) {
        void generate(RecipeOutput output, Metal metal, Function<ResourceUtils.ItemVariant, Optional<Item>> itemLookup) {
            var head = itemLookup.apply(new ResourceUtils.ItemVariant(headComponent, Optional.of(metal)));
            var weaponVariant = new ResourceUtils.ItemVariant(weaponType, Optional.of(metal));
            var weapon = itemLookup.apply(weaponVariant);
            var grip = itemLookup.apply(new ResourceUtils.ItemVariant(ComponentType.GRIP, Optional.empty()));

            if (head.isPresent() && weapon.isPresent() && grip.isPresent()) {
                var builder = CraftingRecipeBuilder.shaped(weapon.get(), 1);
                for (String p : pattern) builder.pattern(p);
                
                builder.define('H', head.get()).define('L', TFCTags.Items.LUMBER).define('G', grip.get())
                    .unlockedBy("has_head", has(head.get()))
                    .save(output, recipeId("crafting/" + weaponVariant.getRegistryPath() + "/assembly"));
            }
        }
    }

    private static final List<BladeHiltRecipe> BLADE_HILT_RECIPES = List.of(
        new BladeHiltRecipe(WeaponType.LONGSWORD, ComponentType.LONGSWORD_BLADE),
        new BladeHiltRecipe(WeaponType.GREATSWORD, ComponentType.GREATSWORD_BLADE),
        new BladeHiltRecipe(WeaponType.SHORTSWORD, ComponentType.SHORTSWORD_BLADE),
        new BladeHiltRecipe(WeaponType.SWORD, ComponentType.SWORD_BLADE)
    );

    private static final List<HeadGripRecipe> HEAD_GRIP_RECIPES = List.of(
        new HeadGripRecipe(WeaponType.GREATAXE, ComponentType.GREATAXE_HEAD, "  H", " L ", "G  "),
        new HeadGripRecipe(WeaponType.GREATHAMMER, ComponentType.GREATHAMMER_HEAD, "  H", " L ", "G  ")
    );


    @Override
    protected void buildRecipes(@NotNull final RecipeOutput output, @NotNull final HolderLookup.Provider holderLookup) {
        var itemMap = ItemRegistry.getRegister().getEntries().stream()
                .collect(Collectors.toMap(h -> h.getId().getPath(), h -> h));

        Function<ResourceUtils.ItemVariant, Optional<Item>> itemLookup = variant ->
            Optional.ofNullable(itemMap.get(variant.getRegistryPath())).map(DeferredHolder::get);

        generateHiltAssemblyRecipes(output, itemLookup);
        
        generateAnvilRecipes(output, itemMap);

        metalStream().forEach(metal -> {
            BLADE_HILT_RECIPES.forEach(r -> r.generate(output, metal, itemLookup));
            HEAD_GRIP_RECIPES.forEach(r -> r.generate(output, metal, itemLookup));
            generateMorningstarRecipe(output, metal, itemLookup);
            generateQuarterstaffRecipe(output, metal, itemLookup);
        });
    }

    private void generateHiltAssemblyRecipes(RecipeOutput output, Function<ResourceUtils.ItemVariant, Optional<Item>> itemLookup) {
        metalStream().forEach(metal -> {
            var hiltVariant = new ResourceUtils.ItemVariant(ComponentType.HILT, Optional.of(metal));
            var guard = itemLookup.apply(new ResourceUtils.ItemVariant(ComponentType.GUARD, Optional.of(metal)));
            var pommel = itemLookup.apply(new ResourceUtils.ItemVariant(ComponentType.POMMEL, Optional.of(metal)));
            var hilt = itemLookup.apply(hiltVariant);
            var grip = itemLookup.apply(new ResourceUtils.ItemVariant(ComponentType.GRIP, Optional.empty()));

            if (guard.isPresent() && pommel.isPresent() && hilt.isPresent() && grip.isPresent()) {
                CraftingRecipeBuilder.shaped(hilt.get(), 1)
                    .pattern(" P ").pattern(" G ").pattern(" R ")
                    .define('P', pommel.get()).define('G', guard.get()).define('R', grip.get())
                    .unlockedBy("has_grip", has(grip.get()))
                    .unlockedBy("has_guard", has(guard.get()))
                    .unlockedBy("has_pommel", has(pommel.get()))
                    .save(output, recipeId("crafting/" + hiltVariant.getRegistryPath() + "/assembly"));
            }
        });
    }

    private void generateMorningstarRecipe(RecipeOutput output, Metal metal, Function<ResourceUtils.ItemVariant, Optional<Item>> itemLookup) {
        var weaponVariant = new ResourceUtils.ItemVariant(WeaponType.MORNINGSTAR, Optional.of(metal));
        var head = itemLookup.apply(new ResourceUtils.ItemVariant(ComponentType.MORNINGSTAR_HEAD, Optional.of(metal)));
        var weapon = itemLookup.apply(weaponVariant);
        var grip = itemLookup.apply(new ResourceUtils.ItemVariant(ComponentType.GRIP, Optional.empty()));

        if (head.isPresent() && weapon.isPresent() && grip.isPresent()) {
            CraftingRecipeBuilder.shaped(weapon.get(), 1)
                .pattern("H").pattern("R")
                .define('H', head.get()).define('R', grip.get())
                .unlockedBy("has_head", has(head.get()))
                .save(output, recipeId("crafting/" + weaponVariant.getRegistryPath() + "/assembly"));
        }
    }

    private void generateQuarterstaffRecipe(RecipeOutput output, Metal metal, Function<ResourceUtils.ItemVariant, Optional<Item>> itemLookup) {
        var weaponVariant = new ResourceUtils.ItemVariant(WeaponType.QUARTERSTAFF, Optional.of(metal));
        var hilt = itemLookup.apply(new ResourceUtils.ItemVariant(ComponentType.HILT, Optional.of(metal)));
        var weapon = itemLookup.apply(weaponVariant);

        if (hilt.isPresent() && weapon.isPresent()) {
            CraftingRecipeBuilder.shaped(weapon.get(), 1)
                .pattern("  L").pattern(" H ").pattern("L  ")
                .define('L', TFCTags.Items.LUMBER).define('H', hilt.get())
                .unlockedBy("has_hilt", has(hilt.get()))
                .save(output, recipeId("crafting/" + weaponVariant.getRegistryPath() + "/assembly"));
        }
    }

    private void generateAnvilRecipes(RecipeOutput output, Map<String, ? extends DeferredHolder<Item, ? extends Item>> itemMap) {
        metalStream().forEach(metal ->
            ANVIL_SPECS.forEach(spec -> createAnvilRecipe(output, spec, metal, itemMap))
        );
    }

    private void createAnvilRecipe(RecipeOutput output, AnvilSpec spec, Metal metal, Map<String, ? extends DeferredHolder<Item, ? extends Item>> itemMap) {
        var itemVariant = new ResourceUtils.ItemVariant(spec.component(), Optional.of(metal));
        var resultItem = getItem(itemMap, itemVariant.getRegistryPath());

        if (resultItem == null) return;

        var ingredientTag = TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            ResourceLocation.parse(String.format(spec.ingredientTagFormat(), metal.getSerializedName())));

        var builder = CorrectedAnvilRecipeBuilder.working(resultItem, 1)
            .minTier(metal.tier())
            .input(Ingredient.of(ingredientTag))
            .applyForgingBonus();

        spec.rules().forEach(builder::rule);
            
        builder.save(output, recipeId("anvil/" + itemVariant.getRegistryPath()));
    }

    private static Stream<Metal> metalStream() {
        return Arrays.stream(Metal.values()).filter(TFCUtils::isValidMetal);
    }

    private static Item getItem(Map<String, ? extends DeferredHolder<Item, ? extends Item>> itemMap, String path) {
        DeferredHolder<Item, ? extends Item> holder = itemMap.get(path);
        return holder != null ? holder.get() : null;
    }

    private static ResourceLocation recipeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(TFCWeaponsPlus.MOD_ID, path);
    }

    private static class CorrectedAnvilRecipeBuilder extends AnvilRecipeBuilder {
        private final List<ForgeRule> rules = new ArrayList<>();
        private Ingredient input;
        private int minTier = 0;
        private boolean applyForgingBonus;

        protected CorrectedAnvilRecipeBuilder(String directory, ItemStackProvider output) {
            super(directory, output);
        }

        public static CorrectedAnvilRecipeBuilder working(ItemLike item, int count) {
            return new CorrectedAnvilRecipeBuilder("anvil", ItemStackProvider.of(new ItemStack(item, count)));
        }

        public CorrectedAnvilRecipeBuilder input(Ingredient input) {
            this.input = input;
            return this;
        }

        public CorrectedAnvilRecipeBuilder minTier(int minTier) {
            this.minTier = minTier;
            return this;
        }

        public CorrectedAnvilRecipeBuilder rule(ForgeRule rule) {
            this.rules.add(rule);
            return this;
        }

        public CorrectedAnvilRecipeBuilder applyForgingBonus() {
            this.applyForgingBonus = true;
            return this;
        }

        @Override
        protected void ensureValid(@NotNull ResourceLocation id) {
            if (!ForgeRule.isConsistent(rules)) {
                throw new IllegalStateException(id + " rules " + rules + " cannot be satisfied by any combination of steps!");
            }
            if (input == null) throw new IllegalStateException(id + " input not set");
        }

        @Override
        protected Recipe<?> recipe() {
            return new AnvilRecipe(input, minTier, rules, applyForgingBonus, output);
        }
    }
}
