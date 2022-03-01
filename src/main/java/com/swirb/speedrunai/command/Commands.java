package com.swirb.speedrunai.command;

import com.swirb.speedrunai.Debug;
import com.swirb.speedrunai.Theta;
import com.swirb.speedrunai.client.Client;
import com.swirb.speedrunai.client.ClientHandler;
import com.swirb.speedrunai.path.TestPath;
import com.swirb.speedrunai.path.ThetaStar;
import com.swirb.speedrunai.utils.ChatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.List;

public class Commands implements CommandExecutor {

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
                    sender.sendMessage("commands you can use:");
                    sender.sendMessage("/bot join [anyName] [(optional) amount] => spawns bots at world spawn or where they last logged out.");
                    sender.sendMessage("/bot disconnect [anyName] => disconnects (kicks) a bot from the server");
                    sender.sendMessage("/bot disconnectAll => disconnects (kicks) all bots from the server");
                    sender.sendMessage("/bot respawn [anyName] => respawns a bot after it died (they respawn by themselves so no need to use)");
                    sender.sendMessage("/bot start [anyName] => starts the bot with that name (it will start hunting you)");
                    sender.sendMessage("/bot startAll => starts all bots (they will start hunting you)");
                    sender.sendMessage("/bot stop [anyName] => stops the bot (it will stop hunting you)");
                    sender.sendMessage("/bot stopAll => stops all bots (they will stop hunting you)");
                    sender.sendMessage("/bot ignoreBots => toggles the bots attacking each other");
                    sender.sendMessage("/bot kit [anyName] => give that bot a default kit");
                    sender.sendMessage("/bot kitAll => give all bots the default kit");
                    sender.sendMessage(ChatUtils.DASH_THIN);
                    sender.sendMessage("TIPS");
                    sender.sendMessage("since you don't have permissions for commands");
                    sender.sendMessage("use the /gamemode [creative/survival/spectator] command");
                    sender.sendMessage("to give yourself and the bots stuff");
                    sender.sendMessage(ChatUtils.DASH_THIN);
                    sender.sendMessage("you can use /bot kit to give the bots items too!");
                    sender.sendMessage(ChatUtils.DASH_THIN);
                    sender.sendMessage("the bots will get their own food if they are started and hungry, but they wont get their own stuff.");
                    sender.sendMessage("give them stuff to use by throwing it out, they will pick it up and use it (if they can use it)");
                    sender.sendMessage(ChatUtils.DASH_THIN);
                    sender.sendMessage("IMPORTANT");
                    sender.sendMessage("before you leave, make sure to DISCONNECT ALL THE BOTS");
                    sender.sendMessage("(they take up playercount and noone will be able to join)");
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
                /*
                else if (args[0].equalsIgnoreCase("ignoreBots")) {
                    boolean ignore = false;
                    for (Client client : this.clientHandler.clients()) {
                        client.controller().ignoreClients = !client.controller().ignoreClients;
                        ignore = client.controller().ignoreClients;
                    }
                    sender.sendMessage("bots will now " + (ignore ? "ignore" : "attack") + " other bots");
                }

                 */
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
                else if (args[0].equalsIgnoreCase("click")) {
                    this.clientHandler.get(args[1]).input.RIGHT_CLICK = true;
                }
                else if (args[0].equalsIgnoreCase("theta")) {
                    ThetaStar theta = new ThetaStar(this.clientHandler.get(args[1]), this.clientHandler.get(args[1]).blockPosition(), ((CraftPlayer) sender).getHandle().blockPosition());
                    theta.calculate(false);
                }
                else if (args[0].equalsIgnoreCase("set")) {
                    this.blockPos = ((CraftPlayer) sender).getHandle().blockPosition();
                }
                else if (args[0].equalsIgnoreCase("test")) {
                    TestPath testPath = new TestPath(((CraftPlayer) sender).getHandle().level, this.blockPos, ((CraftPlayer) sender).getHandle().blockPosition());
                    testPath.calculate();
                }
                else if (args[0].equalsIgnoreCase("t")) {
                    new Theta(((CraftPlayer) sender).getHandle());
                }
                else if (args[0].equalsIgnoreCase("visible")) {
                    BlockPos p0 = new BlockPos(1297, 127, -1015);
                    BlockPos p1 = new BlockPos(1298, 127, -1014);
                    Debug.visualizeBlockPosition(((CraftPlayer) sender).getHandle().level, p0, Color.GREEN, 1.0F);
                    Debug.visualizeBlockPosition(((CraftPlayer) sender).getHandle().level, p1, Color.RED, 1.0F);
                    System.out.println(this.lineOfSight(((CraftPlayer) sender).getHandle().level, p0, p1));
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            sender.sendMessage("try again. wrong inputs lmaooo (you probably forgot to write the name of a bot after)");
        }
        return false;
    }

    public boolean lineOfSight(Level level, BlockPos from, BlockPos to) {
        if (from == null || to == null) {
            return false;
        }
        Vec3 vec3 = Vec3.atCenterOf(from);
        Vec3 vec31 = Vec3.atCenterOf(to);
        return level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to);
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
}