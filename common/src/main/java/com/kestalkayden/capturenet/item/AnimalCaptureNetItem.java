package com.kestalkayden.capturenet.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import com.kestalkayden.capturenet.CaptureNetRefs;

/** Reusable capture-and-release item. Empty net + right-click on a capturable entity stores
 *  the entity's full NBT in a data component and removes it from the world. Filled net +
 *  right-click on a block face spawns the entity on top of that block and empties the net.
 *
 *  Capture allowed: passive/neutral living entities. Blocked: players, bosses (Ender Dragon,
 *  Wither, Warden), and anything in {@link MobCategory#MONSTER}. Hostile capture is reserved
 *  for a future reinforced net tier. */
public class AnimalCaptureNetItem extends Item {

    public AnimalCaptureNetItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        // Safety-net path. The loader pre-interact hooks (Fabric UseEntityCallback / NeoForge
        // PlayerInteractEvent.EntityInteract) are what actually win against mobs whose own
        // mobInteract consumes the click first — villagers (trade GUI), allays (item swap), and
        // any modded creature with custom right-click. This still captures normally if for some
        // reason that hook didn't fire (foreign call path, mod intercept).
        return tryCapture(stack, player, target, hand);
    }

    /** Capture {@code target} if it's eligible. SUCCESS on capture, PASS when the net is
     *  full / target is blocked / entity type is unknown. Safe to call from a pre-interact
     *  event: a PASS result means "let vanilla handle it" (so trade GUIs still open with a
     *  filled net, etc.); SUCCESS means "we consumed the click." */
    public static InteractionResult tryCapture(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target.level().isClientSide()) {
            return isCapturable(stack, target) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!isCapturable(stack, target)) return InteractionResult.PASS;

        // MC 26.1 routes entity save through ValueOutput; TagValueOutput is the CompoundTag-
        // backed impl. ProblemReporter.DISCARDING silently swallows validation issues —
        // appropriate here since we trust the entity is well-formed.
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,
            target.level().registryAccess());
        target.save(out);
        CompoundTag nbt = out.buildResult();
        Identifier typeId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (typeId == null) return InteractionResult.PASS;  // Unknown entity type — safety bail

        stack.set(CaptureNetRefs.CAPTURED_ENTITY.get(), new CapturedEntity(typeId, nbt));
        // Enchantment glint as a "this net is loaded" visual cue. The modern 1.21+/26.1
        // approach: set the vanilla glint-override component rather than overriding isFoil(),
        // which has flaky method-resolution under DataComponent refactors.
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        // Force the held-item slot to re-sync. In survival the next broadcastChanges
        // catches the component update, but creative mode's client-authoritative
        // inventory can drop it without this prod.
        player.setItemInHand(hand, stack);
        if (player.containerMenu != null) {
            player.containerMenu.broadcastChanges();
        }

        ServerLevel level = (ServerLevel) target.level();
        level.sendParticles(ParticleTypes.POOF,
            target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ(),
            16, 0.3, 0.3, 0.3, 0.05);
        level.playSound(null, target.blockPosition(),
            SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.6F, 1.0F);

        target.discard();
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        CapturedEntity captured = stack.get(CaptureNetRefs.CAPTURED_ENTITY.get());
        if (captured == null) return InteractionResult.PASS;
        if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;

        ServerLevel level = (ServerLevel) context.getLevel();
        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());

        // MC 26.2 routes entity load through ValueInput (mirror of the save-side ValueOutput above).
        ValueInput in = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), captured.entityNbt());
        Entity spawned = EntityType.loadEntityRecursive(in, level, EntitySpawnReason.LOAD, e -> {
            e.setPos(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5);
            return e;
        });
        if (spawned == null) return InteractionResult.PASS;

        if (!level.addFreshEntity(spawned)) return InteractionResult.PASS;

        level.sendParticles(ParticleTypes.CLOUD,
            spawned.getX(), spawned.getY() + 0.5, spawned.getZ(),
            12, 0.2, 0.2, 0.2, 0.02);
        level.playSound(null, spawnPos,
            SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.6F, 1.0F);

        stack.remove(CaptureNetRefs.CAPTURED_ENTITY.get());
        stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        if (context.getPlayer() != null) {
            Player p = context.getPlayer();
            p.setItemInHand(context.getHand(), stack);
            if (p.containerMenu != null) p.containerMenu.broadcastChanges();
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isCapturable(ItemStack stack, LivingEntity entity) {
        // One entity per net; eligibility is the shared rule the Capture Crate uses too.
        if (stack.has(CaptureNetRefs.CAPTURED_ENTITY.get())) return false;
        return CaptureRules.isEligible(entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                 Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        CapturedEntity captured = stack.get(CaptureNetRefs.CAPTURED_ENTITY.get());
        if (captured == null) {
            tooltip.accept(Component.translatable("item.capturenet.animal_capture_net.tooltip.empty")
                .withStyle(ChatFormatting.GRAY));
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(captured.entityType());
        if (type == null) return;

        CompoundTag nbt = captured.entityNbt();
        Component variant = EntityTooltipAdapters.variantFor(captured.entityType(), nbt);
        Component customName = readCustomName(context, nbt);
        boolean baby = EntityTooltipAdapters.isBaby(nbt);

        // Compose "[Name · ][Variant ]EntityType[ · baby]" into the %s arg of the contains key.
        MutableComponent contents = Component.empty();
        if (customName != null) {
            contents.append(customName).append(Component.literal(" · "));
        }
        if (variant != null) {
            contents.append(variant).append(Component.literal(" "));
        }
        contents.append(type.getDescription());
        if (baby) {
            contents.append(Component.literal(" · "))
                    .append(Component.translatable("capturenet.tooltip.baby"));
        }

        tooltip.accept(Component.translatable("item.capturenet.animal_capture_net.tooltip.contains",
            contents).withStyle(ChatFormatting.AQUA));
    }

    /** CustomName in saved entity NBT is stored as a component-as-tag (string for legacy, compound
     *  for modern). Decode via ComponentSerialization with registry-aware ops so style refs resolve.
     *  Returns null on any parse failure — tooltip falls back to no name. */
    private static Component readCustomName(TooltipContext context, CompoundTag nbt) {
        if (!nbt.contains("CustomName")) return null;
        try {
            Tag tag = nbt.get("CustomName");
            if (tag == null) return null;
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, context.registries());
            return ComponentSerialization.CODEC.parse(ops, tag).result().orElse(null);
        } catch (Throwable t) {
            return null;
        }
    }
}
