package com.swirb.pvpbots.client;

import com.swirb.pvpbots.client.utils.InventoryUtils;
import com.swirb.pvpbots.client.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

// this is actually called Controller

public class PvP {

    private final Client client;
    private final SimplePathing pathing;
    private boolean active;

    private List<ItemStack> armorLastTick;
    private float keepCharging;
    private boolean needAir;
    private boolean mlgd;

    public PvP(Client client) {
        this.client = client;
        this.pathing = new SimplePathing(this.client);
        this.active = false;
        this.armorLastTick = new ArrayList<>();
        this.keepCharging = 0.0F;
        this.needAir = false;
        this.mlgd = false;
    }

    public void tick() {
        if (!this.active || this.client.getHealth() <= 0.0F) {
            return;
        }
        this.pvp();
        this.keepCharging--;
    }

    public void pvp() {
        this.releaseAll();
        if (!this.armorLastTick.equals(this.client.inventoryUtils.getAll(item -> item instanceof ArmorItem))) {
            ItemStack itemStackFeet = this.client.inventoryUtils.get(InventoryUtils.Section.ARMOR, 0);
            ItemStack itemStackLegs = this.client.inventoryUtils.get(InventoryUtils.Section.ARMOR, 1);
            ItemStack itemStackChest = this.client.inventoryUtils.get(InventoryUtils.Section.ARMOR, 2);
            ItemStack itemStackHead = this.client.inventoryUtils.get(InventoryUtils.Section.ARMOR, 3);
            for (ItemStack itemStackArmor : this.client.inventoryUtils.getAll(this.client.getInventory().items, item -> item instanceof ArmorItem)) {
                ArmorItem armor = ((ArmorItem) itemStackArmor.getItem());
                if (armor.getSlot() == EquipmentSlot.FEET && ((itemStackFeet.getItem() instanceof ArmorItem && armor.getMaterial().getDefenseForSlot(EquipmentSlot.FEET) > ((ArmorItem) itemStackFeet.getItem()).getMaterial().getDefenseForSlot(EquipmentSlot.FEET)) || itemStackFeet.isEmpty())) {
                    this.client.inventoryUtils.swap(itemStackArmor, InventoryUtils.Section.ARMOR, 0);
                }
                else if (armor.getSlot() == EquipmentSlot.LEGS && ((itemStackLegs.getItem() instanceof ArmorItem && armor.getMaterial().getDefenseForSlot(EquipmentSlot.LEGS) > ((ArmorItem) itemStackLegs.getItem()).getMaterial().getDefenseForSlot(EquipmentSlot.LEGS)) || itemStackLegs.isEmpty())) {
                    this.client.inventoryUtils.swap(itemStackArmor, InventoryUtils.Section.ARMOR, 1);
                }
                else if (armor.getSlot() == EquipmentSlot.CHEST && ((itemStackChest.getItem() instanceof ArmorItem && armor.getMaterial().getDefenseForSlot(EquipmentSlot.CHEST) > ((ArmorItem) itemStackChest.getItem()).getMaterial().getDefenseForSlot(EquipmentSlot.CHEST)) || itemStackChest.isEmpty())) {
                    this.client.inventoryUtils.swap(itemStackArmor, InventoryUtils.Section.ARMOR, 2);
                }
                else if (armor.getSlot() == EquipmentSlot.HEAD && ((itemStackHead.getItem() instanceof ArmorItem && armor.getMaterial().getDefenseForSlot(EquipmentSlot.HEAD) > ((ArmorItem) itemStackHead.getItem()).getMaterial().getDefenseForSlot(EquipmentSlot.HEAD)) || itemStackHead.isEmpty())) {
                    this.client.inventoryUtils.swap(itemStackArmor, InventoryUtils.Section.ARMOR, 3);
                }
            }
        }
        this.armorLastTick = this.client.inventoryUtils.getAll(item -> item instanceof ArmorItem);

        // death prev

        if (this.needAir && this.client.isInWater() && !this.client.level.getBlockState(this.client.eyeBlockPosition()).getMaterial().isLiquid()) {
            this.client.logger().info("breathing");
            this.client.input.SPACE = true;
            if (this.client.getAirSupply() >= 250) {
                this.needAir = false;
            }
            return;
        }

        if (this.client.isInWater() && this.client.getAirSupply() <= 40 && this.client.level.getBlockState(this.client.eyeBlockPosition()).getMaterial().isLiquid()) {
            this.client.logger().info("need air");
            this.client.input.SPACE = true;
            this.client.input.SPRINT = true;
            this.client.input.W = true;
            this.client.look(this.client.getYRot(), -80.0F);
            this.needAir = true;
            return;
        }

        if (this.client.isInWall()) {
            this.client.logger().info("claustrophobia moment");
            this.client.inventoryUtils.swap(this.client.inventoryUtils.bestToolFor(this.client.level.getBlockState(this.client.eyeBlockPosition())), InventoryUtils.Section.ITEMS, 0);
            this.client.input.SPACE = true;
            this.client.input.SPRINT = true;
            this.client.input.W = true;
            this.client.input.LEFT_CLICK = true;
            for (BlockPos blockPos : this.client.around(true)) {
                if (!this.client.level.getBlockState(blockPos).getMaterial().isSolid()) {
                    this.client.lookAt(blockPos);
                    return;
                }
            }
            return;
        }

        if (this.client.fallDistance >= 3.0F && this.client.inventoryUtils.has(Items.WATER_BUCKET) && !this.above(Material.WATER)) {
            this.client.logger().info("mlg");
            this.client.inventoryUtils.swap(this.client.inventoryUtils.get(Items.WATER_BUCKET), InventoryUtils.Section.ITEMS, 0);
            this.center();
            this.client.input.RIGHT_CLICK = true;
            this.client.look(this.client.getYRot(), 90);
            this.mlgd = true;
            return;
        }

        if (this.client.isOnFire() && (!this.client.isInLava() || (this.client.isInLava() && !this.isPassable(this.client.blockPosition().below()))) && this.client.inventoryUtils.get(Items.WATER_BUCKET) != null) {
            this.client.logger().info("chilling");
            this.client.inventoryUtils.swap(this.client.inventoryUtils.get(Items.WATER_BUCKET), InventoryUtils.Section.ITEMS, 0);
            this.client.look(this.client.getYRot(), 90);
            this.clickRight();
            this.mlgd = true;
            return;
        }

        if (this.mlgd && this.client.getHealth() > 0.0F) {
            this.client.logger().info("picking up water");
            if (this.client.fallDistance != 0) {
                return;
            }
            this.client.inventoryUtils.swap(this.client.inventoryUtils.get(Items.BUCKET), InventoryUtils.Section.ITEMS, 0);
            for (BlockPos blockPos : MathUtils.sphere(this.client.blockPosition(), 5)) {
                if (this.client.canSee(blockPos) && this.client.level.getBlockState(blockPos).getMaterial() == Material.WATER && this.client.level.getBlockState(blockPos).getFluidState().isSource()) {
                    this.client.lookAt(blockPos);
                    this.clickRight();
                    this.mlgd = false;
                    return;
                }
            }
            this.mlgd = false;
            return;
        }

        // pvp

        Entity entity = this.client.nearest(100, 100, 100, e -> e instanceof ServerPlayer);
        if (entity == null) {
            return;
        }
        this.pathing.tick(entity.blockPosition(), true);
    }

    private void shortRangeFight(Entity entity) {
        ItemStack weapon = this.client.inventoryUtils.weapon();
        if (entity instanceof LivingEntity && ((LivingEntity) entity).isBlocking() && this.client.inventoryUtils.get(item -> item instanceof AxeItem) != null) {
            weapon = this.client.inventoryUtils.get(item -> item instanceof AxeItem);
        }
        this.client.inventoryUtils.swap(weapon, InventoryUtils.Section.ITEMS, 0);
        if (this.client.isInWater() && !this.client.isSwimming() && this.client.level.getBlockState(new BlockPos(this.client.position()).below(2)).getMaterial() == Material.WATER) {
            this.client.input.SHIFT = true;
            this.client.input.SPACE = false;
        }
        else this.client.input.SHIFT = false;
        if (this.client.inReach(entity) && !this.client.input.LEFT_CLICK) {
            this.client.lookAt(entity);
            if (this.client.waitForRecharge <= 0) {
                this.clickLeft();
            }
        }
    }

    private void shoot(Entity entity) {
        if (!this.client.inventoryUtils.has(Items.BOW) && !this.client.inventoryUtils.has(Items.CROSSBOW)) {
            return;
        }
        this.client.logger().info("shooting " + entity.getName().getString());
        ItemStack shootingWeapon = this.client.inventoryUtils.shootingWeapon();
        if ((shootingWeapon.getItem() instanceof BowItem && this.client.inventoryUtils.get(item -> item instanceof ArrowItem) != null) || (shootingWeapon.getItem() instanceof CrossbowItem && this.client.inventoryUtils.get(item -> item instanceof ArrowItem || item instanceof FireworkRocketItem) != null) || shootingWeapon.getItem() instanceof TridentItem) {
            this.client.inventoryUtils.swap(shootingWeapon, InventoryUtils.Section.ITEMS, 0);
            ItemStack shootable = this.client.inventoryUtils.get(item -> item instanceof TippedArrowItem || item instanceof FireworkRocketItem);
            if (shootingWeapon.getItem() instanceof CrossbowItem && shootable != null) {
                this.client.inventoryUtils.swap(shootable, InventoryUtils.Section.OFFHAND, 0);
                if (CrossbowItem.isCharged(shootingWeapon)) {
                    this.client.input.RIGHT_CLICK = true;
                    return;
                }
            }
            Vec3 vec3 = MathUtils.calculateArrowVelocity(this.client);
            double[] angles = MathUtils.shootingAngle(this.client.position(), this.client.getDeltaMovement(), entity.position(), entity.getDeltaMovement(), vec3, false);
            float angle = (float) angles[1];
            float angle1 = (float) angles[0];
            if (shootingWeapon.getItem() instanceof BowItem || shootingWeapon.getItem() instanceof TridentItem) {
                this.client.look(angle1, angle - 5.5F);
            }
            else {
                this.client.lookAt(entity);
                this.client.look(angle1, this.client.getXRot());
            }
            this.client.input.RIGHT_CLICK = true;
            if (this.keepCharging <= 0) {
                this.keepCharging = (shootingWeapon.getItem() instanceof BowItem || shootingWeapon.getItem() instanceof TridentItem) ? 22 : shootingWeapon.getItem().getUseDuration(shootingWeapon);
                if (shootingWeapon.getItem() instanceof CrossbowItem && this.client.inventoryUtils.get(item -> item instanceof FireworkRocketItem) != null) {
                    this.client.input.RIGHT_CLICK = false;
                    if (!this.isPassable(this.client.front())) {
                        this.client.input.SPACE = true;
                    }
                    return;
                }
                if (!Float.isNaN(angle) && !Float.isNaN(angle1)) {
                    this.client.look(angle1, angle - 5.5F);
                    this.client.input.RIGHT_CLICK = false;
                    if (!this.isPassable(this.client.front())) {
                        this.client.input.SPACE = true;
                    }
                }
            }
        }
    }

    public void center() {
        double x0 = this.client.getX();
        double z0 = this.client.getZ();
        double x1 = Mth.floor(x0) + 0.5D;
        double z1 = Mth.floor(z0) + 0.5D;
        Direction direction = this.client.getDirection();
        double difX = x1 - x0;
        double difZ = z1 - z0;
        if (direction == Direction.NORTH) {
            if (difX > 0) this.client.input.D = true;
            else if (difX < 0) this.client.input.A = true;
            if (difZ > 0) this.client.input.S = true;
            else if (difZ < 0) this.client.input.W = true;
            return;
        }
        if (direction == Direction.SOUTH) {
            if (difX > 0) this.client.input.A = true;
            else if (difX < 0) this.client.input.D = true;
            if (difZ > 0) this.client.input.W = true;
            else if (difZ < 0) this.client.input.S = true;
            return;
        }
        if (direction == Direction.EAST) {
            if (difX > 0) this.client.input.W = true;
            else if (difX < 0) this.client.input.S = true;
            if (difZ > 0) this.client.input.D = true;
            else if (difZ < 0) this.client.input.A = true;
            return;
        }
        if (direction == Direction.WEST) {
            if (difX > 0) this.client.input.S = true;
            else if (difX < 0) this.client.input.W = true;
            if (difZ > 0) this.client.input.A = true;
            else if (difZ < 0) this.client.input.D = true;
        }
    }

    private void clickRight() {
        this.client.input.RIGHT_CLICK = true;
        this.client.updateInputs();
        this.client.input.RIGHT_CLICK = false;
    }

    private void clickLeft() {
        this.client.input.LEFT_CLICK = true;
        this.client.updateInputs();
        this.client.input.LEFT_CLICK = false;
    }

    private void releaseAll() {
        this.client.input.W = false;
        this.client.input.S = false;
        this.client.input.A = false;
        this.client.input.D = false;
        this.client.input.SPRINT = false;
        this.client.input.SHIFT = false;
        this.client.input.SPACE = false;
        this.client.input.LEFT_CLICK = false;
        this.client.input.RIGHT_CLICK = false;
    }

    public boolean isPassable(BlockPos position) {
        return this.client.level.getBlockState(position).getCollisionShape(this.client.level, position).isEmpty();
    }

    public boolean above(Material material) {
        for (int i = this.client.blockPosition().getY(); i > -64; i--) {
            if (!this.client.level.getBlockState(new BlockPos(this.client.getX(), i, this.client.getZ())).isAir()) {
                return this.client.level.getBlockState(new BlockPos(this.client.getX(), i, this.client.getZ())).getMaterial() == material;
            }
        }
        return false;
    }

    public void startup() {
        this.active = true;
    }

    public void shutDown() {
        this.releaseAll();
        this.active = false;
    }
}
