package com.kestalkayden.capturenet.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;

/** Shared capture-eligibility filter, used by both the single Capture Net and the Containment
 *  Box so they catch exactly the same set of entities. This is purely the per-entity rule; the
 *  "is there room?" check (single net already loaded / box at capacity) lives on each item.
 *
 *  <p>Allowed: passive/neutral living entities, plus anything a modpack whitelists via
 *  {@code always_capturable}. Blocked: players, bosses (Ender Dragon, Wither, Warden), the
 *  {@code cannot_capture} blocklist, and the {@link MobCategory#MONSTER} category. */
public final class CaptureRules {

    private CaptureRules() {}

    public static boolean isEligible(LivingEntity entity) {
        // Hardcoded absolute blocks — not overridable by the always_capturable tag.
        if (entity instanceof Player) return false;
        if (entity instanceof EnderDragon) return false;
        if (entity instanceof WitherBoss) return false;
        if (entity instanceof Warden) return false;

        EntityType<?> type = entity.getType();
        // Datapack blocklist takes precedence over the whitelist below — both can target the
        // same entity, in which case the modpack author's intent is "block this."
        if (type.builtInRegistryHolder().is(CaptureNetTags.CANNOT_CAPTURE)) return false;
        if (type.builtInRegistryHolder().is(CaptureNetTags.ALWAYS_CAPTURABLE)) return true;
        return type.getCategory() != MobCategory.MONSTER;
    }
}
