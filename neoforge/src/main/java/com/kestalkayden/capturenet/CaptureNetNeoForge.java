package com.kestalkayden.capturenet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.capturenet.item.CaptureNetDataComponents;
import com.kestalkayden.capturenet.item.CaptureNetItems;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(CaptureNetNeoForge.MOD_ID)
public class CaptureNetNeoForge {
    public static final String MOD_ID = "capturenet";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public CaptureNetNeoForge(IEventBus modBus) {
        LOGGER.info("Initializing Capture Net (NeoForge)");

        CaptureNetItems.ITEMS.register(modBus);
        CaptureNetDataComponents.COMPONENTS.register(modBus);

        modBus.addListener(CaptureNetNeoForge::onBuildCreativeTabs);
    }

    private static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CaptureNetItems.ANIMAL_CAPTURE_NET.get());
        }
    }
}
