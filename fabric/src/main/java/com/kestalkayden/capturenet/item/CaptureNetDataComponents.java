package com.kestalkayden.capturenet.item;

import com.kestalkayden.capturenet.CaptureNetFabric;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class CaptureNetDataComponents {

    public static DataComponentType<CapturedEntity> CAPTURED_ENTITY;
    public static DataComponentType<ContainedEntities> CONTAINED_ENTITIES;

    private CaptureNetDataComponents() {}

    public static void register() {
        CAPTURED_ENTITY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(CaptureNetFabric.MOD_ID, "captured_entity"),
            DataComponentType.<CapturedEntity>builder()
                .persistent(CapturedEntity.CODEC)
                .networkSynchronized(CapturedEntity.STREAM_CODEC)
                .build());

        CONTAINED_ENTITIES = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(CaptureNetFabric.MOD_ID, "contained_entities"),
            DataComponentType.<ContainedEntities>builder()
                .persistent(ContainedEntities.CODEC)
                .networkSynchronized(ContainedEntities.STREAM_CODEC)
                .build());
    }
}
