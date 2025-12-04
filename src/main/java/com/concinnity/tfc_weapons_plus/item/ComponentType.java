package com.concinnity.tfc_weapons_plus.item;

/**
 * Sealed interface for component types using modern Java 21 sealed classes
 */
public sealed interface ComponentType permits ComponentType.Grip, ComponentType.Guard, ComponentType.Pommel, ComponentType.Hilt, ComponentType.LongswordBlade, ComponentType.GreatswordBlade {
    
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
    
    // Constants for easy access
    ComponentType GRIP = new Grip();
    ComponentType GUARD = new Guard();
    ComponentType POMMEL = new Pommel();
    ComponentType HILT = new Hilt();
    ComponentType LONGSWORD_BLADE = new LongswordBlade();
    ComponentType GREATSWORD_BLADE = new GreatswordBlade();
}

