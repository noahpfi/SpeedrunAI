package com.swirb.speedrunai.utils;

import com.swirb.speedrunai.client.Client;
import com.swirb.speedrunai.main.SpeedrunAI;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.util.RayTraceResult;

public class MouseUtils {

    private final Client client;
    private boolean isDestroying = false;
    private BlockPos destroyPos = BlockPos.ZERO;
    private float destroyProgress = 0.0F;
    private float digTicks = 0.0F;

    public MouseUtils(Client client) {
        this.client = client;
    }

    public boolean isDestroying() {
        return this.isDestroying;
    }

    public void startDestroying() {
        RayTraceResult r = this.client.rayTraceBukkit(-1);
        if (r == null) {
            this.stopDestroying("block null");
            return;
        }
        if (this.client.isUsingItem()) {
            this.stopDestroying("doing something else");
            return;
        }
        if (r.getHitBlock() == null) {
            this.stopDestroying("block null");
            return;
        }
        this.destroyPos = ((CraftBlock) r.getHitBlock()).getPosition();
        if (!this.client.level.getWorldBorder().isWithinBounds(this.destroyPos)) {
            this.stopDestroying("outside of worldBorder");
            return;
        }
        this.isDestroying = true;
    }

    public void continueDestroying() {
        if (!this.isDestroying) {
            return;
        }
        if (this.client.isDeadOrDying()) {
            this.stopDestroying("dead");
            return;
        }
        if (!this.client.input.LEFT_CLICK) {
            this.stopDestroying("not holding leftClick");
            return;
        }
        if (this.client.level.getBlockState(this.destroyPos).getMaterial().isLiquid() || this.client.level.getBlockState(this.destroyPos).isAir()) {
            this.stopDestroying("air / liquid");
            return;
        }
        RayTraceResult r = this.client.rayTraceBukkit(-1);
        if (r == null || r.getHitBlock() == null) {
            this.stopDestroying("block null");
            return;
        }
        if (!this.sameBlock(((CraftBlock) r.getHitBlock()).getPosition())) {
            this.stopDestroying("different block");
            this.startDestroying();
            return;
        }
        this.client.swing(InteractionHand.MAIN_HAND);
        float damage = this.client.level.getBlockState(((CraftBlock) r.getHitBlock()).getPosition()).getDestroyProgress(client, this.client.level, this.destroyPos);
        double ticksTotal = (Math.ceil(1 / damage));
        this.client.logger().info(
                "destroying " + this.client.level.getBlockState(this.destroyPos).getBlock().getName().getString()
                + " at " + this.destroyPos.getX() + " " + this.destroyPos.getY() + " " + this.destroyPos.getZ()
                + " (" + (this.digTicks > ticksTotal ? 0.0 : (ticksTotal - this.digTicks) / 20) + ")"
        );
        this.destroyProgress += damage;
        SoundType soundType = this.client.level.getBlockState(this.destroyPos).getSoundType();
        if (this.digTicks % 4.0F == 0.0F && soundType != null) {
            this.client.level.playSound(null, this.destroyPos.getX(), this.destroyPos.getY(), this.destroyPos.getZ(), this.client.level.getBlockState(this.destroyPos).getSoundType().getBreakSound(), SoundSource.BLOCKS, soundType.getVolume() / 4.0F, soundType.getPitch());
        }
        this.digTicks++;
        if ((this.client.gameMode.isCreative() && !this.client.level.getWorldBorder().isWithinBounds(this.destroyPos)) || this.destroyProgress >= 1.0F) {
            this.client.gameMode.destroyBlock(this.destroyPos);
            this.stopDestroying("finished destroying");
        }
        this.send(new ClientboundBlockDestructionPacket(this.client.getId(), this.destroyPos, (int) (this.destroyProgress * 10.0F) - 1));
    }

    public void stopDestroying(String s) {
        this.client.logger().info("stopped destroying (" + s + ")");
        this.send(new ClientboundBlockDestructionPacket(this.client.getId(), this.destroyPos, -1));
        this.client.swing(InteractionHand.MAIN_HAND);
        this.digTicks = 0.0F;
        this.destroyProgress = 0.0F;
        this.destroyPos = BlockPos.ZERO;
        this.isDestroying = false;
    }

    public void stopDestroyingNoMessage() {
        this.send(new ClientboundBlockDestructionPacket(this.client.getId(), this.destroyPos, -1));
        this.digTicks = 0.0F;
        this.destroyProgress = 0.0F;
        this.destroyPos = BlockPos.ZERO;
        this.isDestroying = false;
    }

    private void send(Packet<?> packet) {
        for (ServerPlayer player : ((CraftServer) Bukkit.getServer()).getHandle().players) {
            if (player.hasLineOfSight(this.client)) {
                player.connection.send(packet);
            }
        }
    }

    private boolean sameBlock(BlockPos position) {
        return this.destroyPos.equals(position);
    }

    public void startUsingItem() {
        if (this.client.handsOccupied) {
            this.client.logger().info("can't right click (hands occupied)");
        }
        else if (this.isDestroying()) {
            this.client.logger().info("can't right click (destroying a block)");
        }
        else {
            for (InteractionHand hand : InteractionHand.values()) {
                BlockHitResult b = this.client.rayTrace(-1);
                ItemStack itemStack = this.client.getItemInHand(hand);
                if (itemStack != null) {
                    RayTraceResult r = this.client.rayTraceBukkit(-1);
                    if (r != null && r.getHitEntity() != null) {
                        Entity entity = ((CraftEntity) r.getHitEntity()).getHandle();
                        InteractionResult result = null;
                        if (entity instanceof LivingEntity) {
                            result = itemStack.interactLivingEntity(this.client, (LivingEntity) entity, hand);
                        }
                        if (result != null && !result.consumesAction()) {
                            result = entity.interact(this.client, hand);
                        }
                        if (result != null && result.consumesAction()) {
                            if (result.shouldSwing()) {
                                this.client.swing(hand);
                            }
                            return;
                        }
                    }
                    else if (b != null && !this.client.level.getBlockState(b.getBlockPos()).isAir() && !itemStack.isEmpty()) {
                        InteractionResult result = itemStack.useOn(new UseOnContext(this.client, hand, b), hand);
                        if (result.shouldSwing()) {
                            this.client.swing(hand);
                            return;
                        }
                        if (result == InteractionResult.FAIL) {
                            return;
                        }
                    }
                    InteractionResultHolder<ItemStack> holder = itemStack.use(this.client.level, this.client, hand);
                    final ItemStack itemStack1 = holder.getObject();
                    if (itemStack1 != itemStack) {
                        this.client.setItemInHand(hand, itemStack1);
                    }
                }
            }
        }
    }
}
