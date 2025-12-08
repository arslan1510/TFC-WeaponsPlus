package com.concinnity.tfc_weapons_plus.item;

/**
 * Sealed interface for component types using modern Java 21 sealed classes
 */
public sealed interface ComponentType permits ComponentType.Grip, ComponentType.Guard, ComponentType.Pommel, ComponentType.Hilt, ComponentType.LongswordBlade, ComponentType.GreatswordBlade, ComponentType.ShortswordBlade, ComponentType.GreatAxeHead, ComponentType.GreatHammerHead {
    
    String name();
    
    record Grip() implements ComponentType {
        @Override
        public String name() { return "grip"; }
    }
    
    record Guard() implements ComponentType {
        @Override
        public String name() { return "guard"; }
    }
    
    record Pommel() implements ComponentType {
        @Override
        public String name() { return "pommel"; }
    }
    
    record Hilt() implements ComponentType {
        @Override
        public String name() { return "hilt"; }
    }
    
    record LongswordBlade() implements ComponentType {
        @Override
        public String name() { return "longsword_blade"; }
    }
    
    record GreatswordBlade() implements ComponentType {
        @Override
        public String name() { return "greatsword_blade"; }
    }
    
    record ShortswordBlade() implements ComponentType {
        @Override
        public String name() { return "shortsword_blade"; }
    }
    
    record GreatAxeHead() implements ComponentType {
        @Override
        public String name() { return "greataxe_head"; }
    }
    
    record GreatHammerHead() implements ComponentType {
        @Override
        public String name() { return "greathammer_head"; }
    }
    
    // Constants for easy access
    ComponentType GRIP = new Grip();
    ComponentType GUARD = new Guard();
    ComponentType POMMEL = new Pommel();
    ComponentType HILT = new Hilt();
    ComponentType LONGSWORD_BLADE = new LongswordBlade();
    ComponentType GREATSWORD_BLADE = new GreatswordBlade();
    ComponentType SHORTSWORD_BLADE = new ShortswordBlade();
    ComponentType GREATAXE_HEAD = new GreatAxeHead();
    ComponentType GREATHAMMER_HEAD = new GreatHammerHead();
}

