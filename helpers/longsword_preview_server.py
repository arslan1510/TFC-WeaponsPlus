#!/usr/bin/env python3
"""
Web server with real-time preview for longsword texture generation
Allows adjusting hilt position parameters and seeing the result instantly
"""

from flask import Flask, render_template_string, send_file, request, jsonify
from PIL import Image
import os
import io
import base64

app = Flask(__name__)

# Default parameters
DEFAULT_PARAMS = {
    'hilt_x_offset': -8,
    'hilt_y_offset': 12,
    'hilt_rotation': 132,
    'hilt_scale': 0.5,  # 50% of 32x32
    'blade_x_offset': -2,
    'blade_y_offset': 1,
    'blade_rotation': 0,
    'blade_height': 35,
    'blade_scale': 1.0,
    'output_size': 32
}

def clean_alpha_edges(image, threshold=128):
    """Clean up semi-transparent edges"""
    if image.mode != 'RGBA':
        image = image.convert('RGBA')
    
    pixels = image.load()
    width, height = image.size
    
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            if a >= threshold:
                pixels[x, y] = (r, g, b, 255)
            else:
                pixels[x, y] = (r, g, b, 0)
    
    return image

def generate_longsword_preview(blade_path, hilt_path, params):
    """Generate longsword texture with given parameters"""
    output_size = (params['output_size'], params['output_size'])
    longsword_texture = Image.new("RGBA", output_size, (0, 0, 0, 0))
    
    # Load and scale hilt texture (hilt is 32x32)
    hilt = Image.open(hilt_path).convert("RGBA")
    if hilt.size != (32, 32):
        hilt = hilt.resize((32, 32), Image.Resampling.LANCZOS)
    
    # Scale hilt
    scale_size = int(32 * params['hilt_scale'])
    hilt = hilt.resize((scale_size, scale_size), Image.Resampling.LANCZOS)
    
    # Rotate hilt
    hilt = hilt.rotate(params['hilt_rotation'], expand=True, resample=Image.Resampling.BICUBIC)
    
    # Load blade texture
    blade = Image.open(blade_path).convert("RGBA")
    original_width, original_height = blade.size
    
    # Scale blade
    aspect_ratio = original_width / original_height
    target_blade_height = int(params['blade_height'] * params['blade_scale'])
    target_blade_width = int(target_blade_height * aspect_ratio)
    blade = blade.resize((target_blade_width, target_blade_height), Image.Resampling.LANCZOS)
    
    # Rotate blade if needed
    if params.get('blade_rotation', 0) != 0:
        blade = blade.rotate(params['blade_rotation'], expand=True, resample=Image.Resampling.BICUBIC)
    
    # Position blade first (base layer)
    blade_x = (output_size[0] - blade.width) // 2 + params.get('blade_x_offset', 0)
    blade_y = params.get('blade_y_offset', 0)
    longsword_texture.paste(blade, (blade_x, blade_y), blade)
    
    # Position hilt (above blade)
    hilt_x = (output_size[0] - hilt.width) // 2 + params['hilt_x_offset']
    hilt_y = params['hilt_y_offset']
    longsword_texture.paste(hilt, (hilt_x, hilt_y), hilt)
    
    # Clean up alpha edges
    longsword_texture = clean_alpha_edges(longsword_texture)
    
    return longsword_texture

HTML_TEMPLATE = """
<!DOCTYPE html>
<html>
<head>
    <title>Longsword Texture Preview</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
            background: #1e1e1e;
            color: #fff;
        }
        .container {
            display: flex;
            gap: 20px;
        }
        .controls {
            flex: 1;
            background: #2d2d2d;
            padding: 20px;
            border-radius: 8px;
        }
        .preview {
            flex: 1;
            background: #2d2d2d;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
        }
        .control-group {
            margin-bottom: 20px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }
        input[type="range"] {
            width: 100%;
        }
        input[type="number"] {
            width: 80px;
            padding: 5px;
            background: #3d3d3d;
            border: 1px solid #555;
            color: #fff;
            border-radius: 4px;
        }
        .preview-image {
            max-width: 100%;
            image-rendering: pixelated;
            image-rendering: -moz-crisp-edges;
            image-rendering: crisp-edges;
            border: 2px solid #555;
            background: #000;
        }
        button {
            padding: 10px 20px;
            background: #4CAF50;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            margin-top: 10px;
        }
        button:hover {
            background: #45a049;
        }
        .value-display {
            display: inline-block;
            margin-left: 10px;
            color: #4CAF50;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <h1>Longsword Texture Preview</h1>
    <div class="container">
        <div class="controls">
            <h2>Hilt Position Controls</h2>
            
            <div class="control-group">
                <label>
                    Hilt X Offset: 
                    <span class="value-display" id="hilt_x_value">{{ params.hilt_x_offset }}</span>
                </label>
                <input type="range" id="hilt_x_offset" min="-16" max="16" value="{{ params.hilt_x_offset }}" 
                       oninput="updateValue('hilt_x_value', this.value); updatePreview()">
            </div>
            
            <div class="control-group">
                <label>
                    Hilt Y Offset: 
                    <span class="value-display" id="hilt_y_value">{{ params.hilt_y_offset }}</span>
                </label>
                <input type="range" id="hilt_y_offset" min="0" max="32" value="{{ params.hilt_y_offset }}" 
                       oninput="updateValue('hilt_y_value', this.value); updatePreview()">
            </div>
            
            <div class="control-group">
                <label>
                    Hilt Rotation: 
                    <span class="value-display" id="hilt_rotation_value">{{ params.hilt_rotation }}</span>°
                </label>
                <input type="range" id="hilt_rotation" min="0" max="360" value="{{ params.hilt_rotation }}" 
                       oninput="updateValue('hilt_rotation_value', this.value); updatePreview()">
            </div>
            
            <div class="control-group">
                <label>
                    Hilt Scale: 
                    <span class="value-display" id="hilt_scale_value">{{ params.hilt_scale }}</span>
                </label>
                <input type="range" id="hilt_scale" min="0.1" max="1.0" step="0.05" value="{{ params.hilt_scale }}" 
                       oninput="updateValue('hilt_scale_value', parseFloat(this.value).toFixed(2)); updatePreview()">
            </div>
            
            <h2>Blade Controls</h2>
            
            <div class="control-group">
                <label>
                    Blade X Offset: 
                    <span class="value-display" id="blade_x_value">{{ params.blade_x_offset }}</span>
                </label>
                <input type="range" id="blade_x_offset" min="-16" max="16" value="{{ params.blade_x_offset }}" 
                       oninput="updateValue('blade_x_value', this.value); updatePreview()">
            </div>
            
            <div class="control-group">
                <label>
                    Blade Y Offset: 
                    <span class="value-display" id="blade_y_value">{{ params.blade_y_offset }}</span>
                </label>
                <input type="range" id="blade_y_offset" min="-16" max="32" value="{{ params.blade_y_offset }}" 
                       oninput="updateValue('blade_y_value', this.value); updatePreview()">
            </div>
            
            <div class="control-group">
                <label>
                    Blade Rotation: 
                    <span class="value-display" id="blade_rotation_value">{{ params.blade_rotation }}</span>°
                </label>
                <input type="range" id="blade_rotation" min="0" max="360" value="{{ params.blade_rotation }}" 
                       oninput="updateValue('blade_rotation_value', this.value); updatePreview()">
            </div>
            
            <div class="control-group">
                <label>
                    Blade Height: 
                    <span class="value-display" id="blade_height_value">{{ params.blade_height }}</span>
                </label>
                <input type="range" id="blade_height" min="15" max="50" value="{{ params.blade_height }}" 
                       oninput="updateValue('blade_height_value', this.value); updatePreview()">
            </div>
            
            <div class="control-group">
                <label>
                    Blade Scale: 
                    <span class="value-display" id="blade_scale_value">{{ params.blade_scale }}</span>
                </label>
                <input type="range" id="blade_scale" min="0.5" max="2.0" step="0.1" value="{{ params.blade_scale }}" 
                       oninput="updateValue('blade_scale_value', parseFloat(this.value).toFixed(1)); updatePreview()">
            </div>
            
            <button onclick="saveTexture()">Save Current Texture</button>
            <button onclick="generateAll()">Generate All Metal Variants</button>
        </div>
        
        <div class="preview">
            <h2>Preview</h2>
            <img id="preview_image" class="preview-image" src="/preview" alt="Longsword Preview">
            <p>32x32 pixels</p>
        </div>
    </div>
    
    <script>
        function updateValue(elementId, value) {
            document.getElementById(elementId).textContent = value;
        }
        
        function updatePreview() {
            const params = {
                hilt_x_offset: parseInt(document.getElementById('hilt_x_offset').value),
                hilt_y_offset: parseInt(document.getElementById('hilt_y_offset').value),
                hilt_rotation: parseInt(document.getElementById('hilt_rotation').value),
                hilt_scale: parseFloat(document.getElementById('hilt_scale').value),
                blade_x_offset: parseInt(document.getElementById('blade_x_offset').value),
                blade_y_offset: parseInt(document.getElementById('blade_y_offset').value),
                blade_rotation: parseInt(document.getElementById('blade_rotation').value),
                blade_height: parseInt(document.getElementById('blade_height').value),
                blade_scale: parseFloat(document.getElementById('blade_scale').value)
            };
            
            const queryString = new URLSearchParams(params).toString();
            document.getElementById('preview_image').src = '/preview?' + queryString;
        }
        
        function saveTexture() {
            const params = {
                hilt_x_offset: parseInt(document.getElementById('hilt_x_offset').value),
                hilt_y_offset: parseInt(document.getElementById('hilt_y_offset').value),
                hilt_rotation: parseInt(document.getElementById('hilt_rotation').value),
                hilt_scale: parseFloat(document.getElementById('hilt_scale').value),
                blade_x_offset: parseInt(document.getElementById('blade_x_offset').value),
                blade_y_offset: parseInt(document.getElementById('blade_y_offset').value),
                blade_rotation: parseInt(document.getElementById('blade_rotation').value),
                blade_height: parseInt(document.getElementById('blade_height').value),
                blade_scale: parseFloat(document.getElementById('blade_scale').value),
                save: true
            };
            
            window.location.href = '/preview?' + new URLSearchParams(params).toString();
        }
        
        function generateAll() {
            const params = {
                hilt_x_offset: parseInt(document.getElementById('hilt_x_offset').value),
                hilt_y_offset: parseInt(document.getElementById('hilt_y_offset').value),
                hilt_rotation: parseInt(document.getElementById('hilt_rotation').value),
                hilt_scale: parseFloat(document.getElementById('hilt_scale').value),
                blade_x_offset: parseInt(document.getElementById('blade_x_offset').value),
                blade_y_offset: parseInt(document.getElementById('blade_y_offset').value),
                blade_rotation: parseInt(document.getElementById('blade_rotation').value),
                blade_height: parseInt(document.getElementById('blade_height').value),
                blade_scale: parseFloat(document.getElementById('blade_scale').value),
                generate_all: true
            };
            
            fetch('/generate_all?' + new URLSearchParams(params).toString())
                .then(response => response.json())
                .then(data => {
                    alert(data.message || 'All textures generated!');
                })
                .catch(error => {
                    alert('Error: ' + error);
                });
        }
    </script>
</body>
</html>
"""

@app.route('/')
def index():
    """Main page with controls"""
    return render_template_string(HTML_TEMPLATE, params=DEFAULT_PARAMS)

@app.route('/preview')
def preview():
    """Generate and return preview image"""
    # Get parameters from query string
    params = DEFAULT_PARAMS.copy()
    params['hilt_x_offset'] = int(request.args.get('hilt_x_offset', params['hilt_x_offset']))
    params['hilt_y_offset'] = int(request.args.get('hilt_y_offset', params['hilt_y_offset']))
    params['hilt_rotation'] = int(request.args.get('hilt_rotation', params['hilt_rotation']))
    params['hilt_scale'] = float(request.args.get('hilt_scale', params['hilt_scale']))
    params['blade_x_offset'] = int(request.args.get('blade_x_offset', params['blade_x_offset']))
    params['blade_y_offset'] = int(request.args.get('blade_y_offset', params['blade_y_offset']))
    params['blade_rotation'] = int(request.args.get('blade_rotation', params['blade_rotation']))
    params['blade_height'] = int(request.args.get('blade_height', params['blade_height']))
    params['blade_scale'] = float(request.args.get('blade_scale', params['blade_scale']))
    params['output_size'] = int(request.args.get('output_size', params['output_size']))
    
    # Paths
    blade_path = "blade_longsword.png"
    hilt_path = "src/main/resources/assets/tfc_weapons_plus/textures/item/metal/hilt/copper.png"
    
    # Check if files exist
    if not os.path.exists(blade_path):
        return "Blade texture not found: " + blade_path, 404
    if not os.path.exists(hilt_path):
        return "Hilt texture not found: " + hilt_path, 404
    
    # Generate preview
    img = generate_longsword_preview(blade_path, hilt_path, params)
    
    # Save if requested
    if request.args.get('save'):
        output_path = "preview_longsword.png"
        img.save(output_path, "PNG")
        return send_file(output_path, mimetype='image/png', as_attachment=True, download_name='longsword_preview.png')
    
    # Return image
    img_io = io.BytesIO()
    img.save(img_io, 'PNG')
    img_io.seek(0)
    return send_file(img_io, mimetype='image/png')

@app.route('/generate_all')
def generate_all():
    """Generate all metal variant textures with current parameters"""
    params = DEFAULT_PARAMS.copy()
    params['hilt_x_offset'] = int(request.args.get('hilt_x_offset', params['hilt_x_offset']))
    params['hilt_y_offset'] = int(request.args.get('hilt_y_offset', params['hilt_y_offset']))
    params['hilt_rotation'] = int(request.args.get('hilt_rotation', params['hilt_rotation']))
    params['hilt_scale'] = float(request.args.get('hilt_scale', params['hilt_scale']))
    params['blade_x_offset'] = int(request.args.get('blade_x_offset', params['blade_x_offset']))
    params['blade_y_offset'] = int(request.args.get('blade_y_offset', params['blade_y_offset']))
    params['blade_rotation'] = int(request.args.get('blade_rotation', params['blade_rotation']))
    params['blade_height'] = int(request.args.get('blade_height', params['blade_height']))
    params['blade_scale'] = float(request.args.get('blade_scale', params['blade_scale']))
    params['output_size'] = int(request.args.get('output_size', params['output_size']))
    
    blade_path = "blade_longsword.png"
    base_dir = "src/main/resources/assets/tfc_weapons_plus/textures/item"
    metals = ["copper", "bronze", "bismuth_bronze", "black_bronze", "wrought_iron", "steel", "black_steel", "blue_steel", "red_steel"]
    
    if not os.path.exists(blade_path):
        return jsonify({'error': 'Blade texture not found'}), 404
    
    generated = []
    for metal in metals:
        hilt_path = f"{base_dir}/metal/hilt/{metal}.png"
        output_path = f"{base_dir}/metal/longsword/{metal}.png"
        
        if os.path.exists(hilt_path):
            try:
                img = generate_longsword_preview(blade_path, hilt_path, params)
                os.makedirs(os.path.dirname(output_path), exist_ok=True)
                img.save(output_path, "PNG")
                generated.append(metal)
            except Exception as e:
                return jsonify({'error': f'Error generating {metal}: {str(e)}'}), 500
    
    return jsonify({'message': f'Generated {len(generated)} textures', 'metals': generated})

if __name__ == '__main__':
    print("Starting Longsword Preview Server...")
    print("Open http://localhost:5000 in your browser")
    app.run(debug=True, host='0.0.0.0', port=5000)

