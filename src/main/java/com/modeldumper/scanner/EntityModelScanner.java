package com.modeldumper.scanner;

import com.modeldumper.ModelDumper;
import com.modeldumper.util.ModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityModelScanner {

    public static List<ModelData> scanEntityModels(boolean isClient) {
        List<ModelData> models = new ArrayList<>();

        if (!isClient) {
            ModelDumper.LOGGER.info("Server-side entity scanning not available");
            return models;
        }

        try {
            Minecraft minecraft = Minecraft.getInstance();
            var entityRenderDispatcher = minecraft.getEntityRenderDispatcher();

            ModelDumper.LOGGER.info("Scanning entity models...");

            for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues()) {
                try {
                    ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
                    String entityName = entityId.toString();

                    // Skip vanilla Minecraft entities
                    if (entityId.getNamespace().equals("minecraft")) {
                        continue;
                    }

                    // Get renderer directly from the entity type without creating an entity instance
                    EntityRenderer<?> renderer = entityRenderDispatcher.renderers.get(entityType);

                    if (renderer == null) {
                        continue;
                    }

                    if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
                        EntityModel<?> model = livingRenderer.getModel();

                        ModelData modelData = new ModelData();
                        modelData.name = entityName.replace(":", "_");
                        modelData.type = ModelData.ModelType.ENTITY;
                        modelData.model = model;
                        modelData.namespace = entityId.getNamespace();

                        // Extract texture location
                        try {
                            modelData.textureLocation = livingRenderer.getTextureLocation(null);
                        } catch (Exception e) {
                            ModelDumper.LOGGER.warn("Could not get texture for entity: " + entityName);
                        }

                        models.add(modelData);
                        ModelDumper.LOGGER.debug("Found entity model: " + entityName);
                    }
                } catch (Exception e) {
                    // Some entities may not be accessible, skip them
                    ModelDumper.LOGGER.debug("Skipped entity: " + e.getMessage());
                }
            }

            if (models.isEmpty()) {
                ModelDumper.LOGGER.info("No modded entity models found");
            } else {
                ModelDumper.LOGGER.info("Found {} modded entity models", models.size());
            }
        } catch (Exception e) {
            ModelDumper.LOGGER.error("Error scanning entity models", e);
        }

        return models;
    }
}
