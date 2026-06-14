package com.kestalkayden.capturenet;

import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentType;

import com.kestalkayden.capturenet.item.CapturedEntity;

/** Loader-agnostic accessor for the one registered object the shared item logic needs — the
 *  captured-entity data component. Each loader binds this lazy supplier from its own registration
 *  (Fabric registers directly; NeoForge via DeferredRegister) in its initializer, and shared
 *  {@code common} code reads it through {@link Supplier#get()}.
 *
 *  <p>This single indirection is what lets {@link com.kestalkayden.capturenet.item.AnimalCaptureNetItem}
 *  live in {@code common} without importing the per-loader {@code CaptureNetDataComponents} (whose
 *  field type differs between loaders). The supplier is lazy, so a loader may bind it before its
 *  deferred registry has fired; resolution happens at first {@code get()}, which is always well
 *  after registration — and capture/release only ever {@code get()} at interaction time. */
public final class CaptureNetRefs {
    private CaptureNetRefs() {}

    public static Supplier<DataComponentType<CapturedEntity>> CAPTURED_ENTITY;
}
