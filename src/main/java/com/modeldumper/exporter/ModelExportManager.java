package com.modeldumper.exporter;

import com.modeldumper.ModelDumper;
import com.modeldumper.scanner.ArmorModelScanner;
import com.modeldumper.scanner.EntityModelScanner;
import com.modeldumper.scanner.ItemModelScanner;
import com.modeldumper.util.ModelData;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ModelExportManager {

    private static final String EXPORT_ROOT = "model_dumps";

    public static void exportAllModels(boolean isClient) {
        ModelDumper.LOGGER.info("=== Starting Model Dump ===");
        ModelDumper.LOGGER.info("Client: " + isClient);

        // Create export directory structure
        File exportRoot = new File(EXPORT_ROOT);
        exportRoot.mkdirs();

        // Scan all models
        List<ModelData> allModels = new ArrayList<>();

        ModelDumper.LOGGER.info("Scanning entities...");
        allModels.addAll(EntityModelScanner.scanEntityModels(isClient));

        ModelDumper.LOGGER.info("Scanning items...");
        allModels.addAll(ItemModelScanner.scanItemModels(isClient));

        ModelDumper.LOGGER.info("Scanning armor...");
        allModels.addAll(ArmorModelScanner.scanArmorModels(isClient));

        ModelDumper.LOGGER.info("Total models found: " + allModels.size());

        // Export each model
        int exported = 0;
        for (ModelData modelData : allModels) {
            try {
                exportModel(modelData, exportRoot);
                exported++;
            } catch (Exception e) {
                ModelDumper.LOGGER.error("Failed to export model: " + modelData.name, e);
            }
        }

        ModelDumper.LOGGER.info("=== Model Dump Complete ===");
        ModelDumper.LOGGER.info("Exported: " + exported + " / " + allModels.size() + " models");
        ModelDumper.LOGGER.info("Location: " + exportRoot.getAbsolutePath());
    }

    private static void exportModel(ModelData modelData, File exportRoot) {
        // Create organized folder structure: model_dumps/<namespace>/<type>/<model_name>/
        String typeFolder = modelData.type.name().toLowerCase();
        File modelDir = new File(exportRoot, modelData.namespace + "/" + typeFolder + "/" + modelData.name);
        modelDir.mkdirs();

        File objFile = new File(modelDir, modelData.name + ".obj");
        File mtlFile = new File(modelDir, modelData.name + ".mtl");
        File textureFile = new File(modelDir, modelData.name + ".png");

        // Export based on type
        switch (modelData.type) {
            case ENTITY:
                if (modelData.model != null) {
                    OBJExporter.exportEntityModel(modelData, objFile, mtlFile);
                    if (modelData.textureLocation != null) {
                        TextureExtractor.extractTexture(modelData, textureFile);
                    }
                }
                break;

            case ITEM:
                if (modelData.bakedModel != null) {
                    OBJExporter.exportItemModel(modelData, objFile, mtlFile);
                    if (modelData.particleTexture != null) {
                        TextureExtractor.extractTexture(modelData, textureFile);
                    }
                }
                break;

            case ARMOR:
                if (modelData.armorItem != null) {
                    // Armor models are humanoid models, we'll export a reference to the texture
                    // and create a simple readme explaining how to use it
                    File readmeFile = new File(modelDir, "README.txt");
                    try (java.io.FileWriter writer = new java.io.FileWriter(readmeFile)) {
                        writer.write("Armor Model: " + modelData.name + "\n");
                        writer.write("Type: " + modelData.armorItem.getEquipmentSlot() + "\n");
                        writer.write("Material: " + modelData.armorItem.getMaterial().getName() + "\n");
                        writer.write("\n");
                        writer.write("Armor uses the standard Minecraft humanoid model.\n");
                        writer.write("Import the texture into your 3D software and apply it to a humanoid armor model.\n");
                        writer.write("Texture location: " + modelData.armorTextureLocation + "\n");
                    } catch (Exception e) {
                        ModelDumper.LOGGER.error("Error writing armor README", e);
                    }
                    if (modelData.armorTextureLocation != null) {
                        TextureExtractor.extractArmorTexture(modelData.armorTextureLocation, textureFile);
                    }
                }
                break;
        }

        // Create info file with metadata
        File infoFile = new File(modelDir, "info.txt");
        try (java.io.FileWriter writer = new java.io.FileWriter(infoFile)) {
            writer.write("Model Name: " + modelData.name + "\n");
            writer.write("Type: " + modelData.type + "\n");
            writer.write("Namespace: " + modelData.namespace + "\n");
            writer.write("Export Format: OBJ with MTL\n");
            writer.write("\n");
            writer.write("Files:\n");
            writer.write("- " + modelData.name + ".obj (3D model)\n");
            writer.write("- " + modelData.name + ".mtl (material definition)\n");
            writer.write("- " + modelData.name + ".png (texture)\n");
            writer.write("\n");
            writer.write("Compatible with: Blockbench, Blender, Cinema4D, and other 3D software\n");
        } catch (Exception e) {
            ModelDumper.LOGGER.error("Error writing info file", e);
        }
    }
}
