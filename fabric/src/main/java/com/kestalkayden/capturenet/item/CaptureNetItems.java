package com.kestalkayden.capturenet.item;

import com.kestalkayden.capturenet.CaptureNetFabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class CaptureNetItems {

    public static Item ANIMAL_CAPTURE_NET;

    private CaptureNetItems() {}

    public static void register() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CaptureNetFabric.MOD_ID, "animal_capture_net");
        // 1.21.1 has no Item.Properties.setId (added in 1.21.2) — the registry name comes from
        // the Registry.register key alone.
        ANIMAL_CAPTURE_NET = Registry.register(BuiltInRegistries.ITEM, id,
            new AnimalCaptureNetItem(new Item.Properties().stacksTo(1)));
    }
}
