package com.modeldumper.client;

import com.modeldumper.ModelDumper;
import com.modeldumper.exporter.ModelExportManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public class ClientEventHandler {

    private static boolean hasExported = false;

    @SubscribeEvent
    public void onScreenOpen(ScreenEvent.Opening event) {
        // Export models when the title screen opens for the first time
        // This ensures all resources are loaded
        if (!hasExported && event.getScreen() instanceof TitleScreen) {
            hasExported = true;

            ModelDumper.LOGGER.info("Title screen detected, starting model export...");

            // Run in a separate thread to avoid blocking the UI
            new Thread(() -> {
                try {
                    // Small delay to ensure everything is fully initialized
                    Thread.sleep(1000);

                    // Run on render thread
                    Minecraft.getInstance().execute(() -> {
                        ModelExportManager.exportAllModels(true);
                    });
                } catch (InterruptedException e) {
                    ModelDumper.LOGGER.error("Model export interrupted", e);
                }
            }, "Model-Export-Thread").start();
        }
    }
}