package com.modeldumper.exporter;

import com.modeldumper.ModelDumper;
import com.modeldumper.util.ModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class TextureExtractor {

    public static void extractTexture(ModelData modelData, File outputFile) {
        ResourceLocation textureLocation = getTextureLocation(modelData);

        if (textureLocation == null) {
            ModelDumper.LOGGER.warn("No texture location for model: " + modelData.name);
            return;
        }

        try {
            Minecraft minecraft = Minecraft.getInstance();
            ResourceManager resourceManager = minecraft.getResourceManager();

            // Convert texture location to resource path
            ResourceLocation resourcePath = new ResourceLocation(
                textureLocation.getNamespace(),
                "textures/" + textureLocation.getPath() + ".png"
            );

            // If it already has textures/ prefix, use as is
            if (textureLocation.getPath().startsWith("textures/")) {
                resourcePath = textureLocation;
            }

            Optional<Resource> resourceOpt = resourceManager.getResource(resourcePath);

            if (resourceOpt.isPresent()) {
                Resource resource = resourceOpt.get();
                try (InputStream is = resource.open()) {
                    BufferedImage image = ImageIO.read(is);
                    if (image != null) {
                        ImageIO.write(image, "PNG", outputFile);
                        ModelDumper.LOGGER.debug("Extracted texture: " + textureLocation + " to " + outputFile.getName());
                    } else {
                        ModelDumper.LOGGER.warn("Could not read image for: " + textureLocation);
                    }
                }
            } else {
                ModelDumper.LOGGER.warn("Could not find texture resource: " + resourcePath);
            }

        } catch (Exception e) {
            ModelDumper.LOGGER.error("Error extracting texture for: " + modelData.name, e);
        }
    }

    private static ResourceLocation getTextureLocation(ModelData modelData) {
        switch (modelData.type) {
            case ENTITY:
                return modelData.textureLocation;
            case ITEM:
                return modelData.particleTexture;
            case ARMOR:
                return modelData.armorTextureLocation;
            default:
                return null;
        }
    }

    public static void extractArmorTexture(ResourceLocation textureLocation, File outputFile) {
        if (textureLocation == null) {
            return;
        }

        try {
            Minecraft minecraft = Minecraft.getInstance();
            ResourceManager resourceManager = minecraft.getResourceManager();

            Optional<Resource> resourceOpt = resourceManager.getResource(textureLocation);

            if (resourceOpt.isPresent()) {
                Resource resource = resourceOpt.get();
                try (InputStream is = resource.open()) {
                    BufferedImage image = ImageIO.read(is);
                    if (image != null) {
                        ImageIO.write(image, "PNG", outputFile);
                        ModelDumper.LOGGER.debug("Extracted armor texture: " + textureLocation);
                    }
                }
            } else {
                ModelDumper.LOGGER.warn("Could not find armor texture: " + textureLocation);
            }

        } catch (Exception e) {
            ModelDumper.LOGGER.error("Error extracting armor texture: " + textureLocation, e);
        }
    }
}
