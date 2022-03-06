package com.swirb.speedrunai.client;

import com.swirb.speedrunai.main.SpeedrunAI;
import com.swirb.speedrunai.utils.*;
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
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.RayTraceResult;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class Client extends ServerPlayer {

    private final BukkitScheduler scheduler;
    private final SmrtThetaController controller;
    public final String name;
    public final MouseUtils mouseUtils;
    public final InventoryUtils inventoryUtils;
    public final Input input;
    public final int taskID;

    private boolean attackBlocked = false;
    public boolean handsOccupied = false;
    private int jumpRidingTicks = 0;
    private float jumpRidingScale = 0.0F;
    public float waitForRecharge = 0;

    public Client(Level level, String name, String[] skin) {
        super(level.getCraftServer().getServer(), level.getWorld().getHandle(), new ClientProfile(ClientProfile.validateName(name), skin));
        this.name = name;
        this.scheduler = Bukkit.getScheduler();
        this.controller = new SmrtThetaController(this);
        this.mouseUtils = new MouseUtils(this);
        this.inventoryUtils = new InventoryUtils(this);
        this.input = new Input();
        this.clientViewDistance = Bukkit.getViewDistance();
        this.maxUpStep = 0.7F;
        this.taskID = this.scheduler.scheduleSyncRepeatingTask(SpeedrunAI.getInstance(), this::doTick, 0, 0);
        this.placeClient();
    }

    private void placeClient() {
        SpeedrunAI.getInstance().getLogger().info(ChatUtils.DASH);
        SpeedrunAI.getInstance().getLogger().info("Connecting Bot " + name + "...");
        Connection connection = new ClientConnection(PacketFlow.SERVERBOUND, this);
        SpeedrunAI.getInstance().getLogger().info("Successfully Connected Bot " + name + " to the server!");
        SpeedrunAI.getInstance().getLogger().info("Spawning bot " + name + "...");
        this.server.getPlayerList().placeNewPlayer(connection, this);
        SpeedrunAI.getInstance().getClientHandler().add(this);
        SpeedrunAI.getInstance().getLogger().info("Spawned bot " + name + " at "
                + Math.floor(getX())
                + ", " + Math.floor(getY())
                + ", " + Math.floor(getZ())
                + "!");
        SpeedrunAI.getInstance().getLogger().info(ChatUtils.DASH);
    }

    public void tick() {
        super.tick();
        this.controller.tick();
        this.updateInputs();
    }

    public void remove(RemovalReason removalReason) {
        super.remove(removalReason);
        SpeedrunAI.getInstance().getLogger().info(name + "was removed due to " + removalReason.toString());
        if (removalReason == RemovalReason.KILLED)
            this.scheduler.runTaskLater(SpeedrunAI.getInstance(), () -> SpeedrunAI.getInstance().getClientHandler().respawn(this), 20);
    }

    public void die(DamageSource damageSource) {
        super.die(damageSource);
        SpeedrunAI.getInstance().getLogger().info(name + " died due to " + damageSource.msgId);
    }

    public boolean isLocalPlayer() {
        return true;
    }

    // need that because setRot is protected
    public void setRotation(float yaw, float pitch) {
        this.setRot(yaw, pitch);
    }

    public Entity changeDimension(ServerLevel serverLevel, PlayerTeleportEvent.TeleportCause cause) {
        Entity entity = super.changeDimension(serverLevel, cause);
        this.isChangingDimension = false;
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
                this.scheduler.runTask(SpeedrunAI.getInstance(), () -> Client.this.hurtMarked = true);
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

    public void shutDown() {
        this.scheduler.cancelTask(this.taskID);
    }

    public void lookAt(double x, double y, double z) {
        this.look(new Vec3(x - this.getX(), y - this.getEyeY(), z - this.getZ()));
    }

    public void lookAt(BlockPos blockPos) {
        this.lookAt(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
    }

    public void lookAt(Entity entity) {
        this.lookAt(entity.getX(), entity.getEyeY(), entity.getZ());
    }

    public void lookAway(Entity entity) {
        this.look(new Vec3(this.getX() - entity.getX(), this.getEyeY() - entity.getEyeY(), this.getZ() - entity.getZ()));
    }

    public void look(float yaw, float pitch) {
        this.setRot(yaw, pitch);
    }

    public void look(Vec3 vec3) {
        float[] vals = MathUtils.yawPitch(vec3);
        this.setRot(vals[0], vals[1]);
    }

    public void attack(Entity entity) {
        if (this.isAlive()) {
            if (this.inReach(entity) && this.canSee(entity) && !(entity == this.getVehicle())) {
                super.attack(entity);
                this.swing(InteractionHand.MAIN_HAND);
                this.waitForRecharge = this.getCurrentItemAttackStrengthDelay();
                SpeedrunAI.getInstance().getLogger().info(name + " attacking "
                        + entity.getName().getString()
                        + " with " + this.getItemInHand(InteractionHand.MAIN_HAND).getDisplayName().getString());
            }
            else SpeedrunAI.getInstance().getLogger().info(name + " couldn't attack as it cannot reach / see "
                    + entity.getName().getString());
        }
        else SpeedrunAI.getInstance().getLogger().info(name  + " couldn't attack as "
                + entity.getName().getString() + " is dead");
    }

    public boolean canSee(Entity entity) {
        return this.hasLineOfSight(entity);
    }

    public boolean canSee(BlockPos blockPos) {
        if (blockPos == null) {
            return false;
        }
        Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 vec31 = new Vec3(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
        return this.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(blockPos);
    }

    public boolean canSee(double x, double y, double z) {
        Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 vec31 = new Vec3(x, y, z);
        return this.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(new BlockPos(x, y, z));
    }

    public boolean inReach(Entity entity) {
        return this.inReach(entity.getX(), entity.getEyeY(), entity.getZ()  );
    }

    public boolean inReach(BlockPos blockPos) {
        return this.distanceTo(blockPos, false, false) <= (this.gameMode.isCreative() ? 49 : 36);
    }

    public boolean inReach(double x, double y, double z) {
        return this.distanceTo(x, y, z, false, false) <= (this.gameMode.isCreative() ? 49 : 36);
    }

    public void updateInputs() {
        // keyboard
        this.zza = 0;
        this.yya = 0;
        this.xxa = 0;
        if (this.input.W) this.zza++;
        if (this.input.S) this.zza = (this.zza - 1) * 0.9F;
        if (this.input.A) this.xxa++;
        if (this.input.D) this.xxa--;
        if (this.isUsingItem() && !this.isPassenger()) {
            this.xxa *= 0.2F;
            this.zza *= 0.2F;
        }
        if (this.input.SPACE) {
            if (this.isInWater() || this.isInLava()) {
                if (this.isInLava()) this.setDeltaMovement(this.getDeltaMovement().add(0, 0.04, 0));
                else this.yya++;
            }
            else if (this.isOnGround()) {
                this.jumpFromGround();
            }
            else if (!this.isOnGround() && this.isFallFlying()) {
                this.stopFallFlying();
            }
            this.tryToStartFallFlying();
            if (this.getVehicle() instanceof PlayerRideableJumping) {
                if (this.jumpRidingTicks < 0) {
                    this.jumpRidingTicks++;
                    if (this.jumpRidingTicks == 0) {
                        this.jumpRidingScale = 0.0F;
                    }
                }
                if (this.input.SPACE_LAST && !this.input.SPACE) {
                    this.jumpRidingTicks = -10;
                    PlayerRideableJumping jumping = (PlayerRideableJumping) this.getVehicle();
                    if (jumping.canJump() && (this.jumpRidingScale * 100) > 0) {
                        jumping.onPlayerJump((int) (this.jumpRidingScale * 100));
                    }
                }
                else if (!this.input.SPACE_LAST && this.input.SPACE) {
                    this.jumpRidingTicks = 0;
                    this.jumpRidingScale = 0.0F;
                }
                else if (this.input.SPACE_LAST) {
                    this.jumpRidingTicks++;
                    if (this.jumpRidingTicks < 10) {
                        this.jumpRidingScale = this.jumpRidingTicks * 0.1F;
                    }
                    else {
                        this.jumpRidingScale = 0.8F + 2.0F / (this.jumpRidingTicks - 9) * 0.1F;
                    }
                }
            }
            else {
                this.jumpRidingScale = 0.0F;
            }
        }
        if (this.input.SHIFT) {
            if (this.isInWater()) {
                this.yya--;
            }
            else if (!this.isSwimming() && !this.isInLava()) {
                this.xxa = this.xxa * 0.3F;
                this.zza = this.zza * 0.3F;
            }
        }
        if (!(this.foodData.foodLevel <= 6) && !this.hasEffect(MobEffects.BLINDNESS) && !this.isUsingItem() && !this.input.S) {
            this.setSprinting(this.input.SPRINT);
        }
        else this.setSprinting(false);
        this.setShiftKeyDown(this.input.SHIFT);

        // steers boat
        if (this.isPassenger() && this.getVehicle() instanceof Boat boat) {
            try {
                Field fieldW = boat.getClass().getDeclaredField("inputUp");
                Field fieldA = boat.getClass().getDeclaredField("inputLeft");
                Field fieldS = boat.getClass().getDeclaredField("inputDown");
                Field fieldD = boat.getClass().getDeclaredField("inputRight");
                fieldW.setAccessible(true);
                fieldA.setAccessible(true);
                fieldS.setAccessible(true);
                fieldD.setAccessible(true);
                fieldW.set(boat, this.input.W);
                fieldA.set(boat, this.input.A);
                fieldS.set(boat, this.input.S);
                fieldD.set(boat, this.input.D);
                fieldW.setAccessible(false);
                fieldA.setAccessible(false);
                fieldS.setAccessible(false);
                fieldD.setAccessible(false);
                Method method = boat.getClass().getDeclaredMethod("controlBoat");
                method.setAccessible(true);
                method.invoke(boat);
                method.setAccessible(false);
            } catch (Exception ex) {
                SpeedrunAI.getInstance().getLogger().warning(name + " could not steer boat");
            }
        }

        // hotkeys
        if (this.input.INVENTORY) {
            if (this.containerMenu instanceof InventoryMenu) {
                this.closeContainer();
            }
            else if (this.getVehicle() instanceof AbstractHorse) {
                ((AbstractHorse) this.getVehicle()).openInventory(this);
            }
            else {
                this.getBukkitEntity().openInventory(this.inventoryMenu.getBukkitView());
            }
        }
        if (this.input.SWAP) {
            this.swapItemInHands();
        }
        if (this.input.DROP && !this.isSpectator()) {
            this.drop(this.input.CTRL);
        }
        if (this.input.SLOT0) this.selectOnHotBar(0);
        else if (this.input.SLOT1) this.selectOnHotBar(1);
        else if (this.input.SLOT2) this.selectOnHotBar(2);
        else if (this.input.SLOT3) this.selectOnHotBar(3);
        else if (this.input.SLOT4) this.selectOnHotBar(4);
        else if (this.input.SLOT5) this.selectOnHotBar(5);
        else if (this.input.SLOT6) this.selectOnHotBar(6);
        else if (this.input.SLOT7) this.selectOnHotBar(7);
        else if (this.input.SLOT8) this.selectOnHotBar(8);

        // mouse
        if (this.isUsingItem()) {
            if (!this.input.RIGHT_CLICK) {
                this.releaseUsingItem();
            }
        }
        else {
            if (this.input.LEFT_CLICK) {
                RayTraceResult r = this.rayTrace(-1);
                if (r != null && r.getHitEntity() != null && r.getHitEntity() instanceof LivingEntity && !this.handsOccupied) {
                    this.mouseUtils.stopDestroyingNoMessage();
                    this.attack(((CraftEntity) r.getHitEntity()).getHandle());
                }
                else if (!this.handsOccupied) {
                    this.mouseUtils.startDestroying();
                }
            }
        }
        if (this.input.RIGHT_CLICK && !this.isUsingItem() && !this.handsOccupied) {
            this.mouseUtils.startUsingItem();
        }
        this.mouseUtils.continueDestroying();
        this.handsOccupied = this.isPassenger() && (this.input.W || this.input.S || this.input.A || this.input.D);
        this.waitForRecharge--;
        this.input.sync();
    }

    public void swapItemInHands() {
        if (!this.isSpectator()) {
            ItemStack itemstack = this.getItemInHand(InteractionHand.OFF_HAND);
            this.setItemInHand(InteractionHand.OFF_HAND, this.getItemInHand(InteractionHand.MAIN_HAND));
            this.setItemInHand(InteractionHand.MAIN_HAND, itemstack);
            this.stopUsingItem();
        }
    }

    public void selectOnHotBar(int slot) {
        if (!this.isImmobile()) {
            if (slot >= 0 && slot < Inventory.getSelectionSize()) {
                if (this.getInventory().selected != slot && this.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                    this.stopUsingItem();
                }
                this.getInventory().selected = slot;
                this.resetLastActionTime();
            } else {
                SpeedrunAI.getInstance().getLogger().warning(name + " tried to set an invalid carried item");
            }
        }
    }

    public void chat(String text) {
        this.connection.chat(text, false);
    }

    public void updateDifficulty(int difficulty) {
        this.server.setDifficulty(Difficulty.byId(difficulty), false);
    }

    public void setViewDistance(int viewDistance) {
        this.clientViewDistance = viewDistance < 1 ? 1 : (Math.min(viewDistance, 32));
    }

    public void updateOptions(int viewDistance, int modelCustomisation, int mainHand) {
        this.setViewDistance(viewDistance);
        if (modelCustomisation != -1) this.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) modelCustomisation);
        if (mainHand >= 0 && mainHand <= 1) this.entityData.set(DATA_PLAYER_MAIN_HAND, (byte) mainHand);
    }

    public Entity nearest(double x, double y, double z, Predicate<Entity> predicate) {
        double d = Double.MAX_VALUE;
        Entity e = null;
        List<Entity> entities = this.nearbyEntities(x, y, z, predicate);
        for (Entity entity : entities) {
            double distance = MathUtils.distanceSquared(this.getX(), this.getY(), this.getZ(), entity.getX(), entity.getY(), entity.getZ(), false, false);
            if (distance < d) {
                d = distance;
                e = entity;
            }
        }
        return e;
    }

    public List<Entity> nearbyEntities(double x, double y, double z, Predicate<Entity> predicate) {
        return this.level.getEntities(this, this.getBoundingBox().inflate(x, y, z), predicate.and(EntitySelector.NO_SPECTATORS));
    }

    public BlockPos front() {
        return new BlockPos(this.position()).relative(this.getDirection());
    }

    public Set<BlockPos> around() {
        Set<BlockPos> positions = new HashSet<>();
        for (Direction direction : Direction.values()) {
            positions.add(new BlockPos(this.position()).relative(direction));
            positions.add(new BlockPos(this.position()).above().relative(direction));
        }
        positions.remove(new BlockPos(this.position()));
        positions.remove(new BlockPos(this.position()).above());
        return positions;
    }

    public double distanceTo(BlockPos position, boolean root, boolean ignoreY) {
        return MathUtils.distanceSquared(new BlockPos(this.position()), position, root, ignoreY);
    }

    public double distanceTo(double x, double y, double z, boolean root, boolean ignoreY) {
        return MathUtils.distanceSquared(this.getX(), this.getEyeY(), this.getZ(), x, y, z, root, ignoreY);
    }

    public RayTraceResult rayTrace(double distance) {
        double d0 = this.getX();
        double d1 = this.getEyeY();
        double d2 = this.getZ();
        double d3 = distance == -1 ? (this.gameMode.isCreative() ? 5.0D : 4.5D) : distance;
        FluidCollisionMode fcm = (this.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BucketItem || this.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof BucketItem) ? FluidCollisionMode.SOURCE_ONLY : FluidCollisionMode.NEVER;
        return this.getBukkitEntity().getWorld().rayTrace(new Location(this.level.getWorld(), d0, d1, d2), this.getBukkitEntity().getLocation().getDirection(), d3, fcm, false, 0.1D, entity -> entity.getUniqueId() != this.getBukkitEntity().getUniqueId() && !((CraftEntity) entity).getHandle().isSpectator() && !entity.isDead());
    }

    public BlockHitResult rayTrace2(double distance) {
        float f1 = this.getXRot();
        float f2 = this.getYRot();
        Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        float f3 = Mth.cos(-f2 * 0.017453292F - 3.1415927F);
        float f4 = Mth.sin(-f2 * 0.017453292F - 3.1415927F);
        float f5 = -Mth.cos(-f1 * 0.017453292F);
        float f6 = Mth.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = distance == -1 ? (this.gameMode.isCreative() ? 5.0D : 4.5D) : distance;
        Vec3 vec31 = vec3.add((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
        ClipContext.Fluid f = (this.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BucketItem || this.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof BucketItem) ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE;
        return this.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, f, this));
    }

    public int tickCount() {
        return this.tickCount;
    }

    public SmrtThetaController controller() {
        return this.controller;
    }
}
