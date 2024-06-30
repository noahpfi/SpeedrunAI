package com.swirb.statues.client.command;

import com.swirb.statues.Statues;
import com.swirb.statues.client.Client;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R2.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.List;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        try {
            if (args.length != 0) {
                if (args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage("HELP:");
                    sender.sendMessage("/statues join [name] [skin name] [amount] : (skin name is the name of the Player with that skin)");
                    sender.sendMessage("/statues disconnect [name]");
                    sender.sendMessage("/statues disconnectAll");
                    sender.sendMessage("/statues respawn [name]");
                    sender.sendMessage("/statues inventory [name] : allows you to look into a players inventory");
                    sender.sendMessage("/statues swap [name] : swaps items in players hands");
                    sender.sendMessage("/statues armor [name] : equips the armor the player has (it will use better armor over worse)");
                    sender.sendMessage("IMPORTANT");
                    sender.sendMessage("if any of the above commands don't work or you want to do something else, ALL VANILLA COMMANDS WORK");
                }
                else if (args[0].equalsIgnoreCase("join")) {
                    if (sender instanceof BlockCommandSender) {
                        Statues.getClientHandler().createClientsCommandBlock((CraftBlockCommandSender) sender, args[1], args[2], (int) Double.parseDouble(args[3]));
                    }
                    else {
                        Statues.getClientHandler().createClients((Player) sender, args[1], args[2], (int) Double.parseDouble(args[3]));
                    }
                }
                else if (args[0].equalsIgnoreCase("disconnect")) {
                    Statues.getClientHandler().disconnect(Statues.getClientHandler().get(args[1]), "[Commands] disconnected");
                }
                else if (args[0].equalsIgnoreCase("disconnectAll")) {
                    for (Client client : Statues.getClientHandler().clients()) {
                        Statues.getClientHandler().disconnect(client, "[Commands] disconnected");
                    }
                }
                else if (args[0].equalsIgnoreCase("respawn")) {
                    Statues.getClientHandler().respawn(Statues.getClientHandler().get(args[1]));
                }
                else if (args[0].equalsIgnoreCase("inventory")) {
                    Inventory inventory = Statues.getClientHandler().get(args[1]).getBukkitEntity().getInventory();
                    ((Player) sender).openInventory(inventory);
                }
                else if (args[0].equalsIgnoreCase("swap")) {
                    Statues.getClientHandler().get(args[1]).swapItemInHands();
                }
                else if (args[0].equalsIgnoreCase("armor")) {
                    Statues.getClientHandler().get(args[1]).equipArmor();
                }
            }
        }
        catch (Exception ex) {
            sender.sendMessage("try again. wrong inputs lmaooo (you probably forgot to write the name of a bot after)");
        }
        return false;
    }
}