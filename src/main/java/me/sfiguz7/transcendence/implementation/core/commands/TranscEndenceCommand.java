package me.sfiguz7.transcendence.implementation.core.commands;

import me.mrCookieSlime.Slimefun.cscorelib2.chat.ChatColors;
import me.sfiguz7.transcendence.TranscEndence;
import me.sfiguz7.transcendence.implementation.items.items.Daxi;
import me.sfiguz7.transcendence.implementation.listeners.TranscEndenceGuideListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class TranscEndenceCommand implements CommandExecutor {

    @Override
    // TODO: sort this mess into subcommands because it's getting obnoxious
    // TODO: move all sender.sendMessage's to some structured, cleaned up place
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("guide")
                && sender instanceof Player) {
                Player p = (Player) sender;
                p.getInventory().addItem(TranscEndenceGuideListener.getGuide());
            } else if (args[0].equalsIgnoreCase("walkthrough")) {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "进阶模组 > " + ChatColor.GRAY +
                    "https://github.com/Sfiguz7/TranscEndence/wiki/Walkthrough-guide-thingy");
            } else {
                sendHelp(sender);
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reapply")) {
                if (sender.hasPermission("te.command.reapply")) {
                    Player p = Bukkit.getPlayer(args[1]);
                    if (p != null) {
                        Set<Daxi.Type> effects =
                            TranscEndence.getRegistry().getDaxiEffectPlayers().get(p.getUniqueId());
                        if (effects != null) {
                            StringBuilder message = new StringBuilder("Reapplied: ");
                            for (Daxi.Type t : effects) {
                                message.append(" ").append(t);
                            }
                            Bukkit.getScheduler().runTask(TranscEndence.getInstance(), () -> Daxi.reapplyEffects(p));
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + "进阶模组 > " + ChatColor.RED +
                                message);

                        } else {
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + "进阶模组 > " + ChatColor.RED +
                                "The chosen player has no active Daxi!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "进阶模组 > " + ChatColor.RED + "The chosen " +
                            "player does not exist!");
                    }
                } else {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "进阶模组 > " + ChatColor.RED +
                        "Insufficient permissions!");
                }
            } else if (args[0].equalsIgnoreCase("toggle")) {
                if (sender.hasPermission("te.command.toggle")) {
                    Player p = Bukkit.getPlayer(args[1]);
                    if (p != null) {
                        Set<UUID> toggledPlayers = TranscEndence.getRegistry().getToggledPlayers();
                        UUID uuid = p.getUniqueId();
                        if (toggledPlayers.contains(uuid)) {
                            toggledPlayers.remove(uuid);
                            Bukkit.getScheduler().runTask(TranscEndence.getInstance(), () -> Daxi.reapplyEffects(p));
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + "进阶模组 > " + ChatColor.RED +
                                "Effects refreshing toggled ON for " + p.getDisplayName());
                        } else {
                            toggledPlayers.add(uuid);
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + "进阶模组 > " + ChatColor.RED +
                                "Effects refreshing toggled OFF for " + p.getDisplayName());
                        }
                    } else {
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "进阶模组 > " + ChatColor.RED + "The chosen " +
                            "player does not exist!");
                    }
                } else {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "进阶模组 > " + ChatColor.RED +
                        "Insufficient permissions!");
                }
            }
        } else {
            sendHelp(sender);
        }
		return false;
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColors.color("&a进阶模组 &2v" + TranscEndence.getVersion()));

        sender.sendMessage(ChatColors.color("&3/te guide &b") + "获取一个进阶模组指南");
        sender.sendMessage(ChatColors.color("&3/te walkthrough &b") + "获取帮助链接");
        if (sender.hasPermission("te.command.reapply")) {
            sender.sendMessage(ChatColors.color("&3/te reapply <name> &b") + "Reapplies Daxi effects to <name>");
        }
        if (sender.hasPermission("te.command.toggle")) {
            sender.sendMessage(ChatColors.color("&3/te toggle <name> &b") + "Toggles Daxi effects refreshing for <name>");
        }
    }

}

