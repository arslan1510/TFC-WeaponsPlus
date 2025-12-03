#!/usr/bin/env python3
"""
Script to generate metal variant textures for guard and pommel
Uses the base textures as templates and applies metal-specific color tints

Requires: Pillow (pip install Pillow)
"""

from PIL import Image
import os

# Metal color tints (RGB multipliers)
# These are approximate colors for each TFC metal
METAL_COLORS = {
    "copper": (1.0, 0.6, 0.4),  # Orange-red
    "bronze": (0.8, 0.6, 0.4),  # Brown-orange
    "bismuth_bronze": (0.75, 0.85, 0.7),  # Lighter bronze with greenish tint (bismuth bronze characteristic)
    "black_bronze": (0.4, 0.3, 0.25),  # Darker, almost black bronze (darker than regular bronze)
    "wrought_iron": (0.7, 0.7, 0.7),  # Gray
    "steel": (0.9, 0.9, 0.9),  # Light gray
    "black_steel": (0.3, 0.3, 0.3),  # Dark gray/black
    "blue_steel": (0.5, 0.6, 0.8),  # Blue-gray
    "red_steel": (0.8, 0.5, 0.5),  # Red-gray
}

def convert_to_grayscale(image):
    """Convert image to grayscale while preserving alpha channel"""
    if image.mode != 'RGBA':
        image = image.convert('RGBA')
    
    # Convert RGB to grayscale using luminance formula
    grayscale = Image.new('RGBA', image.size)
    pixels = image.load()
    gray_pixels = grayscale.load()
    
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = pixels[x, y]
            # Use luminance formula: 0.299*R + 0.587*G + 0.114*B
            gray_value = int(0.299 * r + 0.587 * g + 0.114 * b)
            gray_pixels[x, y] = (gray_value, gray_value, gray_value, a)
    
    return grayscale

def tint_image(image, color_multiplier):
    """Apply a color tint to a grayscale image"""
    # Ensure image is RGBA
    if image.mode != 'RGBA':
        image = image.convert('RGBA')
    
    # Create a new image with the tint applied
    tinted = Image.new('RGBA', image.size)
    pixels = image.load()
    tinted_pixels = tinted.load()
    
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = pixels[x, y]
            # For grayscale images, r == g == b, so we use the gray value
            gray_value = r  # Since it's grayscale, all channels are the same
            
            # Apply tint: multiply grayscale value by color multiplier
            # This preserves the brightness while adding color
            new_r = int(min(255, gray_value * color_multiplier[0]))
            new_g = int(min(255, gray_value * color_multiplier[1]))
            new_b = int(min(255, gray_value * color_multiplier[2]))
            tinted_pixels[x, y] = (new_r, new_g, new_b, a)
    
    return tinted

def generate_variants():
    """Generate metal variant textures"""
    base_dir = "src/main/resources/assets/tfc_weapons_plus/textures/item"
    components = ["guard", "pommel"]
    
    # Use bronze as the base texture since it exists
    base_metal = "bronze"
    
    for component in components:
        # Try to use existing bronze texture as base
        base_path = f"{base_dir}/metal/{component}/{base_metal}.png"
        
        if not os.path.exists(base_path):
            print(f"Warning: Base texture not found: {base_path}")
            continue
        
        print(f"Processing {component}...")
        base_image = Image.open(base_path)
        
        # Convert base texture to grayscale first
        grayscale_image = convert_to_grayscale(base_image)
        
        for metal_name, color in METAL_COLORS.items():
            # Skip the base metal (bronze) since we're using it as the source
            if metal_name == base_metal:
                continue
                
            # Create tinted variant from grayscale
            tinted = tint_image(grayscale_image.copy(), color)
            
            # Save variant to TFC-style path: metal/{component}/{metal}.png
            variant_dir = f"{base_dir}/metal/{component}"
            os.makedirs(variant_dir, exist_ok=True)
            variant_path = f"{variant_dir}/{metal_name}.png"
            tinted.save(variant_path)
            print(f"  Generated: {variant_path}")
    
    print("\nDone! Metal variant textures generated.")
    print("Note: You may want to manually adjust colors to better match TFC's metal aesthetics.")

if __name__ == "__main__":
    try:
        generate_variants()
    except ImportError:
        print("Error: Pillow is required. Install it with: pip install Pillow")
    except Exception as e:
        print(f"Error: {e}")

