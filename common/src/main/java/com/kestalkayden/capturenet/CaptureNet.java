package com.kestalkayden.capturenet;

/** Shared constants resolvable in the platform-agnostic {@code common} source set. The per-loader
 *  entry points keep their own {@code MOD_ID} too — NeoForge's {@code @Mod} annotation needs a
 *  compile-time constant and Fabric correlates it with {@code fabric.mod.json} — but anything in
 *  {@code common} (e.g. {@link com.kestalkayden.capturenet.item.CaptureNetTags}) reads it here. */
public final class CaptureNet {
    public static final String MOD_ID = "capturenet";

    private CaptureNet() {}
}
