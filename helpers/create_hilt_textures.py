#!/usr/bin/env python3
"""
Create combined hilt textures by positioning grip, guard, and pommel correctly
- Grip: Full vertical height (base layer)
- Pommel: Top position, reduced to 50% size (0.5x scale)
- Guard: Positioned lower on the grip
"""

from PIL import Image
import os

def create_hilt_texture(grip_path, guard_path, pommel_path, output_path):
    """
    Create a combined hilt texture with positioned components
    
    Args:
        grip_path: Path to grip texture
        guard_path: Path to guard texture (metal-specific)
        pommel_path: Path to pommel texture (metal-specific)
        output_path: Path to save combined texture
    """
    # Load textures
    grip = Image.open(grip_path).convert("RGBA")
    guard = Image.open(guard_path).convert("RGBA")
    pommel = Image.open(pommel_path).convert("RGBA")
    
    size_h = grip.size[1]
    size_w = grip.size[0]
    # Reduce all components to smaller size
    grip_width, grip_height = grip.size
    grip_scaled = grip.resize((grip_width // 2, grip_height // 2), Image.Resampling.LANCZOS)
    
    guard_width, guard_height = guard.size
    guard_scaled = guard.resize((guard_width // 2, guard_height // 2), Image.Resampling.LANCZOS)
    
    pommel_width, pommel_height = pommel.size
    pommel_scaled = pommel.resize((pommel_width // 4, pommel_height // 4), Image.Resampling.LANCZOS)
    
    # Use grip's scaled size as base for output (grip is vertical, full height)
    output_size = (size_w, size_h)
    hilt_texture = Image.new("RGBA", output_size, (0, 0, 0, 0))
    
    # 1. Draw grip first (base layer, full height, centered horizontally)
    # Move grip up a bit
    grip_x = (output_size[0] - grip_scaled.width) // 2
    grip_y = 190  # Move up by 2 pixels
    hilt_texture.paste(grip_scaled, (grip_x, grip_y), grip_scaled)
    
    # 2. Draw pommel at the top, centered horizontally
    pommel_x = (output_size[0] - pommel_scaled.width) // 2
    pommel_y = 160  # Move up by 2 pixels
    hilt_texture.paste(pommel_scaled, (pommel_x, pommel_y), pommel_scaled)
    
    # 3. Draw guard positioned lower on the grip (moved up from 50% to 40%)
    guard_x = (output_size[0] - guard_scaled.width) // 2
    guard_y = int(output_size[1] * 0.4) - 3  # 40% down from top, moved up by 3 pixels
    hilt_texture.paste(guard_scaled, (guard_x, guard_y), guard_scaled)
    
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

