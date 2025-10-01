package com.modeldumper.scanner;

import com.modeldumper.ModelDumper;
import com.modeldumper.util.ModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class ArmorModelScanner {

    public static List<ModelData> scanArmorModels(boolean isClient) {
        List<ModelData> models = new ArrayList<>();

        if (!isClient) {
            ModelDumper.LOGGER.info("Server-side armor scanning not available");
            return models;
        }

        try {
            Minecraft minecraft = Minecraft.getInstance();

            ModelDumper.LOGGER.info("Scanning armor models...");

            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                try {
                    if (item instanceof ArmorItem armorItem) {
                        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
                        String itemName = itemId.toString();

                        ModelData modelData = new ModelData();
                        modelData.name = itemName.replace(":", "_");
                        modelData.type = ModelData.ModelType.ARMOR;
                        modelData.armorItem = armorItem;
                        modelData.namespace = itemId.getNamespace();

                        // Get armor texture location
                        String armorMaterial = armorItem.getMaterial().getName();
                        EquipmentSlot slot = armorItem.getEquipmentSlot();

                        // Armor textures are typically in assets/<namespace>/textures/models/armor/
                        // Format: <material>_layer_1.png for helmet, chestplate, boots
                        // Format: <material>_layer_2.png for leggings
                        int layer = (slot == EquipmentSlot.LEGS) ? 2 : 1;

                        ResourceLocation textureLocation = new ResourceLocation(
                            itemId.getNamespace(),
                            "textures/models/armor/" + armorMaterial + "_layer_" + layer + ".png"
                        );

                        modelData.armorTextureLocation = textureLocation;

                        models.add(modelData);
                        ModelDumper.LOGGER.debug("Found armor model: " + itemName);
                    }
                } catch (Exception e) {
                    // Skip items that cause issues
                }
            }

            ModelDumper.LOGGER.info("Found {} armor models", models.size());
        } catch (Exception e) {
            ModelDumper.LOGGER.error("Error scanning armor models", e);
        }

        return models;
    }
}
