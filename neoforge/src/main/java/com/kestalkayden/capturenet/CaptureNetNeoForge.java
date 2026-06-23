package com.kestalkayden.capturenet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.capturenet.item.AnimalCaptureNetItem;
import com.kestalkayden.capturenet.item.CaptureCrateItem;
import com.kestalkayden.capturenet.item.CaptureNetDataComponents;
import com.kestalkayden.capturenet.item.CaptureNetItems;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
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

        // Bind the loader-agnostic ref (lazy — resolves after the deferred registry fires) so
        // shared common code reaches the data component without importing NeoForge registration.
        CaptureNetRefs.CAPTURED_ENTITY = () -> CaptureNetDataComponents.CAPTURED_ENTITY;
        CaptureNetRefs.CONTAINED_ENTITIES = () -> CaptureNetDataComponents.CONTAINED_ENTITIES;

        modBus.addListener(CaptureNetNeoForge::onBuildCreativeTabs);
        // Game-event bus, not the mod bus: PlayerInteractEvent fires on the NeoForge bus.
        NeoForge.EVENT_BUS.addListener(CaptureNetNeoForge::onEntityInteract);
    }

    private static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CaptureNetItems.ANIMAL_CAPTURE_NET.get());
            event.accept(CaptureNetItems.CAPTURE_CRATE.get());
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
        Item item = stack.getItem();
        InteractionResult result;
        if (item instanceof AnimalCaptureNetItem) {
            result = AnimalCaptureNetItem.tryCapture(stack, event.getEntity(), living, event.getHand());
        } else if (item instanceof CaptureCrateItem) {
            result = CaptureCrateItem.tryCapture(stack, event.getEntity(), living, event.getHand());
        } else {
            return;
        }
        if (result.consumesAction()) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }
}
