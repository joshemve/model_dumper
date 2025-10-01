package com.modeldumper;

import com.modeldumper.client.ClientEventHandler;
import com.modeldumper.exporter.ModelExportManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ModelDumper.MODID)
public class ModelDumper {
    public static final String MODID = "modeldumper";
    public static final Logger LOGGER = LoggerFactory.getLogger(ModelDumper.class);

    public ModelDumper() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::onClientSetup);
        }

        modEventBus.addListener(this::onServerSetup);

        LOGGER.info("Model Dumper initialized");
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Model Dumper Client Setup - registering event handler");
        // Register client event handler to trigger after world loads
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }

    private void onServerSetup(final FMLDedicatedServerSetupEvent event) {
        LOGGER.info("Starting model dump on SERVER...");
        event.enqueueWork(() -> {
            ModelExportManager.exportAllModels(false);
        });
    }
}
