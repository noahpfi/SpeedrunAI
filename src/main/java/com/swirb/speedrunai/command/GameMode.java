package com.swirb.speedrunai.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameMode implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        try {
            if (args != null) {
                if (args[0].equalsIgnoreCase("creative")) {
                    ((Player) sender).setGameMode(org.bukkit.GameMode.CREATIVE);
                }
                else if (args[0].equalsIgnoreCase("survival")) {
                    ((Player) sender).setGameMode(org.bukkit.GameMode.SURVIVAL);
                }
                else if (args[0].equalsIgnoreCase("spectator")) {
                    ((Player) sender).setGameMode(org.bukkit.GameMode.SPECTATOR);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            sender.sendMessage("try again. wrong inputs lmaooo");
        }
        return false;
    }
}