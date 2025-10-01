package com.modeldumper.config;

public class ModConfig {
    // Export configuration options
    public static final boolean EXPORT_ENTITIES = true;
    public static final boolean EXPORT_ITEMS = true;
    public static final boolean EXPORT_ARMOR = true;

    public static final boolean EXPORT_VANILLA_MODELS = true;
    public static final boolean EXPORT_MODDED_MODELS = true;

    public static final String EXPORT_FORMAT = "OBJ"; // OBJ is widely supported

    // File organization
    public static final boolean ORGANIZE_BY_NAMESPACE = true;
    public static final boolean ORGANIZE_BY_TYPE = true;

    // Texture export
    public static final boolean EXPORT_TEXTURES = true;
    public static final boolean GENERATE_MTL_FILES = true;

    // Info files
    public static final boolean GENERATE_INFO_FILES = true;
    public static final boolean GENERATE_README = true;
}
