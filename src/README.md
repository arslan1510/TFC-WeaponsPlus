# TFC Weapons Plus - Code Documentation

## Overview

**TFC Weapons Plus** is a TerraFirmaCraft addon for Minecraft 1.21.1 that adds modular weapon crafting with full TFC metal system integration.

## Architecture

### Core Design Patterns

**1. Centralized Registry Pattern**

`WeaponRegistry` is the single source of truth for all weapon/component metadata:

```java
public record ItemDef(
    String type,
    String pathPrefix,
    Function<String, Item> itemGetter,
    Size size,
    Weight weight,
    int heatUnits,
    boolean metalItem
) {}
```

**Benefits:** Eliminates hardcoded strings, easy to extend, consistent data access.

**2. Factory Pattern**

`WeaponItemFactory` creates weapons via switch expression:

```java
public static Item createWeapon(String weaponType, String metal, Item.Properties props) {
    return switch (weaponType) {
        case "longsword" -> new WeaponItem(metal, props,
            (t, m) -> WeaponAttributes.createAttributes(t, m, "longsword"));
        case "greataxe" -> new GreatAxeItem(metal, props);
        // ...
    };
}
```

**3. Data-Driven Providers**

All data generators use record-based specs to eliminate duplication:

```java
private record ComponentModel(String type, Function<String, Item> getter) {}
private static final List<ComponentModel> COMPONENT_MODELS = List.of(...);
```

**4. Unified Component Class**

`ComponentItem` replaces multiple wrapper classes - single class for all components.

---

## Package Structure

```
com.concinnity.tfc_weapons_plus/
├── TFCWeaponsPlus.java          # Main mod entry point
├── providers/                   # Data generation (recipes, models, tags, lang)
├── item/
│   ├── ModItems.java            # Central item registry
│   ├── ComponentType.java       # Sealed interface for component types
│   ├── ComponentItem.java       # Unified component class
│   └── weapon/
│       ├── WeaponItem.java      # Generic weapon class
│       ├── WeaponItemFactory.java
│       ├── WeaponAttributes.java
│       ├── GreatAxeItem.java
│       └── GreatHammerItem.java
└── util/
    ├── WeaponRegistry.java      # Central metadata registry
    ├── WeaponType.java          # Weapon type enum
    ├── MetalData.java           # Metal properties
    └── NameUtils.java           # Name normalization
```

---

## Core Components

### ModItems

Central registry using data-driven `ItemDef` records:

```java
private static final List<ItemDef> DEFINITIONS = List.of(
    new ItemDef("guard", "metal/guard", false,
        (metal, props) -> new ComponentItem(ComponentType.GUARD, metal.getSerializedName(), props)),
    new ItemDef("longsword", "metal/longsword", true,
        (metal, props) -> WeaponItemFactory.createWeapon("longsword", metal.getSerializedName(), props))
);
```

All items auto-generate for all TFC metals via `metals().forEach(...)`.

### WeaponRegistry

Single source of truth with lazy-initialized item definitions:

```java
// Get all metal items
WeaponRegistry.metalItems().forEach(def -> { /* ... */ });

// Get weapon-specific stats
WeaponStatsMeta stats = WeaponRegistry.getWeaponStats("longsword");

// Tag groupings
WeaponRegistry.SWORDS  // Set.of("longsword", "greatsword", "shortsword")
WeaponRegistry.SLASHING  // Set.of("longsword", "greatsword", "shortsword", "greataxe")
```

### WeaponAttributes

Consolidated attribute calculation with exponential tier scaling:

```java
public static ItemAttributeModifiers createAttributes(Tier tier, String metalName, String weaponType) {
    // Exponential drop-off: pow(tierBonus, 0.6) prevents overpowered high-tier weapons
    // Weight-based damage/speed calculations from WeaponRegistry stats
    return WeaponStats.from(tier, metalName, weaponType).toModifiers();
}
```

**Damage Formula:**
- Exponential tier bonus: `pow(tierBonus, 0.6) * 1.5`
- Weight scaling: `baseDamage * (1.0 + (weight - 1.0) * 0.3) + scaledTierBonus`
- Prevents blue steel weapons from becoming too powerful

### MetalData

Provides metal properties from TFC's Metal enum:

```java
MetalData.stream()  // Stream<Metal> of all supported metals
MetalData.names()   // Stream<String> of metal names
MetalData.dataProps(metal)  // MetalProperties record
```

Auto-supports any TFC metal with `tier() > 0` and `allParts() == true`.

---

## Data Generation

All providers use `WeaponRegistry` for centralized data:

**Key Providers:**
- `ModItemModelProvider` - Item models
- `TFCItemSizeProvider` - TFC size/weight data
- `TFCItemHeatProvider` - TFC heating recipes
- `TFCAnvilRecipeProvider` - Forging recipes and melting
- `ModRecipeProvider` - Crafting recipes
- `ModItemTagsProvider` - Item tags
- `ModLanguageProvider` - Translations

**Melting Values** (matches TFC pattern):
- Longsword: 200mb (2 ingots, same as TFC sword)
- Greatsword: 300mb (3 ingots)
- Greathammer: 400mb (4 ingots)
- Components: 50mb each (0.5 ingots)

**Size/Weight** (relative to TFC swords):
- TFC swords: VERY_LARGE + VERY_HEAVY
- Longsword: VERY_LARGE + VERY_HEAVY (matches TFC)
- Greatsword/Greathammer: HUGE + VERY_HEAVY (largest)
- Shortsword: LARGE + HEAVY (smaller)

---

## TFC Integration

**Metal System:**
- Uses TFC's `Metal` enum and `FluidHeat` data
- Melting points from TFC fluid definitions
- Tier system matches TFC progression (1-6)

**Heating System:**
- All weapons/components register with TFC's heat system
- Heat capacity from `WeaponRegistry` metadata
- Weapons show heat tooltips and can be melted

**Tags:**
- `tfc:deals_slashing_damage` - Slashing weapons
- `tfc:deals_crushing_damage` - Crushing weapons
- `tfc:deals_piercing_damage` - Piercing weapons
- `tfc:usable_on_tool_rack` - All weapons
- `tfc:tools/{metal}` - Metal-specific tool tags

---

## Extending the Mod

### Adding a New Weapon

1. **Add to WeaponRegistry metadata:**
```java
// METADATA map
Map.entry("newweapon", new Meta(Size.VERY_LARGE, Weight.VERY_HEAVY, 200))
```

2. **Add to WeaponType enum:**
```java
NEWWEAPON("newweapon", "metal/newweapon", 1.0f, 4.0f, -2.6f, Size.VERY_LARGE, Weight.VERY_HEAVY, 200, true)
```

3. **Register in ModItems:**
```java
new ItemDef("newweapon", "metal/newweapon", true,
    (metal, props) -> WeaponItemFactory.createWeapon("newweapon", metal.getSerializedName(), props))
```

4. **Update factory if needed:**
```java
// In WeaponItemFactory
case "newweapon" -> new WeaponItem(metal, properties,
    (t, m) -> WeaponAttributes.createAttributes(t, m, "newweapon"));
```

5. **Add to data provider specs** (models, recipes, translations)

### Adding a New Metal

**No code changes needed!** The mod auto-supports any TFC metal with `tier() > 0` and `allParts() == true`.

### Adding a New Component

1. Add to `ComponentType` sealed interface
2. Register in `ModItems.DEFINITIONS`
3. Add to `WeaponRegistry.METADATA`
4. Update data provider specs

---

## Code Practices

**Java 21 Features:**
- Sealed interfaces for type safety
- Records for immutable data
- Pattern matching in switch expressions
- Direct Item returns (no Optional overhead)

**Key Principles:**
- Single Responsibility: Each class has one clear purpose
- DRY: Eliminate duplication through consolidation
- Data-Driven: Use specs and registries over hardcoding
- Immutable Data: Final fields and records
- No Null Returns: Throw exceptions for missing items

**Resource Naming:**
- Pattern: `{namespace}:{category}/{type}/{metal}`
- Example: `tfc_weapons_plus:metal/longsword/steel`

---

## Build Commands

```bash
# Run data generation
./gradlew runData

# Build mod
./gradlew build

# Run client (testing)
./gradlew runClient
```

Generated files: `src/generated/resources/`

---

## Key Files to Know

**Core:**
- `TFCWeaponsPlus.java` - Main entry point
- `ModItems.java` - Item registration
- `WeaponRegistry.java` - Central metadata

**Data Generation:**
- `DataGenerators.java` - Coordinates all providers
- `TFCAnvilRecipeProvider.java` - Forging and melting recipes

**Utilities:**
- `MetalData.java` - Metal properties
- `WeaponType.java` - Weapon type enum with stats
- `NameUtils.java` - Name normalization

---

## Quick Reference

**Get an item:**
```java
Item sword = ModItems.getLongswordForMetal("steel");
```

**Iterate all weapons:**
```java
WeaponRegistry.weapons().forEach(def -> {
    Item weapon = def.item("steel");
});
```

**Get weapon stats:**
```java
WeaponStatsMeta stats = WeaponRegistry.getWeaponStats("longsword");
// stats.baseDamage(), stats.baseWeight(), stats.baseSpeed()
```

**Metal properties:**
```java
MetalData.Props props = MetalData.dataProps(Metal.STEEL);
// props.name(), props.tier(), props.meltingPoint()
```
