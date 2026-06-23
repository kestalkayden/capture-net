package com.kestalkayden.capturenet.item;

import com.kestalkayden.capturenet.CaptureNetFabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class CaptureNetItems {

    public static Item ANIMAL_CAPTURE_NET;

    private CaptureNetItems() {}

    public static void register() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CaptureNetFabric.MOD_ID, "animal_capture_net");
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        ANIMAL_CAPTURE_NET = Registry.register(BuiltInRegistries.ITEM, id,
            new AnimalCaptureNetItem(new Item.Properties().setId(key).stacksTo(1)));
    }
}
