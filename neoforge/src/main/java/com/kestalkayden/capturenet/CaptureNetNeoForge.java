package com.kestalkayden.capturenet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.capturenet.item.AnimalCaptureNetItem;
import com.kestalkayden.capturenet.item.CaptureNetDataComponents;
import com.kestalkayden.capturenet.item.CaptureNetItems;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(CaptureNetNeoForge.MOD_ID)
public class CaptureNetNeoForge {
    public static final String MOD_ID = "capturenet";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public CaptureNetNeoForge(IEventBus modBus) {
        LOGGER.info("Initializing Capture Net (NeoForge)");

        CaptureNetItems.ITEMS.register(modBus);
        CaptureNetDataComponents.COMPONENTS.register(modBus);

        modBus.addListener(CaptureNetNeoForge::onBuildCreativeTabs);
        // Game-event bus, not the mod bus: PlayerInteractEvent fires on the NeoForge bus.
        NeoForge.EVENT_BUS.addListener(CaptureNetNeoForge::onEntityInteract);
    }

    private static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CaptureNetItems.ANIMAL_CAPTURE_NET.get());
        }
    }

    /** Pre-interact hook: fires before entity.interact(), so the net wins against mobs
     *  whose own mobInteract would otherwise consume the click — villagers (trade GUI),
     *  allays (accept-item), modded creatures with custom right-click. interactLivingEntity
     *  alone isn't enough: vanilla Player.interactOn calls entity.interact() first, and any
     *  SUCCESS there short-circuits the item handler. */
    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof LivingEntity living)) return;
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof AnimalCaptureNetItem)) return;
        InteractionResult result = AnimalCaptureNetItem.tryCapture(
            stack, event.getEntity(), living, event.getHand());
        if (result.consumesAction()) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }
}
