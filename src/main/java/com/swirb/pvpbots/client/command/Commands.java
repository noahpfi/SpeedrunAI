package com.swirb.pvpbots.client.command;

import com.swirb.pvpbots.PvPBots;
import com.swirb.pvpbots.basicallyGarbage.Train;
import com.swirb.pvpbots.basicallyGarbage.ai.SigmoidActivation;
import com.swirb.pvpbots.basicallyGarbage.ai3.AI3;
import com.swirb.pvpbots.basicallyGarbage.ai2.AI2;
import com.swirb.pvpbots.client.Client;
import com.swirb.pvpbots.client.ClientHandler;
import com.swirb.pvpbots.client.utils.Debug;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class Commands implements CommandExecutor {

    /**
     * all of this is garbage lmao i was too lazy to add checks
     */

    private final ClientHandler clientHandler;
    private BlockPos blockPos;

    public Commands(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        try {
            if (args != null) {
                if (args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage("HELP");
                }
                else if (args[0].equalsIgnoreCase("join")) {
                    try {
                        this.clientHandler.createClients((Player) sender, args[1], (int) Double.parseDouble(args[2]));
                    }
                    catch (Exception ignored) {
                        this.clientHandler.createClients((Player) sender, args[1], 1);
                    }
                }
                else if (args[0].equalsIgnoreCase("disconnect")) {
                    this.clientHandler.disconnect(this.clientHandler.get(args[1]), "[Commands] disconnected");
                }
                else if (args[0].equalsIgnoreCase("disconnectAll")) {
                    for (Client client : this.clientHandler.clients()) {
                        this.clientHandler.disconnect(client, "[Commands] disconnected");
                    }
                }
                else if (args[0].equalsIgnoreCase("respawn")) {
                    this.clientHandler.respawn(this.clientHandler.get(args[1]));
                }
                else if (args[0].equalsIgnoreCase("startAll")) {
                    for (Client client : this.clientHandler.clients()) {
                        client.controller().startup();
                    }
                }
                else if (args[0].equalsIgnoreCase("kit")) {
                    Client client = this.clientHandler.get(args[1]);
                    if (client != null) {
                        for (ItemStack itemStack : this.kit()) {
                            client.getInventory().add(itemStack);
                        }
                    }
                }
                else if (args[0].equalsIgnoreCase("kitAll")) {
                    for (Client client : this.clientHandler.clients()) {
                        for (ItemStack itemStack : this.kit()) {
                            client.getInventory().add(itemStack);
                        }
                    }
                }
                else if (args[0].equalsIgnoreCase("inv")) {
                    Inventory inventory = this.clientHandler.get(args[1]).getBukkitEntity().getInventory();
                    ((Player) sender).openInventory(inventory);
                }
                else if (args[0].equalsIgnoreCase("start")) {
                    this.clientHandler.get(args[1]).controller().startup();
                }
                else if (args[0].equalsIgnoreCase("stop")) {
                    this.clientHandler.get(args[1]).controller().shutDown();
                }
                else if (args[0].equalsIgnoreCase("stopAll")) {
                    for (Client client : this.clientHandler.clients()) {
                        client.controller().shutDown();
                    }
                }
                else if (args[0].equalsIgnoreCase("train")) {
                    new Train(((CraftPlayer) sender).getHandle().level);
                }
                else if (args[0].equalsIgnoreCase("ai")) {
                    AI2 ai = new AI2(((CraftPlayer) sender).getHandle().level, 10, new SigmoidActivation());
                    new BukkitRunnable() {
                        public void run() {
                            if (ai.train(50000)) {
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(PvPBots.getInstance(), 0, 50);
                }
                else if (args[0].equalsIgnoreCase("aisetup")) {
                    AI2 ai = new AI2(((CraftPlayer) sender).getHandle().level, 10, new SigmoidActivation());
                    ai.trainer().setup();
                    Bukkit.getScheduler().runTaskLater(PvPBots.getInstance(), () -> ai.trainer().nextGeneration(), 50);
                    Bukkit.getScheduler().runTaskLater(PvPBots.getInstance(), () -> ai.trainer().nextGeneration(), 100);
                }
                else if (args[0].equalsIgnoreCase("ai3")) {
                    AI3 ai = new AI3(((CraftPlayer) sender).getHandle().level, new SigmoidActivation(), 10);
                    new BukkitRunnable() {
                        public void run() {
                            if (ai.train(Double.MAX_VALUE)) {
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(PvPBots.getInstance(), 0, 0);
                }
                else if (args[0].equalsIgnoreCase("click")) {
                    this.clientHandler.get(args[1]).input.RIGHT_CLICK = true;
                }
                else if (args[0].equalsIgnoreCase("punch")) {
                    this.clientHandler.get(args[1]).input.LEFT_CLICK = true;
                }
                else if (args[0].equalsIgnoreCase("swap")) {
                    this.clientHandler.get(args[1]).swapItemInHands();
                }
                else if (args[0].equalsIgnoreCase("jump")) {
                    this.clientHandler.get(args[1]).input.SPACE = true;
                }
                // for testing the mojang raytrace cutting corner thing
                else if (args[0].equalsIgnoreCase("sight")) {
                    BlockPos p0 = new BlockPos(56, 89, 718);
                    BlockPos p1 = new BlockPos(56, 90, 719);
                    Level level = ((CraftPlayer) sender).getHandle().level;
                    Debug.visualizeBlockPosition(level, p0, Color.RED, 1.0F);
                    Debug.visualizeBlockPosition(level, p1, Color.YELLOW, 1.0F);
                    System.out.println(this.sight(level, p0, p1));
                    //if ray start inside a block, no sight
                    //any corners will interfere, no sight
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            sender.sendMessage("try again. wrong inputs lmaooo (you probably forgot to write the name of a bot after)");
        }
        return false;
    }

    private List<ItemStack> kit() {
        return Arrays.asList(new ItemStack(Items.DIAMOND_BOOTS, 1),
                new ItemStack(Items.DIAMOND_LEGGINGS, 1),
                new ItemStack(Items.DIAMOND_CHESTPLATE, 1),
                new ItemStack(Items.DIAMOND_HELMET, 1),
                new ItemStack(Items.IRON_AXE, 1),
                new ItemStack(Items.NETHERITE_SWORD, 1),
                new ItemStack(Items.SHIELD, 1),
                new ItemStack(Items.BOW, 1),
                new ItemStack(Items.ARROW, 64),
                new ItemStack(Items.COOKED_PORKCHOP, 32),
                new ItemStack(Items.BREAD, 16),
                new ItemStack(Items.DRIED_KELP, 16),
                new ItemStack(Items.COBBLESTONE, 64),
                new ItemStack(Items.COBBLESTONE, 64),
                new ItemStack(Items.COBBLESTONE, 64),
                new ItemStack(Items.WATER_BUCKET, 1)
        );
    }

    private boolean sight(Level level, BlockPos from, BlockPos to) {
        if (from == null || to == null) {
            return false;
        }
        Vec3 vec3 = Vec3.atCenterOf(from);
        Vec3 vec31 = Vec3.atCenterOf(to);
        return level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to);
    }
}