#!/usr/bin/env python3
"""
Merge a tree image with red steel sword and axe from TerraFirmaCraft
Removes black background from tree and places weapons on top of the tree.

Usage:
    python merge_tree_with_weapons.py <tree_image_path> [output_path]

Requires: Pillow (pip install Pillow)
"""

from PIL import Image
import os
import sys

# Paths to TerraFirmaCraft textures
TFC_MOD_PATH = "run/mods/TerraFirmaCraft-NeoForge-1.21.1-4.0.11-beta"
SWORD_PATH = f"{TFC_MOD_PATH}/assets/tfc/textures/item/metal/sword/red_steel.png"
AXE_PATH = f"{TFC_MOD_PATH}/assets/tfc/textures/item/metal/axe/red_steel.png"

def remove_black_background(image, threshold=30):
    """
    Remove black background from image by making black pixels transparent
    
    Args:
        image: PIL Image in RGBA mode
        threshold: RGB threshold below which pixels are considered black (0-255)
    
    Returns:
        Image with black pixels made transparent
    """
    if image.mode != 'RGBA':
        image = image.convert('RGBA')
    
    # Create a new image with transparency
    result = Image.new('RGBA', image.size, (0, 0, 0, 0))
    pixels = image.load()
    result_pixels = result.load()
    
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = pixels[x, y]
            # Check if pixel is black (all RGB values below threshold)
            if r <= threshold and g <= threshold and b <= threshold:
                # Make it transparent
                result_pixels[x, y] = (r, g, b, 0)
            else:
                # Keep original pixel
                result_pixels[x, y] = (r, g, b, a)
    
    return result

def merge_tree_with_weapons(tree_path, output_path=None):
    """
    Merge tree image with red steel sword and axe
    
    Args:
        tree_path: Path to tree image
        output_path: Optional output path (defaults to tree_path with _merged suffix)
    """
    # Check if tree image exists
    if not os.path.exists(tree_path):
        print(f"Error: Tree image not found: {tree_path}")
        return False
    
    # Check if weapon textures exist
    if not os.path.exists(SWORD_PATH):
        print(f"Error: Sword texture not found: {SWORD_PATH}")
        return False
    
    if not os.path.exists(AXE_PATH):
        print(f"Error: Axe texture not found: {AXE_PATH}")
        return False
    
    # Load images
    print(f"Loading tree image: {tree_path}")
    tree = Image.open(tree_path).convert("RGBA")
    
    print("Removing black background from tree...")
    tree = remove_black_background(tree)
    
    # Resize tree to 64x64
    print("Resizing tree to 64x64...")
    tree = tree.resize((64, 64), Image.Resampling.LANCZOS)
    
    print(f"Loading sword: {SWORD_PATH}")
    sword = Image.open(SWORD_PATH).convert("RGBA")
    
    print(f"Loading axe: {AXE_PATH}")
    axe = Image.open(AXE_PATH).convert("RGBA")
    
    # Use 64x64 as base for output
    output_size = (64, 64)
    merged = Image.new("RGBA", output_size, (0, 0, 0, 0))
    
    # 1. Resize weapons to their original 16x16 size
    print(f"Resizing sword to 16x16: {sword.size} -> (16, 16)")
    print(f"Resizing axe to 16x16: {axe.size} -> (16, 16)")
    
    sword = sword.resize((16, 16), Image.Resampling.LANCZOS)
    axe = axe.resize((16, 16), Image.Resampling.LANCZOS)
    
    # Flip sword horizontally (to the left)
    print("Flipping sword horizontally...")
    sword = sword.transpose(Image.FLIP_LEFT_RIGHT)
    
    # 2. Draw tree first (background layer)
    merged.paste(tree, (0, 0), tree)
    
    # 3. Position sword and axe on top (foreground layer)
    # Center both weapons horizontally so they overlap, and move down by 6 pixels
    
    # Center both weapons horizontally
    sword_x = (output_size[0] - sword.width) // 2
    axe_x = (output_size[0] - axe.width) // 2
    
    # Center vertically and move down by 6 pixels
    sword_y = (output_size[1] - sword.height) // 2 + 6
    axe_y = (output_size[1] - axe.height) // 2 + 6
    
    # Paste sword and axe on top (foreground layer)
    merged.paste(sword, (sword_x, sword_y), sword)
    merged.paste(axe, (axe_x, axe_y), axe)
    
    # Determine output path
    if output_path is None:
        base_name = os.path.splitext(tree_path)[0]
        ext = os.path.splitext(tree_path)[1]
        output_path = f"{base_name}_merged{ext}"
    
    # Save the merged image
    merged.save(output_path, "PNG")
    print(f"Created merged image: {output_path}")
    return True

def main():
    """Main function"""
    if len(sys.argv) < 2:
        print("Usage: python merge_tree_with_weapons.py <tree_image_path> [output_path]")
        print("\nExample:")
        print("  python merge_tree_with_weapons.py tree.png")
        print("  python merge_tree_with_weapons.py tree.png output.png")
        sys.exit(1)
    
    tree_path = sys.argv[1]
    output_path = sys.argv[2] if len(sys.argv) > 2 else None
    
    try:
        success = merge_tree_with_weapons(tree_path, output_path)
        if success:
            print("\nDone! Merged image created.")
        else:
            sys.exit(1)
    except ImportError:
        print("Error: Pillow is required. Install it with: pip install Pillow")
        sys.exit(1)
    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    main()

