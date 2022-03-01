package com.swirb.speedrunai.client;

import com.swirb.speedrunai.main.SpeedrunAI;
import com.swirb.speedrunai.utils.InventoryUtils;
import com.swirb.speedrunai.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Arrays;
import java.util.List;

public class Controller {

    private final Client client;
    private final SpeedrunAI plugin;
    private final BukkitScheduler scheduler;
    private boolean active;
    public boolean ignoreClients;

    private final String[] sentences = {"haha bad", "lamoo", "sryy", "whoops", "ez", "im too gud"};

    public Controller(Client client) {
        this.client = client;
        this.plugin = SpeedrunAI.getInstance();
        this.scheduler = Bukkit.getScheduler();
        this.active = false;
        this.ignoreClients = true;
    }

    public void tick() {
        if (!this.active) {
            return;
        }
        killEverything();
    }

    private void killEverything() {
        this.releaseAll();

        // Death prevention

        if (this.client.breathe && this.client.isInWater() && !this.client.level.getBlockState(new BlockPos(this.client.getX(), this.client.getEyeY(), this.client.getZ())).getMaterial().isLiquid()) {
            //System.out.println("breathing");
            this.client.input.SPACE = true;
            if (this.client.getAirSupply() >= 250) {
                this.client.breathe = false;
            }
            return;
        }
        if (this.client.isInWater() && this.client.getAirSupply() <= 40 && this.client.level.getBlockState(new BlockPos(this.client.getX(), this.client.getEyeY(), this.client.getZ())).getMaterial().isLiquid()) {
            //System.out.println("need air");
            this.runForward(true);
            this.client.look(this.client.getYRot(), -80.0F);
            this.client.breathe = true;
            return;
        }
        if (this.client.isInWall()) {
            //System.out.println("don't wanna suffocate");
            this.client.inventoryUtils.swap(this.client.inventoryUtils.bestToolFor(this.client.level.getBlockState(new BlockPos(this.client.position()).above())), InventoryUtils.Section.ITEMS, 0);
            this.runForward(true);
            this.client.input.LEFT_CLICK = true;
            for (BlockPos pos : this.client.around()) {
                if (!this.client.level.getBlockState(pos).getMaterial().isSolid()) {
                    this.client.lookAt(pos);
                    return;
                }
            }
            return;
        }
        //TODO if no water bucket, find nearest block, aim at side (+0.5D in opposite look direction) [clutch]
        // add other kinds of mlg
        if (this.client.fallDistance > 3.0F && this.client.inventoryUtils.get(item -> item == Items.WATER_BUCKET) != null) {
            //System.out.println("mlg");
            this.client.inventoryUtils.swap(this.client.inventoryUtils.get(item -> item == Items.WATER_BUCKET), InventoryUtils.Section.ITEMS, 0);
            this.center();
            this.client.look(this.client.getYRot(), 90);
            this.client.input.RIGHT_CLICK = true;
            this.client.doneMlg = true;
            return;
        }
        if (this.client.doneMlg) {
            //System.out.println("picking up mlg");
            this.client.inventoryUtils.swap(this.client.inventoryUtils.get(item -> item == Items.BUCKET), InventoryUtils.Section.ITEMS, 0);
            this.center();
            this.client.look(this.client.getYRot(), 90);
            this.clickRight();
            this.client.doneMlg = false;
            return;
        }

        // Goal (kill Players for testing)

        if (!this.client.armorLastTick.equals(this.client.inventoryUtils.getAll(item -> item instanceof ArmorItem))) {
            ItemStack itemStackFeet = this.client.inventoryUtils.get(InventoryUtils.Section.ARMOR, 0);
            ItemStack itemStackLegs = this.client.inventoryUtils.get(InventoryUtils.Section.ARMOR, 1);
            ItemStack itemStackChest = this.client.inventoryUtils.get(InventoryUtils.Section.ARMOR, 2);
            ItemStack itemStackHead = this.client.inventoryUtils.get(InventoryUtils.Section.ARMOR, 3);
            for (ItemStack itemStackArmor : this.client.inventoryUtils.getAll(this.client.getInventory().items, item -> item instanceof ArmorItem)) {
                ArmorItem armor = ((ArmorItem) itemStackArmor.getItem());
                if (armor.getSlot() == EquipmentSlot.FEET && ((itemStackFeet.getItem() instanceof ArmorItem && armor.getMaterial().getDefenseForSlot(EquipmentSlot.FEET) > ((ArmorItem) itemStackFeet.getItem()).getMaterial().getDefenseForSlot(EquipmentSlot.FEET)) || itemStackFeet.isEmpty())) {
                    this.client.inventoryUtils.swap(itemStackArmor, InventoryUtils.Section.ARMOR, 0);
                    return;
                }
                if (armor.getSlot() == EquipmentSlot.LEGS && ((itemStackLegs.getItem() instanceof ArmorItem && armor.getMaterial().getDefenseForSlot(EquipmentSlot.LEGS) > ((ArmorItem) itemStackLegs.getItem()).getMaterial().getDefenseForSlot(EquipmentSlot.LEGS)) || itemStackLegs.isEmpty())) {
                    this.client.inventoryUtils.swap(itemStackArmor, InventoryUtils.Section.ARMOR, 1);
                    return;
                }
                if (armor.getSlot() == EquipmentSlot.CHEST && ((itemStackChest.getItem() instanceof ArmorItem && armor.getMaterial().getDefenseForSlot(EquipmentSlot.CHEST) > ((ArmorItem) itemStackChest.getItem()).getMaterial().getDefenseForSlot(EquipmentSlot.CHEST)) || itemStackChest.isEmpty())) {
                    this.client.inventoryUtils.swap(itemStackArmor, InventoryUtils.Section.ARMOR, 2);
                    return;
                }
                if (armor.getSlot() == EquipmentSlot.HEAD && ((itemStackHead.getItem() instanceof ArmorItem && armor.getMaterial().getDefenseForSlot(EquipmentSlot.HEAD) > ((ArmorItem) itemStackHead.getItem()).getMaterial().getDefenseForSlot(EquipmentSlot.HEAD)) || itemStackHead.isEmpty())) {
                    this.client.inventoryUtils.swap(itemStackArmor, InventoryUtils.Section.ARMOR, 3);
                    return;
                }
            }
        }
        this.client.armorLastTick = this.client.inventoryUtils.getAll(item -> item instanceof ArmorItem);

        Entity entity = this.client.nearest(100, 100, 100, entity1 -> entity1 instanceof Player && (!(entity1 instanceof Client) || !this.ignoreClients));
        if ((this.client.tickCount() - this.client.getLastHurtByMobTimestamp()) < 100 && this.client.getLastHurtByMob() != null && this.client.getLastHurtByMob().getHealth() > 0 && (!(this.client.getLastHurtByMob() instanceof Client) || !this.ignoreClients)) {
            entity = this.client.getLastHurtByMob();
        }
        if (entity != null && this.client.canSee(entity)) {
            if (this.client.getHealth() < 3 && (this.client.distanceTo(new BlockPos(entity.position()), false, false) < (5 * 5)) && this.client.inventoryUtils.get(Items.SHIELD) != null && !this.client.getCooldowns().isOnCooldown(Items.SHIELD)) {
                //System.out.println("blocking");
                this.client.inventoryUtils.swap(new ItemStack(Items.SHIELD), InventoryUtils.Section.OFFHAND, 0);
                this.client.input.S = true;
                this.client.input.RIGHT_CLICK = true;
                this.client.lookAt(entity);
                return;
            }
            if (this.client.getHealth() < 5 && this.client.inventoryUtils.hasFood() && this.client.getFoodData().foodLevel != 20) {
                //System.out.println("help <2.5 hearts eating for quick regen");
                this.client.inventoryUtils.swap(this.client.inventoryUtils.weapon(), InventoryUtils.Section.ITEMS, 0);
                this.client.inventoryUtils.swap(this.client.inventoryUtils.optimalEating(), InventoryUtils.Section.OFFHAND, 0);
                this.client.input.RIGHT_CLICK = true;
                this.runForward(true);
                this.client.lookAway(entity);
                return;
            }
            if (this.client.getHealth() < 10) {
                if (this.client.inventoryUtils.hasFood() && this.client.distanceTo(new BlockPos(entity.position()), false, false) > (10 * 10) && this.client.getFoodData().foodLevel != 20) {
                    //System.out.println("help <5 hearts eating");
                    this.client.inventoryUtils.swap(this.client.inventoryUtils.weapon(), InventoryUtils.Section.ITEMS, 0);
                    this.client.inventoryUtils.swap(this.client.inventoryUtils.optimalEating(), InventoryUtils.Section.OFFHAND, 0);
                    this.client.input.RIGHT_CLICK = true;
                    this.runForward(true);
                    this.client.lookAway(entity);
                    return;
                }
                //System.out.println("help <5 hearts");
                this.runForward(true);
                this.client.lookAway(entity);
                if (this.client.isInWater() && !this.client.isSwimming() && this.client.level.getBlockState(new BlockPos(this.client.position()).below(2)).getMaterial() == Material.WATER) {
                    this.client.input.SHIFT = true;
                    this.client.input.SPACE = false;
                }
                else this.client.input.SHIFT = false;
                return;
            }
            if (this.client.getHealth() >= 10) {
                if (this.client.inventoryUtils.hasFood() && this.client.getFoodData().foodLevel <= 6) {
                    //System.out.println("eating can't sprint");
                    this.client.inventoryUtils.swap(this.client.inventoryUtils.weapon(), InventoryUtils.Section.ITEMS, 0);
                    this.client.inventoryUtils.swap(this.client.inventoryUtils.optimalEating(), InventoryUtils.Section.OFFHAND, 0);
                    this.client.input.RIGHT_CLICK = true;
                    this.runForward(true);
                    this.client.lookAt(entity);
                    return;
                }
                if (this.client.inventoryUtils.hasFood() && this.client.distanceTo(new BlockPos(entity.position()), false, false) > (20 * 20) && this.client.getFoodData().foodLevel != 20) {
                    //System.out.println("eating they are far away");
                    this.client.inventoryUtils.swap(this.client.inventoryUtils.weapon(), InventoryUtils.Section.ITEMS, 0);
                    this.client.inventoryUtils.swap(this.client.inventoryUtils.optimalEating(), InventoryUtils.Section.OFFHAND, 0);
                    this.client.input.RIGHT_CLICK = true;
                    this.runForward(true);
                    this.client.lookAt(entity);
                    return;
                }
                // 15 * 15
                if (this.client.distanceTo(new BlockPos(entity.position()), false, true) > (5 * 5) && !this.client.isInWater() && !this.client.isInLava() && this.client.inventoryUtils.shootingWeapon() != null) {
                    //System.out.println("shooting them they far away");
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
                        this.client.input.RIGHT_CLICK = true;
                        this.runForward(false);
                        if (this.client.keepChargingWeapon <= 0) {
                            this.client.keepChargingWeapon = (shootingWeapon.getItem() instanceof BowItem || shootingWeapon.getItem() instanceof TridentItem) ? 22 : shootingWeapon.getItem().getUseDuration(shootingWeapon);
                            Vec3 velocity = MathUtils.calculateArrowVelocity(this.client);
                            float angle = MathUtils.calculateShootingAngle(velocity, this.client.distanceTo(new BlockPos(entity.position()), true, true), this.client.getY() - entity.getY());
                            float angle1 = MathUtils.calculateShootingAngelHorizontal(this.client.level, entity.getEyePosition(), this.client.getEyePosition(), entity.getDeltaMovement(), velocity);
                            if (shootingWeapon.getItem() instanceof CrossbowItem && this.client.inventoryUtils.get(item -> item instanceof FireworkRocketItem) != null) {
                                this.client.lookAt(entity);
                                this.client.input.RIGHT_CLICK = false;
                                if (!this.isPassable(this.client.front())) {
                                    this.client.input.SPACE = true;
                                }
                                return;
                            }
                            if (!Float.isNaN(angle) && !Float.isNaN(angle1)) {
                                //this.client.lookAt(entity);
                                System.out.println(angle1);
                                this.client.look(angle1, angle); // this.client.getYRot()
                                this.client.input.RIGHT_CLICK = false;
                                if (!this.isPassable(this.client.front())) {
                                    this.client.input.SPACE = true;
                                }
                                return;
                            }
                        }
                        return;
                    }
                }
                //TODO ItemBlock is also seeds bruh
                if (!this.client.isInWater() && entity.getY() - this.client.getY() >= 5.0D && this.client.distanceTo(entity.getX(), entity.getY(), entity.getZ(), false, true) < (5 * 5) && this.client.inventoryUtils.get(item -> item instanceof BlockItem && item.getItemCategory() == CreativeModeTab.TAB_BUILDING_BLOCKS) != null) {
                    //System.out.println("stacking up");
                    this.client.inventoryUtils.swap(this.client.inventoryUtils.get(item -> item instanceof BlockItem && item.getItemCategory() == CreativeModeTab.TAB_BUILDING_BLOCKS), InventoryUtils.Section.ITEMS, 0);
                    this.center();
                    this.client.look(this.client.getYRot(), 90.0F);
                    if (!this.isPassable(new BlockPos(this.client.position()).below())) {
                        this.client.input.SHIFT = true;
                    }
                    this.client.input.SPACE = true;
                    this.client.input.RIGHT_CLICK = true;
                    return;
                }
                if (!this.client.isInWater() && entity.getY() - this.client.getY() >= 5.0D && this.client.distanceTo(entity.getX(), entity.getY(), entity.getZ(), false, true) >= (2 * 2) && this.client.inventoryUtils.get(item -> item instanceof BlockItem && item.getItemCategory() == CreativeModeTab.TAB_BUILDING_BLOCKS) != null) {
                    //System.out.println("bridging staircase");
                    this.client.inventoryUtils.swap(this.client.inventoryUtils.get(item -> item instanceof BlockItem && item.getItemCategory() == CreativeModeTab.TAB_BUILDING_BLOCKS), InventoryUtils.Section.ITEMS, 0);
                    this.client.input.S = true;
                    this.client.input.D = true;
                    this.client.input.SHIFT = true;
                    this.centerBridge(entity);
                    this.client.look(this.direction(entity), 75.0F);
                    if (!this.isPassable(new BlockPos(this.client.position()).below())) {
                        this.client.input.SPACE = true;
                        this.client.input.SHIFT = false;
                    }
                    this.client.input.RIGHT_CLICK = true;
                    return;
                }
                if (!this.client.isInWater() && MathUtils.percentOf(MathUtils.sphere(new BlockPos(this.client.position()), 5), this::isPassable) > 90.0D && entity.getY() - this.client.getY() >= 0.0D && this.client.distanceTo(entity.getX(), entity.getY(), entity.getZ(), false, true) >= (2 * 2) && this.client.inventoryUtils.get(item -> item instanceof BlockItem && item.getItemCategory() == CreativeModeTab.TAB_BUILDING_BLOCKS) != null) {
                    //System.out.println("bridging");
                    this.client.inventoryUtils.swap(this.client.inventoryUtils.get(item -> item instanceof BlockItem && item.getItemCategory() == CreativeModeTab.TAB_BUILDING_BLOCKS), InventoryUtils.Section.ITEMS, 0);
                    this.client.input.S = true;
                    this.client.input.D = true;
                    this.client.input.SHIFT = true;
                    this.centerBridge(entity);
                    this.client.look(this.direction(entity), 75.0F);
                    if (this.client.rayTrace(-1) == null || this.client.rayTrace(-1).getHitBlock() == null) {
                        this.client.input.D = false;
                        this.client.input.S = true;
                    }
                    if (!this.isPassable(new BlockPos(this.client.position()).below())) {
                        this.client.input.SHIFT = false;
                    }
                    this.client.input.RIGHT_CLICK = true;
                    return;
                }
                //System.out.println("attacking");
                ItemStack weapon = this.client.inventoryUtils.weapon();
                if (((LivingEntity) entity).isBlocking() && this.client.inventoryUtils.get(item -> item instanceof AxeItem) != null) {
                    weapon = this.client.inventoryUtils.get(item -> item instanceof AxeItem);
                }
                this.client.inventoryUtils.swap(weapon, InventoryUtils.Section.ITEMS, 0);
                this.runForward(true);
                this.client.lookAt(entity);
                if (this.client.isInWater() && !this.client.isSwimming() && this.client.level.getBlockState(new BlockPos(this.client.position()).below(2)).getMaterial() == Material.WATER) {
                    this.client.input.SHIFT = true;
                    this.client.input.SPACE = false;
                }
                else this.client.input.SHIFT = false;
                if ((MathUtils.distanceSquared(this.client.getX(), this.client.getEyeY(), this.client.getZ(), entity.getX(), entity.getEyeY(), entity.getZ(), false, false) < (5 * 5)) && !this.client.input.LEFT_CLICK) {
                    this.client.lookAt(entity);
                    if (this.client.waitForRecharge <= 0) {
                        this.clickLeft();
                    }
                }
                return;
            }
        }

        // if goal not achieved or goal not possible

        if (this.client.getFoodData().foodLevel != 20) {
            if (this.client.inventoryUtils.hasFood()) {
                //System.out.println("eating");
                this.client.inventoryUtils.swap(this.client.inventoryUtils.weapon(), InventoryUtils.Section.ITEMS, 0);
                this.client.inventoryUtils.swap(this.client.inventoryUtils.optimalEating(), InventoryUtils.Section.OFFHAND, 0);
                this.client.input.RIGHT_CLICK = true;
                return;
            }
            List<Item> items = Arrays.asList(Items.PORKCHOP, Items.BEEF, Items.MUTTON, Items.CHICKEN, Items.COD, Items.SALMON);
            Entity item = this.client.nearest(20, 20, 20, entity1 -> entity1 instanceof ItemEntity && items.contains(((ItemEntity) entity1).getItem().getItem()) && this.client.canSee(entity1));
            if (item != null) {
                //System.out.println("getting food (picking up item)");
                this.runForward(true);
                this.client.lookAt(item);
                if (this.client.isInWater() && !this.client.isSwimming() && this.client.level.getBlockState(new BlockPos(this.client.position()).below(2)).getMaterial() == Material.WATER) {
                    this.client.input.SHIFT = true;
                    this.client.input.SPACE = false;
                }
                else this.client.input.SHIFT = false;
                return;
            }
            List<EntityType<?>> entities = Arrays.asList(EntityType.PIG, EntityType.COW, EntityType.SHEEP, EntityType.CHICKEN, EntityType.COD, EntityType.SALMON);
            Entity entity1 = this.client.nearest(50, 50, 50, entity2 -> entities.contains(entity2.getType()) && this.client.canSee(entity2) && ((LivingEntity) entity2).getHealth() > 0);
            if (entity1 != null) {
                //System.out.println("getting food (killing animals)");
                this.client.inventoryUtils.swap(this.client.inventoryUtils.weapon(), InventoryUtils.Section.ITEMS, 0);
                this.runForward(true);
                this.client.lookAt(entity1);
                if (this.client.isInWater() && !this.client.isSwimming() && this.client.level.getBlockState(new BlockPos(this.client.position()).below(2)).getMaterial() == Material.WATER) {
                    this.client.input.SHIFT = true;
                    this.client.input.SPACE = false;
                }
                else this.client.input.SHIFT = false;
                if ((MathUtils.distanceSquared(this.client.getX(), this.client.getEyeY(), this.client.getZ(), entity1.getX(), entity1.getEyeY(), entity1.getZ(), false, false) < (5 * 5)) && !this.client.input.LEFT_CLICK) {
                    this.client.lookAt(entity1);
                    if (this.client.waitForRecharge <= 0) {
                        this.clickLeft();
                    }
                }
            }
        }
    }

    //TODO make running smarter
    private void runForward(boolean space) {
        this.client.input.SPRINT = true;
        this.client.input.W = true;
        if (space) this.client.input.SPACE = true;
    }

    private boolean isPassable(BlockPos position) {
        return this.client.level.getBlockState(position).getCollisionShape(this.client.level, position).isEmpty();
    }

    private void center() {
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

    private void centerBridge(Entity entity) {
        double x0 = this.client.getX();
        double z0 = this.client.getZ();
        double x1 = Mth.floor(x0) + 0.5D;
        double z1 = Mth.floor(z0) + 0.5D;
        Direction direction = this.directionDir(entity);
        double difX = x1 - x0;
        double difZ = z1 - z0;
        if (direction == Direction.NORTH) {
            difX -= 0.2D;
            if (difX > 0) {
                this.client.input.S = false;
            }
            else if (difX < 0) {
                this.client.input.D = false;
            }
            return;
        }
        if (direction == Direction.SOUTH) {
            difX += 0.2D;
            if (difX > 0) {
                this.client.input.D = false;
            }
            else if (difX < 0) {
                this.client.input.S = false;
            }
            return;
        }
        if (direction == Direction.EAST) {
            difZ -= 0.2D;
            if (difZ > 0) {
                this.client.input.S = false;
            }
            else if (difZ < 0) {
                this.client.input.D = false;
            }
            return;
        }
        if (direction == Direction.WEST) {
            difZ += 0.2D;
            if (difZ > 0) {
                this.client.input.D = false;
            }
            else if (difZ < 0) {
                this.client.input.S = false;
            }
        }
    }

    public Direction directionDir(Entity entity) {
        float yaw = MathUtils.yawPitch(new Vec3(entity.getX() - this.client.getX(), 0.0D, entity.getZ() - this.client.getZ()))[0];
        return Direction.fromYRot(yaw).getOpposite();
    }

    public float direction(Entity entity) {
        float yaw = MathUtils.yawPitch(new Vec3(entity.getX() - this.client.getX(), 0.0D, entity.getZ() - this.client.getZ()))[0];
        Direction direction = Direction.fromYRot(yaw).getOpposite();
        if (direction == Direction.NORTH) {
            return -135.0F;
        }
        else if (direction == Direction.SOUTH) {
            return 45.0F;
        }
        else if (direction == Direction.EAST) {
            return -45.0F;
        }
        else if (direction == Direction.WEST) {
            return 135.0F;
        }
        return -1.0F;
    }

    public void clickLeft() {
        this.client.input.LEFT_CLICK = true;
        this.scheduler.runTaskLater(this.plugin, () -> this.client.input.LEFT_CLICK = false, 2);
    }

    public void clickRight() {
        this.client.input.RIGHT_CLICK = true;
        this.scheduler.runTaskLater(this.plugin, () -> this.client.input.RIGHT_CLICK = false, 2);
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

    public void startup() {
        this.active = true;
    }

    public void shutDown() {
        this.releaseAll();
        this.active = false;
    }
}
