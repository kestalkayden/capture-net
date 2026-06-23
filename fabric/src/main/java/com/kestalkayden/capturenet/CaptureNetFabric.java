package com.kestalkayden.capturenet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.capturenet.item.AnimalCaptureNetItem;
import com.kestalkayden.capturenet.item.CaptureCrateItem;
import com.kestalkayden.capturenet.item.CaptureNetDataComponents;
import com.kestalkayden.capturenet.item.CaptureNetItems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CaptureNetFabric implements ModInitializer {
    public static final String MOD_ID = "capturenet";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Capture Net (Fabric)");

        CaptureNetDataComponents.register();
        CaptureNetItems.register();

        // Bind the loader-agnostic ref so shared common code (AnimalCaptureNetItem) reaches the
        // data component without importing this loader's registration class.
        CaptureNetRefs.CAPTURED_ENTITY = () -> CaptureNetDataComponents.CAPTURED_ENTITY;
        CaptureNetRefs.CONTAINED_ENTITIES = () -> CaptureNetDataComponents.CONTAINED_ENTITIES;

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
            output.accept(CaptureNetItems.ANIMAL_CAPTURE_NET);
            output.accept(CaptureNetItems.CAPTURE_CRATE);
        });

        // Pre-interact hook: runs before entity.interact(), so the net wins against mobs
        // whose own mobInteract would otherwise consume the click — villagers (trade GUI),
        // allays (accept-item), modded creatures with custom right-click. interactLivingEntity
        // alone isn't enough: vanilla Player.interactOn calls entity.interact() first, and
        // any SUCCESS there short-circuits the item handler.
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(entity instanceof LivingEntity living)) return InteractionResult.PASS;
            ItemStack stack = player.getItemInHand(hand);
            Item item = stack.getItem();
            if (item instanceof AnimalCaptureNetItem) {
                return AnimalCaptureNetItem.tryCapture(stack, player, living, hand);
            }
            if (item instanceof CaptureCrateItem) {
                return CaptureCrateItem.tryCapture(stack, player, living, hand);
            }
            return InteractionResult.PASS;
        });
    }
}
