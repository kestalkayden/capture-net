package com.kestalkayden.capturenet.item;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/** One entity stored inside a Containment Box.
 *
 *  <p>The split between the persistent and network forms is deliberate (the "B" architecture):
 *  <ul>
 *    <li>{@code nbt} — the entity's full save NBT (trades, inventory, attributes). Kept only in
 *        the {@link #CODEC persistent} form, so it lives on disk / server-side. Release reads it
 *        back via {@code loadEntityRecursive}, which only ever happens on the server.</li>
 *    <li>{@code name}/{@code variant}/{@code baby} — cheap display bits, precomputed at capture
 *        time (when we still hold the live NBT on the server) so the tooltip never has to touch
 *        the raw NBT client-side.</li>
 *  </ul>
 *  The {@link #STREAM_CODEC network} form sends everything EXCEPT {@code nbt} and reconstructs the
 *  client copy with an empty tag — so a full Containment Box ships its display info, not ten mob
 *  blobs, on every inventory sync. */
public record ContainedEntity(Identifier type, CompoundTag nbt,
                              Optional<Component> name, Optional<Component> variant, boolean baby) {

    /** Persistent (disk / server) form — round-trips every field, including the heavy {@code nbt}. */
    public static final Codec<ContainedEntity> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Identifier.CODEC.fieldOf("type").forGetter(ContainedEntity::type),
            CompoundTag.CODEC.fieldOf("nbt").forGetter(ContainedEntity::nbt),
            ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(ContainedEntity::name),
            ComponentSerialization.CODEC.optionalFieldOf("variant").forGetter(ContainedEntity::variant),
            Codec.BOOL.optionalFieldOf("baby", false).forGetter(ContainedEntity::baby)
        ).apply(instance, ContainedEntity::new));

    /** Network (client) form — drops {@code nbt} (decodes it as an empty tag) and ships only the
     *  display bits. The client never needs the real NBT: it renders tooltip/bar/model, and
     *  release happens server-side where the persistent copy still has the full tag. */
    public static final StreamCodec<RegistryFriendlyByteBuf, ContainedEntity> STREAM_CODEC =
        StreamCodec.composite(
            Identifier.STREAM_CODEC, ContainedEntity::type,
            ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), ContainedEntity::name,
            ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), ContainedEntity::variant,
            ByteBufCodecs.BOOL, ContainedEntity::baby,
            (type, name, variant, baby) -> new ContainedEntity(type, new CompoundTag(), name, variant, baby));
}
