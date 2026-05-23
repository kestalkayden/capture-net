package com.kestalkayden.capturenet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.capturenet.item.CaptureNetDataComponents;
import com.kestalkayden.capturenet.item.CaptureNetItems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;

public class CaptureNetFabric implements ModInitializer {
    public static final String MOD_ID = "capturenet";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Capture Net (Fabric)");

        CaptureNetDataComponents.register();
        CaptureNetItems.register();

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
            output.accept(CaptureNetItems.ANIMAL_CAPTURE_NET);
        });
    }
}
