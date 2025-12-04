#!/usr/bin/env python3
"""
Create combined hilt textures by positioning grip, guard, and pommel correctly
Output size: 32x32 pixels
Positioning (from top to bottom):
- Pommel: Top of texture, moved up slightly (y=-1)
- Grip: Middle of texture (y=10)
- Guard: Below grip, moved lower (y=26)
"""

from PIL import Image
import os

def clean_alpha_edges(image, threshold=128):
    """
    Clean up semi-transparent edges by making pixels either fully opaque or fully transparent.
    This removes grey alpha halos around the edges.
    
    Args:
        image: PIL Image in RGBA mode
        threshold: Alpha threshold (0-255). Pixels above this become fully opaque, below become fully transparent.
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

def create_hilt_texture(grip_path, guard_path, pommel_path, output_path):
    """
    Create a combined hilt texture with positioned components (32x32 output)
    
    Args:
        grip_path: Path to grip texture
        guard_path: Path to guard texture (metal-specific)
        pommel_path: Path to pommel texture (metal-specific)
        output_path: Path to save combined texture
    """
    # Load textures and resize appropriately
    grip = Image.open(grip_path).convert("RGBA")
    if grip.size != (16, 16):
        grip = grip.resize((16, 16), Image.Resampling.LANCZOS)
    
    # Make guard bigger (scale up to 20x20)
    guard = Image.open(guard_path).convert("RGBA")
    if guard.size != (20, 20):
        guard = guard.resize((20, 20), Image.Resampling.LANCZOS)
    
    # Make pommel 25% smaller (16 * 0.75 = 12x12)
    pommel = Image.open(pommel_path).convert("RGBA")
    pommel_size = int(16 * 0.75)  # 25% smaller = 12x12
    if pommel.size != (pommel_size, pommel_size):
        pommel = pommel.resize((pommel_size, pommel_size), Image.Resampling.LANCZOS)
    
    # Create 32x32 output canvas
    output_size = (32, 32)
    hilt_texture = Image.new("RGBA", output_size, (0, 0, 0, 0))
    
    # Position components vertically (from bottom to top for layering):
    # 1. Draw grip first (base layer, middle section)
    grip_x = (output_size[0] - grip.width) // 2
    hilt_texture.paste(grip, (grip_x, 10), grip)  # Base layer
    
    # 2. Draw pommel on top of grip (centered horizontally, top section)
    pommel_x = (output_size[0] - pommel.width) // 2
    hilt_texture.paste(pommel, (pommel_x, 5), pommel)  # On top of grip layer-wise
    
    # 3. Draw guard below grip (centered horizontally, bottom section)
    guard_x = (output_size[0] - guard.width) // 2
    hilt_texture.paste(guard, (guard_x, 16), guard)  # Below grip
    
    # Clean up alpha edges to remove surrounding semi-transparent pixels
    hilt_texture = clean_alpha_edges(hilt_texture)
    
    # Create output directory if it doesn't exist
    output_dir = os.path.dirname(output_path)
    os.makedirs(output_dir, exist_ok=True)
    
    # Save the combined texture
    hilt_texture.save(output_path, "PNG")
    print(f"Created: {output_path}")

def generate_all_hilt_textures():
    """Generate hilt textures for all metal variants"""
    base_dir = "src/main/resources/assets/tfc_weapons_plus/textures/item"
    metals = ["copper", "bronze", "bismuth_bronze", "black_bronze", "wrought_iron", "steel", "black_steel", "blue_steel", "red_steel"]
    
    grip_path = f"{base_dir}/wood/grip.png"
    
    if not os.path.exists(grip_path):
        print(f"Error: Grip texture not found: {grip_path}")
        return
    
    for metal in metals:
        guard_path = f"{base_dir}/metal/guard/{metal}.png"
        pommel_path = f"{base_dir}/metal/pommel/{metal}.png"
        output_path = f"{base_dir}/metal/hilt/{metal}.png"
        
        if os.path.exists(guard_path) and os.path.exists(pommel_path):
            create_hilt_texture(grip_path, guard_path, pommel_path, output_path)
        else:
            print(f"Warning: Missing textures for {metal} - guard: {os.path.exists(guard_path)}, pommel: {os.path.exists(pommel_path)}")

if __name__ == "__main__":
    try:
        generate_all_hilt_textures()
        print("\nDone! Hilt textures generated.")
    except ImportError:
        print("Error: Pillow is required. Install it with: pip install Pillow")
    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()

