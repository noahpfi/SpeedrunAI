package com.swirb.statues.client;

import com.swirb.statues.Statues;
import com.swirb.statues.client.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.RayTraceResult;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class Client extends ServerPlayer {

    private final Logger LOGGER;
    private final BukkitScheduler scheduler;
    public final String name;
    public final InventoryUtils inventoryUtils;
    public final int taskID;

    private boolean attackBlocked = false;

    public Client(Level level, String name, String[] skin) {
        super(level.getCraftServer().getServer(), level.getWorld().getHandle(), new ClientProfile(ClientProfile.validateName(name), skin));
        this.name = this.getGameProfile().getName();
        this.LOGGER = new Logger(this.name);
        this.scheduler = Bukkit.getScheduler();
        this.inventoryUtils = new InventoryUtils(this);
        this.clientViewDistance = Bukkit.getViewDistance();
        this.maxUpStep = 0.7F;
        this.taskID = this.scheduler.scheduleSyncRepeatingTask(Statues.getInstance(), this::doTick, 0, 0);
        this.placeClient();
    }

    private void placeClient() {
        Statues.getInstance().getLogger().info(ChatUtils.DASH);
        Statues.getInstance().getLogger().info("Connecting " + this.name + " to the server...");
        Connection connection = new ClientConnection(PacketFlow.SERVERBOUND, this);
        Statues.getInstance().getLogger().info("Done! successfully connected " + this.name + " to the server");
        Statues.getInstance().getLogger().info("Spawning " + this.name + "...");
        this.server.getPlayerList().placeNewPlayer(connection, this);
        Statues.getClientHandler().add(this);
        Statues.getInstance().getLogger().info("Done! Spawned " + this.name + " at " + Math.floor(this.getX()) + ", " + Math.floor(this.getY()) + ", " + Math.floor(this.getZ()));
        Statues.getInstance().getLogger().info(ChatUtils.DASH);
    }

    public void tick() {
        super.tick();
    }

    public void remove(RemovalReason removalReason) {
        super.remove(removalReason);
        this.LOGGER.info("removed due to " + removalReason.toString());
        if (removalReason == RemovalReason.KILLED) {
            this.scheduler.runTaskLater(Statues.getInstance(), () -> Statues.getClientHandler().respawn(this), 20);
        }
    }

    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.LOGGER.info("died due to " + damageSource.msgId);
    }

    public boolean isLocalPlayer() {
        return true;
    }

    public Entity changeDimension(ServerLevel serverLevel, PlayerTeleportEvent.TeleportCause cause) {
        boolean flag = this.level.getTypeKey() == LevelStem.END && serverLevel != null && serverLevel.getTypeKey() == LevelStem.OVERWORLD;
        Entity entity = super.changeDimension(serverLevel, cause);
        this.isChangingDimension = false;
        if (flag) {
            Objects.requireNonNull(this.level.getServer()).getPlayerList().respawn(this, true);
        }
        return entity;
    }

    public void knockback(double d0, double d1, double d2) {
        if (!this.attackBlocked) {
            super.knockback(d0, d1, d2);
        }
        this.attackBlocked = false;
    }

    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isDamageSourceBlocked(damageSource)) {
            this.attackBlocked = true;
            this.playSound(SoundEvents.SHIELD_BLOCK, 0.8F, 0.8F + this.level.random.nextFloat() * 0.4F);
        }
        boolean damaged = super.hurt(damageSource, f);
        if (damaged) {
            if (this.hurtMarked) {
                this.hurtMarked = false;
                this.scheduler.runTask(Statues.getInstance(), () -> Client.this.hurtMarked = true);
            }
        }
        return damaged;
    }

    public void checkFallDamage(double d0, boolean flag, BlockState blockState, BlockPos blockPos) {
        if (!this.isInWater()) {
            if (this.getVehicle() instanceof Boat) {
                this.wasTouchingWater = false;
            }
            else if (this.updateFluidHeightAndDoFluidPushing(FluidTags.WATER, 0.014D)) {
                if (!this.wasTouchingWater && !this.firstTick) {
                    this.doWaterSplashEffect();
                }
                this.resetFallDistance();
                this.wasTouchingWater = true;
                this.clearFire();
            } else {
                this.wasTouchingWater = false;
            }
        }
        if (!this.level.isClientSide && flag && this.fallDistance > 0.0F) {
            this.removeSoulSpeed();
            this.tryAddSoulSpeed();
        }
        if (!this.level.isClientSide && this.fallDistance > 3.0F && flag) {
            float f = (float) Mth.ceil(this.fallDistance - 3.0F);
            if (!blockState.isAir()) {
                double d1 = Math.min((0.2F + f / 15.0F), 2.5D);
                int i = (int) (150.0D * d1);
                ((ServerLevel) this.level).sendParticles(this, new BlockParticleOption(ParticleTypes.BLOCK, blockState), this.getX(), this.getY(), this.getZ(), i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, false);
            }
        }
        if (flag) {
            if (this.fallDistance > 0.0F) {
                blockState.getBlock().fallOn(this.level, blockState, blockPos, this, this.fallDistance);
                if (!blockState.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS)) {
                    this.gameEvent(GameEvent.HIT_GROUND);
                }
            }
            this.resetFallDistance();
        } else if (d0 < 0.0D) {
            this.fallDistance = (float)((double)this.fallDistance - d0);
        }
    }

    public void swapItemInHands() {
        if (!this.isSpectator()) {
            ItemStack itemstack = this.getItemInHand(InteractionHand.OFF_HAND);
            this.setItemInHand(InteractionHand.OFF_HAND, this.getItemInHand(InteractionHand.MAIN_HAND));
            this.setItemInHand(InteractionHand.MAIN_HAND, itemstack);
            this.stopUsingItem();
        }
    }

    public void equipArmor() {
        ItemStack itemStackFeet = this.inventoryUtils.get(InventoryUtils.Section.ARMOR, 0);
        ItemStack itemStackLegs = this.inventoryUtils.get(InventoryUtils.Section.ARMOR, 1);
        ItemStack itemStackChest = this.inventoryUtils.get(InventoryUtils.Section.ARMOR, 2);
        ItemStack itemStackHead = this.inventoryUtils.get(InventoryUtils.Section.ARMOR, 3);
        for (ItemStack itemStackArmor : this.inventoryUtils.getAll(this.getInventory().items, item -> item instanceof ArmorItem)) {
            ArmorItem armor = ((ArmorItem) itemStackArmor.getItem());
            if (armor.getSlot() == EquipmentSlot.FEET && ((itemStackFeet.getItem() instanceof ArmorItem && armor.getMaterial().getDefenseForSlot(EquipmentSlot.FEET) > ((ArmorItem) itemStackFeet.getItem()).getMaterial().getDefenseForSlot(EquipmentSlot.FEET)) || itemStackFeet.isEmpty())) {
                this.inventoryUtils.swap(itemStackArmor, InventoryUtils.Section.ARMOR, 0);
            }
            else if (armor.getSlot() == EquipmentSlot.LEGS && ((itemStackLegs.getItem() instanceof ArmorItem && armor.getMaterial().getDefenseForSlot(EquipmentSlot.LEGS) > ((ArmorItem) itemStackLegs.getItem()).getMaterial().getDefenseForSlot(EquipmentSlot.LEGS)) || itemStackLegs.isEmpty())) {
                this.inventoryUtils.swap(itemStackArmor, InventoryUtils.Section.ARMOR, 1);
            }
            else if (armor.getSlot() == EquipmentSlot.CHEST && ((itemStackChest.getItem() instanceof ArmorItem && armor.getMaterial().getDefenseForSlot(EquipmentSlot.CHEST) > ((ArmorItem) itemStackChest.getItem()).getMaterial().getDefenseForSlot(EquipmentSlot.CHEST)) || itemStackChest.isEmpty())) {
                this.inventoryUtils.swap(itemStackArmor, InventoryUtils.Section.ARMOR, 2);
            }
            else if (armor.getSlot() == EquipmentSlot.HEAD && ((itemStackHead.getItem() instanceof ArmorItem && armor.getMaterial().getDefenseForSlot(EquipmentSlot.HEAD) > ((ArmorItem) itemStackHead.getItem()).getMaterial().getDefenseForSlot(EquipmentSlot.HEAD)) || itemStackHead.isEmpty())) {
                this.inventoryUtils.swap(itemStackArmor, InventoryUtils.Section.ARMOR, 3);
            }
        }
    }

    public void shutDown() {
        this.scheduler.cancelTask(this.taskID);
    }

    public java.util.logging.Logger logger() {
        return this.LOGGER;
    }
}
