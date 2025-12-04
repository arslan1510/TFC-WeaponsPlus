#!/usr/bin/env python3
"""
Create combined longsword textures by positioning blade and hilt correctly
Output size: 32x32 pixels
Positioning:
- Blade: Original orientation (no rotation), larger than hilt, positioned at top
- Hilt: Rotated 135 degrees anticlockwise, positioned at bottom
- Components overlap where blade meets hilt guard
"""

from PIL import Image
import os

def clean_alpha_edges(image, threshold=128):
    """
    Clean up semi-transparent edges by making pixels either fully opaque or fully transparent.
    This removes grey alpha halos around the edges and black pixels in transparent areas.
    
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
            # If below, make it fully transparent (and ensure RGB is 0,0,0 to avoid black pixels)
            if a >= threshold:
                pixels[x, y] = (r, g, b, 255)
            else:
                # Fully transparent - ensure no black pixels
                pixels[x, y] = (0, 0, 0, 0)
    
    return image

def remove_black_pixels(image):
    """
    Remove black pixels in transparent areas that can appear after rotation/resizing.
    Converts any pixel with low alpha to fully transparent with RGB 0,0,0.
    More aggressive cleaning for edges and transparent areas.
    
    Args:
        image: PIL Image in RGBA mode
    """
    if image.mode != 'RGBA':
        image = image.convert('RGBA')
    
    pixels = image.load()
    width, height = image.size
    
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            # If alpha is very low (transparent), ensure RGB is 0,0,0
            # Also check if it's a black/dark pixel (low RGB values) with low alpha
            # Be more aggressive - any pixel with alpha < 30 or dark pixels with alpha < 100
            if a < 30 or (r < 20 and g < 20 and b < 20 and a < 100):
                pixels[x, y] = (0, 0, 0, 0)
    
    return image

def clean_blade_edges(image):
    """
    Specifically clean black pixels around blade edges that appear after resizing.
    More thorough cleaning for blade textures.
    
    Args:
        image: PIL Image in RGBA mode
    """
    if image.mode != 'RGBA':
        image = image.convert('RGBA')
    
    pixels = image.load()
    width, height = image.size
    
    # First pass: remove obvious black/dark pixels with any transparency
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            # Remove any dark pixels (black or near-black) with low alpha
            # Be more aggressive - any dark pixel with alpha < 200 should be checked
            is_dark = r < 40 and g < 40 and b < 40
            if is_dark and a < 200:
                # If it's dark and not fully opaque, make it transparent
                pixels[x, y] = (0, 0, 0, 0)
            # Also remove any pixel with very low alpha
            elif a < 30:
                pixels[x, y] = (0, 0, 0, 0)
    
    # Second pass: clean edges more aggressively
    # Check neighbors - if a pixel is transparent and has dark neighbors, clean them too
    for y in range(1, height - 1):
        for x in range(1, width - 1):
            r, g, b, a = pixels[x, y]
            # If this pixel is dark and has transparent neighbors, it's likely an artifact
            is_dark = r < 50 and g < 50 and b < 50
            if is_dark and a < 255:
                # Check 8 neighbors
                neighbors = [
                    pixels[x-1, y-1], pixels[x, y-1], pixels[x+1, y-1],
                    pixels[x-1, y],                   pixels[x+1, y],
                    pixels[x-1, y+1], pixels[x, y+1], pixels[x+1, y+1]
                ]
                # Count transparent neighbors
                transparent_count = sum(1 for nr, ng, nb, na in neighbors if na < 50)
                # If most neighbors are transparent, this dark pixel is likely an artifact
                if transparent_count >= 4:
                    pixels[x, y] = (0, 0, 0, 0)
    
    return image

def create_longsword_texture(blade_path, hilt_path, output_path):
    """
    Create a combined longsword texture with blade in original orientation and hilt rotated 135° anticlockwise
    Output size: 32x32 pixels
    
    Args:
        blade_path: Path to blade texture (base blade, no rotation)
        hilt_path: Path to hilt texture (metal-specific, rotated 135° anticlockwise)
        output_path: Path to save combined texture
    """
    # Check if this is black_steel - skip aggressive black pixel cleaning for it
    is_black_steel = "black_steel" in output_path or "black_steel" in blade_path
    
    # Create 32x32 output canvas
    output_size = (32, 32)
    longsword_texture = Image.new("RGBA", output_size, (0, 0, 0, 0))
    
    # Load and scale hilt texture (hilt is already 32x32, then scale to specified scale)
    # Using LANCZOS for highest quality resampling
    hilt = Image.open(hilt_path).convert("RGBA")
    if hilt.size != (32, 32):
        hilt = hilt.resize((32, 32), Image.Resampling.LANCZOS)
    
    # Scale hilt to specified scale (0.75 = 75% of 32x32 = 24x24)
    # Using LANCZOS for highest quality resampling
    hilt_scale = 0.75
    scale_size = int(32 * hilt_scale)
    hilt = hilt.resize((scale_size, scale_size), Image.Resampling.LANCZOS)
    
    # Rotate hilt to specified rotation (132 degrees)
    # Using BICUBIC for rotation (LANCZOS not supported for rotation in this PIL version)
    hilt_rotation = 132
    # Using expand=True to ensure rotated image fits in canvas
    hilt = hilt.rotate(hilt_rotation, expand=True, resample=Image.Resampling.BICUBIC)
    # Remove black pixels that may appear after rotation
    hilt = remove_black_pixels(hilt)
    
    # Load blade texture and ensure proper alpha channel
    blade = Image.open(blade_path).convert("RGBA")
    # Clean initial black pixels if any (skip for black_steel)
    if not is_black_steel:
        blade = remove_black_pixels(blade)
    
    original_width, original_height = blade.size
    
    # Always upscale blade first to high quality (even if it's 32x32)
    # This prevents black pixels and artifacts when scaling to final size
    # Upscale to 512x512 for maximum quality processing
    upscale_size = 512
    # Maintain aspect ratio during upscale
    if original_width >= original_height:
        upscale_width = upscale_size
        upscale_height = int(upscale_size * (original_height / original_width))
    else:
        upscale_height = upscale_size
        upscale_width = int(upscale_size * (original_width / original_height))
    # High quality upscale using LANCZOS
    blade = blade.resize((upscale_width, upscale_height), Image.Resampling.LANCZOS)
    # Clean after upscale - remove any black pixels introduced (skip for black_steel)
    if not is_black_steel:
        blade = clean_blade_edges(blade)
        blade = remove_black_pixels(blade)
    
    # Now scale blade to final target dimensions
    # Using LANCZOS for highest quality resampling
    blade_height = 30
    blade_scale = 1.8
    target_blade_height = int(blade_height * blade_scale)
    # Maintain aspect ratio
    current_width, current_height = blade.size
    aspect_ratio = current_width / current_height
    target_blade_width = int(target_blade_height * aspect_ratio)
    # Resize with proper alpha preservation
    blade = blade.resize((target_blade_width, target_blade_height), Image.Resampling.LANCZOS)
    # Immediately clean after resize (skip for black_steel)
    if not is_black_steel:
        blade = clean_blade_edges(blade)
        blade = remove_black_pixels(blade)
    
    # Rotate blade if needed (0 degrees = no rotation)
    # Using BICUBIC for rotation (LANCZOS not supported for rotation in this PIL version)
    blade_rotation = 0
    if blade_rotation != 0:
        blade = blade.rotate(blade_rotation, expand=True, resample=Image.Resampling.BICUBIC)
    
    # Thoroughly clean black pixels from blade after resizing/rotation (skip for black_steel)
    # Use specialized blade edge cleaning
    if not is_black_steel:
        blade = clean_blade_edges(blade)
        blade = remove_black_pixels(blade)
    
    # Position blade first (base layer)
    # Use alpha composite for proper transparency handling
    blade_x_offset = 1
    blade_y_offset = -11
    blade_x = (output_size[0] - blade.width) // 2 + blade_x_offset
    blade_y = blade_y_offset
    # Create a temporary image for proper alpha compositing
    temp_canvas = Image.new("RGBA", output_size, (0, 0, 0, 0))
    temp_canvas.paste(blade, (blade_x, blade_y), blade)
    longsword_texture = Image.alpha_composite(longsword_texture, temp_canvas)
    
    # Position hilt (above blade)
    # Use alpha composite for proper transparency handling
    hilt_x_offset = -9
    hilt_y_offset = 8
    hilt_x = (output_size[0] - hilt.width) // 2 + hilt_x_offset
    hilt_y = hilt_y_offset
    # Create a temporary image for proper alpha compositing
    temp_canvas = Image.new("RGBA", output_size, (0, 0, 0, 0))
    temp_canvas.paste(hilt, (hilt_x, hilt_y), hilt)
    longsword_texture = Image.alpha_composite(longsword_texture, temp_canvas)
    
    # Clean up alpha edges and remove any remaining black pixels (skip aggressive cleaning for black_steel)
    longsword_texture = clean_alpha_edges(longsword_texture)
    if not is_black_steel:
        longsword_texture = remove_black_pixels(longsword_texture)
    
    # Create output directory if it doesn't exist
    output_dir = os.path.dirname(output_path)
    os.makedirs(output_dir, exist_ok=True)
    
    # Save the combined texture
    longsword_texture.save(output_path, "PNG")
    print(f"Created: {output_path}")

def generate_all_longsword_textures():
    """Generate longsword textures for same-metal combinations (blade and hilt must match)"""
    base_dir = "src/main/resources/assets/tfc_weapons_plus/textures/item"
    metals = ["copper", "bronze", "bismuth_bronze", "black_bronze", "wrought_iron", "steel", "black_steel", "blue_steel", "red_steel"]
    
    blade_dir = f"{base_dir}/metal/longsword_blade"
    hilt_dir = f"{base_dir}/metal/hilt"
    output_dir = f"{base_dir}/metal/longsword"
    
    # Generate only same-metal combinations
    total = len(metals)
    current = 0
    
    for metal in metals:
        current += 1
        blade_path = f"{blade_dir}/{metal}.png"
        hilt_path = f"{hilt_dir}/{metal}.png"
        
        # Output path: longsword/{metal}.png (not {blade}_{hilt})
        output_path = f"{output_dir}/{metal}.png"
        
        if not os.path.exists(blade_path):
            print(f"Warning: Missing blade texture for {metal}: {blade_path}")
            continue
        
        if os.path.exists(hilt_path):
            create_longsword_texture(blade_path, hilt_path, output_path)
            print(f"[{current}/{total}] {metal} longsword ({metal} blade + {metal} hilt)")
        else:
            print(f"Warning: Missing hilt texture for {metal}: {hilt_path}")

if __name__ == "__main__":
    try:
        generate_all_longsword_textures()
        print("\nDone! Longsword textures generated.")
    except ImportError:
        print("Error: Pillow is required. Install it with: pip install Pillow")
    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()

