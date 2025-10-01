package com.modeldumper.util;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;

public class ModelData {
    public enum ModelType {
        ENTITY,
        ITEM,
        ARMOR
    }

    public String name;
    public ModelType type;
    public String namespace;

    // For entities
    public EntityModel<?> model;
    public ResourceLocation textureLocation;

    // For items
    public BakedModel bakedModel;
    public ResourceLocation particleTexture;

    // For armor
    public ArmorItem armorItem;
    public ResourceLocation armorTextureLocation;
}
