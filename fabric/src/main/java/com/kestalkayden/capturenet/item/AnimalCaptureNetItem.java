package com.kestalkayden.capturenet.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.storage.TagValueOutput;

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

        stack.set(CaptureNetDataComponents.CAPTURED_ENTITY, new CapturedEntity(typeId, nbt));
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
            SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 0.5F, 1.2F);

        target.discard();
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        CapturedEntity captured = stack.get(CaptureNetDataComponents.CAPTURED_ENTITY);
        if (captured == null) return InteractionResult.PASS;
        if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;

        ServerLevel level = (ServerLevel) context.getLevel();
        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());

        Entity spawned = EntityType.loadEntityRecursive(captured.entityNbt(), level, EntitySpawnReason.LOAD, e -> {
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
            SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 0.5F, 0.9F);

        stack.remove(CaptureNetDataComponents.CAPTURED_ENTITY);
        if (context.getPlayer() != null) {
            Player p = context.getPlayer();
            p.setItemInHand(context.getHand(), stack);
            if (p.containerMenu != null) p.containerMenu.broadcastChanges();
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isCapturable(ItemStack stack, LivingEntity entity) {
        if (stack.has(CaptureNetDataComponents.CAPTURED_ENTITY)) return false;
        if (entity instanceof Player) return false;
        if (entity instanceof EnderDragon) return false;
        if (entity instanceof WitherBoss) return false;
        if (entity instanceof Warden) return false;
        return entity.getType().getCategory() != MobCategory.MONSTER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                 Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        CapturedEntity captured = stack.get(CaptureNetDataComponents.CAPTURED_ENTITY);
        if (captured == null) {
            tooltip.accept(Component.translatable("item.capturenet.animal_capture_net.tooltip.empty")
                .withStyle(ChatFormatting.GRAY));
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(captured.entityType());
        if (type != null) {
            tooltip.accept(Component.translatable("item.capturenet.animal_capture_net.tooltip.contains",
                type.getDescription()).withStyle(ChatFormatting.AQUA));
        }
    }
}
