#!/usr/bin/env python3
"""
Script to generate metal variant textures for longsword blades
Uses the base blade_longsword.png as template and applies metal-specific color tints

Requires: Pillow (pip install Pillow)
"""

from PIL import Image
import os

# Metal color tints (RGB multipliers)
# These are approximate colors for each TFC metal
METAL_COLORS = {
    "copper": (1.0, 0.6, 0.4),  # Orange-red
    "bronze": (0.8, 0.6, 0.4),  # Brown-orange
    "bismuth_bronze": (0.75, 0.85, 0.7),  # Lighter bronze with greenish tint
    "black_bronze": (0.4, 0.3, 0.25),  # Darker, almost black bronze
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

def clean_alpha_edges(image, threshold=128):
    """
    Clean up semi-transparent edges by making pixels either fully opaque or fully transparent.
    This removes grey alpha halos around the edges.
    """
    if image.mode != 'RGBA':
        image = image.convert('RGBA')
    
    pixels = image.load()
    width, height = image.size
    
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            # If alpha is above threshold, make it fully opaque
            # If below, make it fully transparent
            if a >= threshold:
                pixels[x, y] = (r, g, b, 255)
            else:
                pixels[x, y] = (r, g, b, 0)
    
    return image

def tint_image(image, color_multiplier):
    """
    Apply color tint to an image by multiplying RGB values
    
    Args:
        image: PIL Image in RGBA mode
        color_multiplier: Tuple of (r_mult, g_mult, b_mult) values (0.0-1.0)
    
    Returns:
        Tinted PIL Image
    """
    if image.mode != 'RGBA':
        image = image.convert('RGBA')
    
    tinted = Image.new('RGBA', image.size)
    pixels = image.load()
    tinted_pixels = tinted.load()
    
    r_mult, g_mult, b_mult = color_multiplier
    
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = pixels[x, y]
            
            # Apply color multiplier
            new_r = min(255, int(r * r_mult))
            new_g = min(255, int(g * g_mult))
            new_b = min(255, int(b * b_mult))
            
            tinted_pixels[x, y] = (new_r, new_g, new_b, a)
    
    return tinted

def generate_longsword_blade_variants():
    """Generate metal variant textures for longsword blades"""
    base_dir = "src/main/resources/assets/tfc_weapons_plus/textures/item"
    blade_dir = f"{base_dir}/metal/longsword_blade"
    
    # Base blade texture from project root
    base_path = "blade_longsword.png"
    
    if not os.path.exists(base_path):
        print(f"Error: Base blade texture not found: {base_path}")
        print("Please ensure blade_longsword.png is in the project root directory.")
        return
    
    print(f"Processing longsword blade textures...")
    base_image = Image.open(base_path).convert("RGBA")
    
    # Clean up alpha edges to remove grey halos
    base_image = clean_alpha_edges(base_image)
    
    # Convert base texture to grayscale first
    grayscale_image = convert_to_grayscale(base_image)
    
    # Generate all metal variants
    for metal_name, color in METAL_COLORS.items():
        # Create tinted variant from grayscale
        tinted = tint_image(grayscale_image.copy(), color)
        
        # Clean up alpha edges to remove grey halos
        tinted = clean_alpha_edges(tinted)
        
        # Save variant to: metal/longsword_blade/{metal}.png
        os.makedirs(blade_dir, exist_ok=True)
        variant_path = f"{blade_dir}/{metal_name}.png"
        tinted.save(variant_path, "PNG")
        print(f"  Generated: {variant_path}")
    
    print("\nDone! Longsword blade metal variant textures generated.")

if __name__ == "__main__":
    try:
        generate_longsword_blade_variants()
    except ImportError:
        print("Error: Pillow is required. Install it with: pip install Pillow")
    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()

