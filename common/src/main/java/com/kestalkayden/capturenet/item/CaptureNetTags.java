package com.kestalkayden.capturenet.item;

import com.kestalkayden.capturenet.CaptureNet;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/** Tag keys for modpack-author overrides to the default capture filter. Both tags ship empty
 *  in this mod's data — modpacks add entries via their own datapack files. */
public final class CaptureNetTags {

    /** Entity types in this tag are NEVER capturable, even if they're a passive category.
     *  Lets modpack authors blocklist modded "bosses" or special encounters. */
    public static final TagKey<EntityType<?>> CANNOT_CAPTURE = TagKey.create(
        Registries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(CaptureNet.MOD_ID, "cannot_capture"));

    /** Entity types in this tag ARE capturable even if their MobCategory is MONSTER. Lets
     *  modpack authors whitelist a modded mob that the modder labeled as MONSTER but the pack
     *  considers safe (e.g. friendly companion mobs). */
    public static final TagKey<EntityType<?>> ALWAYS_CAPTURABLE = TagKey.create(
        Registries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(CaptureNet.MOD_ID, "always_capturable"));

    private CaptureNetTags() {}
}
