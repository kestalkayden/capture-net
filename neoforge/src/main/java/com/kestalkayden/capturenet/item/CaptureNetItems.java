package com.kestalkayden.capturenet.item;

import com.kestalkayden.capturenet.CaptureNetNeoForge;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CaptureNetItems {

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(CaptureNetNeoForge.MOD_ID);

    public static final DeferredItem<Item> ANIMAL_CAPTURE_NET = ITEMS.register("animal_capture_net", id -> {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return new AnimalCaptureNetItem(new Item.Properties().setId(key).stacksTo(1));
    });

    public static final DeferredItem<Item> CAPTURE_CRATE = ITEMS.register("capture_crate", id -> {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return new CaptureCrateItem(new Item.Properties().setId(key).stacksTo(1));
    });

    private CaptureNetItems() {}
}
