# TFC Weapons Plus

A TerraFirmaCraft addon for Minecraft 1.21.1 that adds modular weapon components for blacksmithing. Create custom weapons by assembling different components (blades/heads, guards, pommels, and grips) made from TFC metals.

Built with **pure functional programming** principles for maximum maintainability and extensibility.

## Requirements

- **Minecraft**: 1.21.1
- **NeoForge**: 21.1.0 or higher
- **TerraFirmaCraft**: 4.0.0 or higher (required at runtime, optional for data generation)
- **GeckoLib**: For 3D weapon models

### Project Structure

```
src/main/java/com/concinnity/tfcweaponsplus/
├── index/                    
│   ├── ModelIndex.java       - Geometry model paths
│   ├── TextureIndex.java     - Texture paths & validation
│   ├── StatsIndex.java       - Base weapon stats + tier scaling
│   └── ItemsIndex.java       - Weapon definitions 
├── core/                     
│   └── MetalSupplier.java    - TFC Metal query functions
├── data/                     
│   ├── MetalRecord.java      - Metal data from TFC
│   ├── ComponentType.java    - Component types (BLADE, HEAD, GUARD, etc.)
│   └── WeaponStats.java      - Weapon combat statistics
├── registration/
│   ├── ItemData.java         - Pure data record for all items
│   ├── ItemDataBuilder.java  - Data generation functions
│   ├── ItemCreator.java      - Data → Item converter
│   └── ItemRegistry.java     - Registration orchestration
└── item/
    ├── ComponentItem.java    - Unified component item class
    └── WeaponItem.java       - Generic weapon item class
```

### Adding New Weapons

**1. Define in `ItemsIndex.java`:**
```java
new WeaponDef("spear", ComponentType.BLADE, true)
```

**2. Add stats in `StatsIndex.java`:**
```java
"spear", new BaseStats(6.5, -2.2, 4.0)
```

Everything else is automatic - the unified pipeline handles registration for both components and weapons!

## Building

1. Clone this repository
2. Run `./gradlew build` (or `gradlew.bat build` on Windows)
3. The built JAR will be in `build/libs/`

## Development

### Key Design Patterns

**Pure Data-Driven**: Everything is just data → item
```java
// All items are just data records
record ItemData(
    String name,
    Optional<MetalRecord> metal,
    Optional<ComponentType> componentType,
    Optional<WeaponStats> stats
)

// Single function: data → item
Function<ItemData, Item> create = data -> buildFrom(data);
```

**No Conditional Logic**: Data presence determines behavior
```java
// Not: if (isWeapon) addStats()
// But: data.stats().ifPresent(s -> props.attributes(s))
```

**Immutable Data**: All structures are `record`s
```java
public record ItemData(...) {}  // Pure data, no methods
```

**Unified Treatment**: Same function for all items
- No pipelines
- No separate factories
- Data tells us what to build

See `PURE_DATA_ARCHITECTURE.md` for detailed explanation.

## License

MIT

## Additional Resources

- **NeoForge Documentation**: https://docs.neoforged.net/
- **NeoForge Discord**: https://discord.neoforged.net/
- **TerraFirmaCraft**: https://www.curseforge.com/minecraft/mc-mods/terrafirmacraft

## Credits

- **Author**: Concinnity
- **Mod ID**: `tfc_weapons_plus`
