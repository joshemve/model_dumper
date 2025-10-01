package com.modeldumper.scanner;

import com.modeldumper.ModelDumper;
import com.modeldumper.util.ModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class ItemModelScanner {

    public static List<ModelData> scanItemModels(boolean isClient) {
        List<ModelData> models = new ArrayList<>();

        if (!isClient) {
            ModelDumper.LOGGER.info("Server-side item scanning not available");
            return models;
        }

        try {
            Minecraft minecraft = Minecraft.getInstance();
            ModelManager modelManager = minecraft.getModelManager();

            ModelDumper.LOGGER.info("Scanning item models...");

            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                try {
                    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
                    String itemName = itemId.toString();

                    ItemStack stack = new ItemStack(item);
                    BakedModel bakedModel = minecraft.getItemRenderer().getModel(stack, null, null, 0);

                    if (bakedModel != null && !bakedModel.isCustomRenderer()) {
                        ModelData modelData = new ModelData();
                        modelData.name = itemName.replace(":", "_");
                        modelData.type = ModelData.ModelType.ITEM;
                        modelData.bakedModel = bakedModel;
                        modelData.namespace = itemId.getNamespace();

                        // Try to get particle texture
                        try {
                            var particleIcon = bakedModel.getParticleIcon();
                            if (particleIcon != null) {
                                modelData.particleTexture = particleIcon.contents().name();
                            }
                        } catch (Exception e) {
                            // Ignore
                        }

                        models.add(modelData);
                        ModelDumper.LOGGER.debug("Found item model: " + itemName);
                    }
                } catch (Exception e) {
                    // Skip items that cause issues
                }
            }

            ModelDumper.LOGGER.info("Found {} item models", models.size());
        } catch (Exception e) {
            ModelDumper.LOGGER.error("Error scanning item models", e);
        }

        return models;
    }
}
