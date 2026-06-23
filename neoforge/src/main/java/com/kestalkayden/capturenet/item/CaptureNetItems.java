package com.kestalkayden.capturenet.item;

import com.kestalkayden.capturenet.CaptureNetNeoForge;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CaptureNetItems {

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(CaptureNetNeoForge.MOD_ID);

    // 1.21.1 has no Item.Properties.setId (added in 1.21.2); DeferredRegister.Items binds the id.
    public static final DeferredItem<Item> ANIMAL_CAPTURE_NET = ITEMS.register("animal_capture_net",
        id -> new AnimalCaptureNetItem(new Item.Properties().stacksTo(1)));

    private CaptureNetItems() {}
}
