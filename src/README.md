# TFC Weapons Plus - Code Structure Documentation

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Package Structure](#package-structure)
4. [Design Patterns](#design-patterns)
5. [Core Components](#core-components)
6. [Data Generation System](#data-generation-system)
7. [TFC Integration](#tfc-integration)
8. [Code Practices](#code-practices)
9. [Extension Points](#extension-points)

---

## Overview

**TFC Weapons Plus** is a TerraFirmaCraft addon for Minecraft 1.21.1 that adds modular weapon crafting. The mod follows modern Java practices, uses functional programming patterns, and integrates deeply with TFC's metal system.

### Key Features

- **Modular Weapon System**: Weapons are assembled from components (blades, hilts, guards, pommels, heads)
- **Metal Variants**: All weapons support 9 TFC metals (copper through red/blue steel)
- **TFC Integration**: Full integration with TFC's heating, forging, and metal systems
- **Data-Driven**: Extensive use of NeoForge data generation for maintainability
- **Modern Java**: Uses Java 21 features including sealed interfaces, records, pattern matching, and switch expressions
- **Consolidated Architecture**: Centralized registries and factory patterns reduce code duplication

---

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    TFCWeaponsPlus (Main Entry)              │
│  - Mod initialization                                       │
│  - Event bus registration                                   │
│  - Creative tab setup                                       │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼────────┐      ┌────────▼──────────┐
│   ModItems     │      │  WeaponRegistry   │
│  (Registry)    │      │  (Central Data)   │
└───────┬────────┘      └────────┬──────────┘
        │                        │
        │                        │
┌───────▼────────────────────────▼──────────┐
│         Item Classes                       │
│  - ComponentItem (all components)          │
│  - WeaponItem (sword-type weapons)         │
│  - GreatAxeItem, GreatHammerItem          │
│  - WeaponItemFactory (creation)            │
└───────┬────────────────────────────────────┘
        │
┌───────▼────────────────────────────────────┐
│      Data Generation System                 │
│  - Data-driven providers                    │
│  - Recipe specs                             │
│  - Model specs                              │
│  - Translation specs                        │
└────────────────────────────────────────────┘
```

---

## Package Structure

```
com.concinnity.tfc_weapons_plus/
├── TFCWeaponsPlus.java          # Main mod class, entry point
│
├── providers/                   # Data generation providers
│   ├── DataGenerators.java     # Main data generator coordinator
│   ├── TFCItemHeatProvider.java # Generates TFC item_heat JSON files
│   ├── TFCItemSizeProvider.java # Generates TFC item_size JSON files
│   ├── ModItemTagsProvider.java # Generates item tag JSON files
│   ├── ModItemModelProvider.java# Generates item model JSON files
│   ├── ModLanguageProvider.java # Generates language files
│   ├── ModRecipeProvider.java   # Generates crafting recipes
│   └── TFCAnvilRecipeProvider.java # Generates TFC anvil recipes
│
├── item/                        # Item classes and registration
│   ├── ModItems.java           # Central item registry (data-driven)
│   ├── ComponentType.java       # Sealed interface for component types
│   ├── ComponentItem.java       # Unified component item class
│   └── weapon/                 # Weapon item classes
│       ├── WeaponItem.java      # Generic sword-type weapon
│       ├── WeaponItemFactory.java # Factory for weapon creation
│       ├── WeaponAttributes.java # Weapon attribute calculations
│       ├── WeaponTiers.java      # Tier definitions
│       ├── GreatAxeItem.java     # Axe-type weapon
│       └── GreatHammerItem.java  # Hammer-type weapon
│
└── util/                        # Utility classes
    ├── WeaponRegistry.java      # Central registry for all weapon/component data
    ├── MetalData.java           # Metal properties and metadata
    ├── NameUtils.java           # Name normalization utilities
    ├── ItemHeatHelper.java      # Item heat calculation helpers
    └── data/                    # Data generation utilities
        └── FluidHeatData.java   # Fluid heat data with lookup
```

---

## Design Patterns

### 1. **Registry Pattern**

The mod uses NeoForge's `DeferredRegister` pattern for safe item registration:

```java
public static final DeferredRegister.Items ITEMS =
    DeferredRegister.createItems(TFCWeaponsPlus.MOD_ID);

static {
    registerItems();  // Ensures items are registered immediately on class load
}
```

**Benefits:**
- Thread-safe registration
- Lazy initialization
- Type-safe item access
- Static initializer ensures registration before data generation

### 2. **Centralized Registry Pattern**

**WeaponRegistry** provides a single source of truth for all weapon and component definitions:

```java
public final class WeaponRegistry {
    public record ItemDef(
        String type,
        String pathPrefix,
        Function<String, Item> itemGetter,  // Direct Item returns, no Optional
        Size size,
        Weight weight,
        int heatUnits,
        boolean metalItem
    ) {}

    // Centralized metadata
    private static final Map<String, Meta> METADATA = ...;
    private static final EnumMap<WeaponType, WeaponStatsMeta> WEAPON_STATS = ...;
}
```

**Benefits:**
- Single source of truth for all item properties
- Eliminates hardcoded strings across providers
- Direct item access without Optional overhead
- Easy to extend with new items

### 3. **Factory Pattern**

**WeaponItemFactory** creates weapon items based on type:

```java
public static Item createWeapon(String weaponType, String metal, Item.Properties properties) {
    return switch (weaponType) {
        case "longsword" -> new WeaponItem(metal, properties,
            (t, m) -> WeaponAttributes.createAttributes(t, m, "longsword"));
        case "greataxe" -> new GreatAxeItem(metal, properties);
        // ...
    };
}
```

**ModItems** uses a data-driven factory approach:

```java
private static final List<ItemDef> DEFINITIONS = List.of(
    new ItemDef("longsword", "metal/longsword", true,
        (metal, props) -> WeaponItemFactory.createWeapon("longsword", metal.getSerializedName(), props)),
    // ...
);
```

**Benefits:**
- Centralized creation logic
- Consolidated attribute creation (single method for all weapon types)
- Easy to add new weapon types
- Type-safe weapon creation

### 4. **Unified Component Pattern**

**ComponentItem** replaces multiple component classes:

```java
public class ComponentItem extends Item {
    private final ComponentType componentType;
    private final String materialName;
    
    public ComponentItem(ComponentType componentType, String materialName, Properties properties) {
        super(properties);
        this.componentType = componentType;
        this.materialName = materialName;
    }
}
```

**Benefits:**
- Single class for all components
- Eliminates redundant wrapper classes
- Easier maintenance

### 5. **Data-Driven Provider Pattern**

All data providers use data-driven specs to eliminate repetitive code:

**ModRecipeProvider:**
```java
private record BladeHiltRecipe(
    String weaponType,
    Function<String, Optional<Item>> bladeGetter,
    Function<String, Optional<Item>> weaponGetter
) {
    void generate(RecipeOutput output, String metalName) { /* ... */ }
}

private static final List<BladeHiltRecipe> BLADE_HILT_RECIPES = List.of(
    new BladeHiltRecipe("longsword", ModItems::getLongswordBladeForMetal, ModItems::getLongswordForMetal),
    // ...
);
```

**ModItemModelProvider:**
```java
private record ComponentModel(String type, Function<String, Item> getter) {}
private static final List<ComponentModel> COMPONENT_MODELS = List.of(...);
```

**Benefits:**
- Reduced code duplication (~50% reduction in providers)
- Easy to add new items (just add to spec list)
- Consistent generation patterns

### 6. **Sealed Interface Pattern**

**ComponentType** uses Java 21 sealed interfaces for type-safe component definitions:

```java
public sealed interface ComponentType 
    permits ComponentType.Grip, ComponentType.Guard, ComponentType.Pommel, ... {
    String name();
    
    record Grip() implements ComponentType {
        @Override public String name() { return "grip"; }
    }
    // ... other records
}
```

**Benefits:**
- Compile-time exhaustiveness checking
- Type safety
- Clear component hierarchy

### 7. **Strategy Pattern**

Weapon attributes are calculated using a strategy based on metal density and weapon weight:

```java
public static ItemAttributeModifiers createWeightBasedAttributes(
    Tier tier, String metalName, float baseWeight, float baseDamage, float baseSpeed) {
    // Strategy: weight-based damage, metal density affects damage, weight affects speed
}
```

---

## Core Components

### TFCWeaponsPlus (Main Entry Point)

**Location**: `com.concinnity.tfc_weapons_plus.TFCWeaponsPlus`

**Responsibilities:**
- Mod initialization and lifecycle management
- Event bus registration
- Creative tab setup

**Key Methods:**
- `TFCWeaponsPlus(IEventBus)` - Constructor, registers all systems
- `commonSetup(FMLCommonSetupEvent)` - Initializes systems
- `addCreative(BuildCreativeModeTabContentsEvent)` - Adds items to creative tabs

**Design Notes:**
- Uses `@Mod` annotation for NeoForge mod discovery
- Registers event listeners in constructor
- Uses `DeferredRegister` for creative tabs

### ModItems (Item Registry)

**Location**: `com.concinnity.tfc_weapons_plus.item.ModItems`

**Responsibilities:**
- Central registry for all mod items
- Data-driven item registration
- Metal variant management
- Item lookup methods

**Key Features:**
- **Data-Driven Registration**: Uses `ItemDef` records to define all items
- **Factory Pattern**: Uses `WeaponItemFactory` for weapon creation
- **Unified Components**: All components use `ComponentItem`
- **Map-Based Storage**: Uses `Map<String, Map<String, DeferredItem<Item>>>` for metal variants

**Registration Pattern:**
```java
private static final List<ItemDef> DEFINITIONS = List.of(
    new ItemDef("guard", "metal/guard", false, 
        (metal, props) -> new ComponentItem(ComponentType.GUARD, metal.getSerializedName(), new Item.Properties())),
    new ItemDef("longsword", "metal/longsword", true, 
        (metal, props) -> WeaponItemFactory.createWeapon("longsword", metal.getSerializedName(), props)),
    // ...
);

public static void registerItems() {
    metals().forEach(metal -> {
        DEFINITIONS.forEach(def -> {
            // Register item using factory
        });
    });
}
```

**Item Categories:**
1. **Components**: Guard, Pommel, Hilt, Grip (all use `ComponentItem`)
2. **Blades/Heads**: LongswordBlade, GreatswordBlade, ShortswordBlade, GreatAxeHead, GreatHammerHead, MorningstarHead (all use `ComponentItem`)
3. **Complete Weapons**: Longsword, Greatsword, Shortsword, GreatAxe, GreatHammer, Morningstar, Quarterstaff (created via `WeaponItemFactory`)

### WeaponRegistry (Central Data Registry)

**Location**: `com.concinnity.tfc_weapons_plus.util.WeaponRegistry`

**Purpose**: Single source of truth for all weapon and component metadata.

**Key Features:**
- **Item Definitions**: Combines `ModItems.definitions()` with metadata
- **Size/Weight Data**: Centralized size and weight definitions
- **Heat Units**: Centralized heat unit definitions
- **Weapon Stats**: Base weight, damage, and speed for each weapon type
- **Tag Groupings**: Pre-defined groups for tags (SWORDS, AXES, SLASHING, etc.)

**Structure:**
```java
public record ItemDef(
    String type,
    String pathPrefix,
    Function<String, Item> itemGetter,  // Direct Item returns
    Size size,
    Weight weight,
    int heatUnits,
    boolean metalItem
) {}

public record WeaponStatsMeta(float baseWeight, float baseDamage, float baseSpeed) {}
```

**Usage:**
```java
// Get all items (metal items only)
WeaponRegistry.metalItems().forEach(def -> { /* ... */ });

// Get weapon stats by WeaponType enum
WeaponStats stats = WeaponRegistry.getWeaponStats(WeaponType.LONGSWORD);

// Get tag groupings
WeaponRegistry.SWORDS.forEach(/* ... */);
```

### WeaponItem (Generic Weapon Class)

**Location**: `com.concinnity.tfc_weapons_plus.item.weapon.WeaponItem`

**Purpose**: Base class for all sword-type weapons, replaces individual weapon classes.

**Design:**
- Extends `SwordItem` (Minecraft base class)
- Takes a `BiFunction<Tier, String, ItemAttributeModifiers>` for attribute creation
- Handles tier creation and attribute application

**Usage:**
```java
// Created via WeaponItemFactory with lambda for weapon-specific attributes
new WeaponItem(metal, properties, (t, m) -> WeaponAttributes.createAttributes(t, m, "longsword"));
```

**Replaced Classes:**
- `LongswordItem` → `WeaponItem` via factory
- `GreatswordItem` → `WeaponItem` via factory
- `ShortswordItem` → `WeaponItem` via factory
- `MorningstarItem` → `WeaponItem` via factory
- `QuarterstaffItem` → `WeaponItem` via factory

### WeaponItemFactory

**Location**: `com.concinnity.tfc_weapons_plus.item.weapon.WeaponItemFactory`

**Purpose**: Centralized factory for creating all weapon items.

**Methods:**
```java
public static Item createWeapon(String weaponType, String metal, Item.Properties properties)
```

**Supported Types:**
- `longsword`, `greatsword`, `shortsword` → `WeaponItem`
- `greataxe` → `GreatAxeItem`
- `greathammer` → `GreatHammerItem`
- `morningstar`, `quarterstaff` → `WeaponItem`

### ComponentItem (Unified Component Class)

**Location**: `com.concinnity.tfc_weapons_plus.item.ComponentItem`

**Purpose**: Single class for all weapon components, replaces multiple wrapper classes.

**Replaced Classes:**
- `GripItem` → `ComponentItem`
- `HiltItem` → `ComponentItem`
- `MetalComponentItem` → `ComponentItem`
- `SwordComponentItem` → `ComponentItem` (renamed)

**Usage:**
```java
// Wood grip
new ComponentItem(ComponentType.GRIP, "", new Item.Properties())

// Metal component
new ComponentItem(ComponentType.GUARD, metalName, new Item.Properties())
```

### WeaponAttributes

**Location**: `com.concinnity.tfc_weapons_plus.item.weapon.WeaponAttributes`

**Purpose**: Calculates weapon attack damage and speed based on metal properties and weapon type using a consolidated method.

**Key Method:**
```java
public static ItemAttributeModifiers createAttributes(Tier tier, String metalName, String weaponType) {
    return WeaponStats.from(tier, metalName, weaponType).toModifiers();
}
```

**Key Concepts:**

1. **Metal Density Multiplier**: Higher-tier metals are denser, affecting damage
   - Copper: 0.9x (lightest)
   - Bronze: 1.0x (baseline)
   - Steel: 1.2x
   - Blue/Red Steel: 1.4x (heaviest)

2. **Weight-Based Damage**: Heavier weapons deal more damage
   ```java
   damage = baseDamage * (1.0 + (totalWeight - 1.0) * 2.0) + tierBonus
   ```

3. **Weight-Based Speed**: Heavier weapons are slower
   ```java
   speed = baseSpeed - (baseWeight - 1.0) * 1.0
   ```

**Design:**
- Replaced 7 individual weapon-specific methods with single generic method
- Uses weapon type string parameter to look up base stats from `WeaponRegistry`
- Reduces code duplication and makes adding new weapons easier

**Weapon Base Stats** (from `WeaponRegistry`):
- Longsword: weight 1.0, damage 3.0, speed -2.4
- Greatsword: weight 1.5, damage 5.0, speed -2.8
- Shortsword: weight 0.8, damage 2.5, speed -2.0
- GreatAxe: weight 1.6, damage 5.5, speed -2.9
- GreatHammer: weight 1.8, damage 6.0, speed -3.1
- Morningstar: weight 1.7, damage 5.8, speed -3.0
- Quarterstaff: weight 0.9, damage 3.0, speed -2.3

**Data Source:**
- Base stats retrieved from `WeaponRegistry.getWeaponStats()`
- Metal density multipliers from `WeaponRegistry.getMetalDensityMultiplier()`

### ComponentType

**Location**: `com.concinnity.tfc_weapons_plus.item.ComponentType`

**Purpose**: Type-safe component type definitions using sealed interfaces.

**Structure:**
- Sealed interface with 10 permitted record implementations
- Each record returns a normalized name string
- Constants for easy access (GRIP, GUARD, POMMEL, etc.)

**Benefits:**
- Compile-time exhaustiveness checking in switch expressions
- Type safety
- Clear component hierarchy

### MetalData

**Location**: `com.concinnity.tfc_weapons_plus.util.MetalData`

**Purpose**: Provides metal properties and metadata. Supports both runtime and datagen-safe access patterns.

**MetalProperties Record:**
```java
public record MetalProperties(
    String name,           // Display name
    int durability,        // Tool durability
    float efficiency,      // Mining speed
    float attackDamage,    // Base attack damage bonus
    int tier,              // Metal tier (1-6)
    int meltingPoint       // Melting temperature in Celsius
) {}
```

**Supported Metals:**
The mod automatically supports all TFC metals that have tool tiers and can make all parts. This includes:
1. Copper (Tier 1, 1083°C)
2. Bronze (Tier 2, 950°C)
3. Bismuth Bronze (Tier 2, 985°C)
4. Black Bronze (Tier 2, 1070°C)
5. Wrought Iron (Tier 3, 1535°C)
6. Steel (Tier 4, 1540°C)
7. Black Steel (Tier 5, 1485°C)
8. Blue Steel (Tier 6, 1540°C)
9. Red Steel (Tier 6, 1540°C)

**Key Methods:**
- `stream()` - Returns Stream<Metal> of all supported metals
- `names()` - Returns Stream<String> of all metal names
- `dataProps(Metal)` - Returns MetalProperties for a metal

### FluidHeatData

**Location**: `com.concinnity.tfc_weapons_plus.util.data.FluidHeatData`

**Purpose**: Centralized FluidHeat data with lookup method.

**Key Features:**
- **Static Constants**: All FluidHeat instances as constants
- **Map Lookup**: `METAL_TO_FLUID_HEAT` map for O(1) lookup
- **Lookup Method**: `getFluidHeat(Metal)` for easy access

**Usage:**
```java
FluidHeat heat = FluidHeatData.getFluidHeat(metal);
```

**Benefits:**
- Single source of truth for FluidHeat data
- Eliminates duplicate switch statements
- Easy to extend with new metals

### NameUtils

**Location**: `com.concinnity.tfc_weapons_plus.util.NameUtils`

**Purpose**: Normalizes names for use in resource paths.

**Methods:**
- `normalize(String)` - Core normalization (lowercase, replace spaces/hyphens with underscores)
- `normalizeMetalName(String)` - Wrapper for metal names
- `normalizeWoodName(String)` - Wrapper for wood names

**Example:**
- "Wrought Iron" → "wrought_iron"
- "Black-Bronze" → "black_bronze"

---

## Data Generation System

The mod uses NeoForge's data generation framework to create JSON files at build time.

### DataGenerators

**Location**: `com.concinnity.tfc_weapons_plus.DataGenerators`

**Purpose**: Coordinates all data providers.

**Providers:**
1. **ModItemModelProvider** (Client) - Item model JSON files
2. **TFCItemSizeProvider** (Server) - TFC item_size JSON files
3. **TFCItemHeatProvider** (Server) - TFC item_heat JSON files
4. **ModRecipeProvider** (Server) - Crafting recipe JSON files
5. **TFCAnvilRecipeProvider** (Server) - TFC anvil recipe JSON files
6. **ModItemTagsProvider** (Server) - Item tag JSON files
7. **ModLanguageProvider** (Client) - Language file JSON

**Design:**
- All providers use `WeaponRegistry` for centralized data
- Data-driven specs eliminate repetitive code
- Consistent generation patterns across all providers

### ModRecipeProvider

**Location**: `com.concinnity.tfc_weapons_plus.providers.ModRecipeProvider`

**Purpose**: Generates crafting recipes using data-driven specs.

**Key Features:**
- **Recipe Specs**: Uses record-based specs for recipe patterns
- **BladeHiltRecipe**: For sword-type weapons (longsword, greatsword, shortsword)
- **HeadGripRecipe**: For axe/hammer weapons (greataxe, greathammer)
- **Single Loop**: Generates all recipes from specs

**Recipe Types:**
1. **Hilt Assembly**: Grip + Guard + Pommel → Hilt
2. **Sword Assembly**: Hilt + TFC Sword Blade → TFC Sword
3. **Weapon Assembly**: Blade/Head + Hilt/Grip → Complete Weapon

### ModItemModelProvider

**Location**: `com.concinnity.tfc_weapons_plus.providers.ModItemModelProvider`

**Purpose**: Generates item models using data-driven specs.

**Key Features:**
- **Model Specs**: Uses record-based specs for model generation
- **ComponentModel**: For all component types
- **WeaponModel**: For all weapon types
- **Single Loop**: Generates all models from specs

### ModLanguageProvider

**Location**: `com.concinnity.tfc_weapons_plus.providers.ModLanguageProvider`

**Purpose**: Generates language translations using data-driven specs.

**Key Features:**
- **Translation Specs**: Uses record-based specs for translations
- **Component Translations**: For all component types
- **Weapon Translations**: For all weapon types
- **Single Loop**: Generates all translations from specs

### TFCItemHeatProvider

**Location**: `com.concinnity.tfc_weapons_plus.providers.TFCItemHeatProvider`

**Purpose**: Generates TFC `item_heat` JSON files for all weapons and components.

**Key Features:**
- Uses `WeaponRegistry` for item iteration
- Uses `FluidHeatData.getFluidHeat()` for heat data
- Uses `ItemHeatHelper` for heat calculations

**Heat Capacity Values** (from `WeaponRegistry`):
- Components (guard, pommel, hilt): 3 units
- Shortsword blade: 3 units
- Longsword blade: 5 units
- Greatsword blade: 10 units
- Greataxe head: 5 units
- Greathammer head: 10 units
- Morningstar head: 3 units
- Complete weapons: Sum of component heat capacities

### TFCItemSizeProvider

**Location**: `com.concinnity.tfc_weapons_plus.providers.TFCItemSizeProvider`

**Purpose**: Generates TFC `item_size` JSON files for inventory management.

**Key Features:**
- Uses `WeaponRegistry.all()` for item iteration
- Size and weight from `WeaponRegistry` metadata

**Size Categories:**
- **SMALL**: Components (guard, pommel, hilt, grip)
- **NORMAL**: Blades, heads, shortswords, morningstars, quarterstaffs
- **LARGE**: Complete longswords, greatswords, greataxes, greathammers

**Weight Categories:**
- **LIGHT**: Components
- **MEDIUM**: Blades, shortswords
- **HEAVY**: Longswords, greatswords, morningstars, quarterstaffs
- **VERY_HEAVY**: Greataxes, greathammers

### ModItemTagsProvider

**Location**: `com.concinnity.tfc_weapons_plus.providers.ModItemTagsProvider`

**Purpose**: Generates item tag JSON files.

**Key Features:**
- Uses `WeaponRegistry` tag groupings (SWORDS, AXES, SLASHING, etc.)
- Centralized tag definitions

**Tags Generated:**
1. `c:tools` - All weapons (Common Tags)
2. `minecraft:swords` - Sword-type weapons (from `WeaponRegistry.SWORDS`)
3. `minecraft:axes` - Axe-type weapons (from `WeaponRegistry.AXES`)
4. `tfc:tools/{metal}` - Metal-specific tool tags for each metal
5. `tfc:deals_slashing_damage` - Slashing weapons (from `WeaponRegistry.SLASHING`)
6. `tfc:deals_crushing_damage` - Crushing weapons (from `WeaponRegistry.CRUSHING`)
7. `tfc:deals_piercing_damage` - Piercing weapons (from `WeaponRegistry.PIERCING`)
8. `tfc:usable_on_tool_rack` - All weapons

### TFCAnvilRecipeProvider

**Location**: `com.concinnity.tfc_weapons_plus.providers.TFCAnvilRecipeProvider`

**Purpose**: Generates TFC anvil recipe JSON files for blacksmithing components and weapon parts.

**Key Features:**
- Uses `AnvilSpec` and `HeatingSpec` records for recipe definitions
- Uses `WeaponRegistry` for item iteration
- Data-driven approach eliminates repetitive code

**Recipe Types:**
1. **Component Recipes** (guard, pommel, hilt, blades): From ingots/sheets with tier requirements
2. **Head Recipes** (greataxe, greathammer, morningstar): From sheets/ingots with tier requirements
3. **Heating Recipes**: All components and weapons can be melted back into metal fluid

---

## TFC Integration

### Integration Architecture

```
TFCWeaponsPlus
    └── commonSetup()
        └── Initialize systems
            ├── ModItems.registerItems()
            └── WeaponRegistry (static initialization)
```

### Metal Properties Integration

Metal properties are dynamically loaded from TFC's `Metal` enum and `LevelTier` system:
- **Runtime**: Melting points from TFC `FluidHeat` data (via `FluidHeatData`)
- Durability and efficiency from TFC `LevelTier` tool properties
- Tier system matches TFC progression (1-6)
- Only metals with `tier() > 0` and `allParts() == true` are supported

### Heat System Integration

All weapons, blades, and heads are registered with TFC's heat system:
- Heat capacity from `WeaponRegistry` metadata
- Forging/welding temperatures from `FluidHeatData`
- Heat units calculated via `ItemHeatHelper`

This allows:
- Weapons to show heat tooltips
- Weapons to be melted in crucibles
- Weapons to be heated for forging

---

## Code Practices

### Java Version and Features

**Java 21 Features Used:**
- **Sealed Interfaces**: `ComponentType` uses sealed interface with records
- **Records**: `MetalProperties`, `ItemDef`, `WeaponStatsMeta`, recipe specs
- **Pattern Matching**: Switch expressions with pattern matching
- **Records in Switch**: Pattern matching on records

### Functional Programming

**Streams:**
```java
WeaponRegistry.metalItems()
    .map(def -> def.item(metalName))  // Direct Item access
    .forEach(/* ... */);
```

**Direct Item Access:**
```java
Item weapon = ModItems.getLongswordForMetal(metalName);  // Returns Item directly
// No Optional overhead
```

**Lambdas and Method References:**
```java
new WeaponItem(metal, properties, (t, m) -> WeaponAttributes.createAttributes(t, m, "longsword"));
```

### Code Organization

1. **Package by Feature**: Items, providers, util are separate packages
2. **Single Responsibility**: Each class has one clear purpose
3. **Immutable Data**: Records and final fields where possible
4. **No Null Returns**: Methods throw exceptions for missing items instead of returning null
5. **Data-Driven**: Use specs and registries instead of hardcoding
6. **DRY Principle**: Eliminate code duplication through consolidation
7. **Performance**: Direct item access without Optional overhead, EnumMap for fast lookups

### Naming Conventions

- **Classes**: PascalCase (e.g., `TFCWeaponsPlus`)
- **Methods**: camelCase (e.g., `getMetalProperties`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MOD_ID`)
- **Fields**: camelCase (e.g., `metalName`)
- **Packages**: lowercase (e.g., `com.concinnity.tfc_weapons_plus`)

### Error Handling

- **Direct Returns**: Methods return items directly and throw IllegalArgumentException for missing items
- **Exception Handling**: Try-catch in integration layer with proper logging
- **Validation**: Item registry populated via static initializer to ensure availability during data generation
- **Logging**: Use SLF4J logger with appropriate log levels

### Resource Naming

**Pattern**: `{namespace}:{category}/{type}/{metal}`

**Examples:**
- `tfc_weapons_plus:metal/longsword/steel`
- `tfc_weapons_plus:metal/guard/copper`
- `tfc_weapons_plus:wood/grip`

**Categories:**
- `metal/` - Metal-based items
- `wood/` - Wood-based items

---

## Extension Points

### Adding a New Weapon Type

1. **Add to WeaponRegistry:**
```java
// In WEAPON_STATS map
Map.entry("newweapon", new WeaponStatsMeta(1.2f, 4.0f, -2.5f))

// In METADATA map
Map.entry("newweapon", new Meta(Size.LARGE, Weight.HEAVY, 10))
```

2. **Add to WeaponItemFactory:**
```java
// WeaponAttributes.createAttributes() handles all weapon types automatically
case "newweapon" -> new WeaponItem(metal, properties,
    (t, m) -> WeaponAttributes.createAttributes(t, m, "newweapon"));
```

3. **Register in ModItems:**
```java
new ItemDef("newweapon", "metal/newweapon", true,
    (metal, props) -> WeaponItemFactory.createWeapon("newweapon", metal.getSerializedName(), props))
```

4. **Add to Data Generation Specs:**
   - `ModRecipeProvider`: Add to appropriate recipe spec list
   - `ModItemModelProvider`: Add to `WEAPON_MODELS` list
   - `ModLanguageProvider`: Add to `WEAPON_TRANSLATIONS` list
   - Other providers automatically pick it up from `WeaponRegistry`

### Adding a New Metal

**No code changes needed!** The mod automatically supports any TFC metal that:
- Has a tool tier > 0 (`metal.tier() > 0`)
- Can make all parts (`metal.allParts() == true`)

The metal will automatically:
- Appear in all weapon variants
- Get proper melting point calculation (from `FluidHeatData`)
- Get correct durability/efficiency from TFC's `LevelTier`
- Be included in all data generation

**If you need to add FluidHeat data:**
- Add constant to `FluidHeatData`
- Add entry to `METAL_TO_FLUID_HEAT` map

### Adding a New Component Type

1. **Add to ComponentType:**
```java
record NewComponent() implements ComponentType {
    @Override public String name() { return "new_component"; }
}
ComponentType NEW_COMPONENT = new NewComponent();
```

2. **Register in ModItems:**
```java
new ItemDef("new_component", "metal/new_component", false, 
    (metal, props) -> new ComponentItem(ComponentType.NEW_COMPONENT, metal.getSerializedName(), new Item.Properties()))
```

3. **Add to WeaponRegistry:**
```java
// In METADATA map
Map.entry("new_component", new Meta(Size.SMALL, Weight.LIGHT, 3))

// In ITEM_GETTERS map
Map.entry("new_component", ModItems::getNewComponentForMetal)
```

4. **Add to Data Generation Specs:**
   - `ModItemModelProvider`: Add to `COMPONENT_MODELS` list
   - `ModLanguageProvider`: Add to `COMPONENT_TRANSLATIONS` list
   - Other providers automatically pick it up from `WeaponRegistry`

### Customizing Weapon Attributes

Modify `WeaponAttributes.createWeaponAttributes()` or add new calculation methods.

**Key Parameters:**
- `baseWeight`: Affects both damage and speed (from `WeaponRegistry`)
- `baseDamage`: Base attack damage before modifiers (from `WeaponRegistry`)
- `baseSpeed`: Base attack speed (negative values, from `WeaponRegistry`)

**Metal Density**: Modify `WeaponRegistry.getMetalDensityMultiplier()` to change how metals affect damage.

---

## Build and Development

### Running Data Generation

```bash
./gradlew runData
```

This generates all JSON files in `src/generated/resources/`.

### Project Structure

```
WeaponsPlus/
├── src/
│   ├── main/
│   │   ├── java/          # Source code (this README's focus)
│   │   └── resources/     # Assets, models, textures
│   └── generated/         # Generated data files
├── build.gradle           # Build configuration
└── gradle.properties      # Mod properties
```

### Key Dependencies

- **NeoForge**: Mod loader and API
- **TFC**: TerraFirmaCraft (runtime dependency)

---
