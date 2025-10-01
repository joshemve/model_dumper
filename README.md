# Model Dumper - Forge 1.20.1

A Minecraft Forge mod that automatically exports all entity, item, and armor models with their textures to help content creators make thumbnails and promotional art.

## Features

- 🎨 **Automatic Model Export**: Extracts models on client/server startup
- 📦 **Organized Structure**: Models organized by namespace and type
- 🖼️ **Texture Extraction**: Exports textures with proper UV mapping
- 🔧 **Multiple Formats**: Exports to OBJ format (compatible with Blockbench, Blender, Cinema4D)
- 🎯 **Comprehensive Coverage**: Exports entities, items, and armor models
- 📝 **Metadata Files**: Includes info files for each model

## Installation

1. Download the mod JAR file
2. Place it in your Minecraft `mods` folder
3. Launch Minecraft with Forge 1.20.1

## Usage

The mod automatically runs when Minecraft starts. Models are exported to:

```
model_dumps/
├── minecraft/
│   ├── entity/
│   │   ├── zombie/
│   │   │   ├── zombie.obj
│   │   │   ├── zombie.mtl
│   │   │   ├── zombie.png
│   │   │   └── info.txt
│   │   └── ...
│   ├── item/
│   │   ├── diamond_sword/
│   │   │   ├── diamond_sword.obj
│   │   │   ├── diamond_sword.mtl
│   │   │   ├── diamond_sword.png
│   │   │   └── info.txt
│   │   └── ...
│   └── armor/
│       └── ...
└── modid/
    └── ...
```

## Exported Files

Each model folder contains:

- **`.obj`** - 3D model geometry
- **`.mtl`** - Material definition with texture references
- **`.png`** - Texture file with proper UV mapping
- **`info.txt`** - Model metadata and import instructions

## Using Exported Models

### Blockbench
1. File → Import → OBJ
2. Select the `.obj` file
3. Textures are automatically applied via the MTL file

### Blender
1. File → Import → Wavefront (.obj)
2. Select the `.obj` file
3. Textures are automatically loaded from the MTL file

### Cinema4D
1. File → Open
2. Select the `.obj` file
3. Materials and textures are imported automatically

## Building from Source

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`

## Configuration

Edit `ModConfig.java` to customize:
- Which model types to export (entities, items, armor)
- Whether to export vanilla/modded models
- Export format options
- File organization preferences

## Technical Details

- **Format**: Wavefront OBJ with MTL materials
- **UV Mapping**: Preserved from original models
- **Texture Format**: PNG
- **Compatibility**: Minecraft 1.20.1, Forge 47.2.0+

## For Thumbnail Artists

This mod is designed specifically for content creators who need Minecraft models for:
- YouTube thumbnails
- Server promotional art
- Mod showcase videos
- Custom renders

All models include proper UV mapping and textures, making them ready to use in your favorite 3D software.

## License

MIT License

## Support

For issues or suggestions, please open an issue on GitHub.

## Credits

Created for the Minecraft content creation community.
