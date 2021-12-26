package me.sfiguz7.transcendence.implementation.core.commands;

import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
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
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "末地科技 > " + ChatColor.GRAY +
                    "https://github.com/Sfiguz7/TranscEndence/wiki/Walkthrough-guide-thingy");
            } else if (args[0].equalsIgnoreCase("list")) {
                Set<UUID> uuids =
                        TranscEndence.getRegistry().getDaxiEffectPlayers().keySet();
                StringBuilder list = new StringBuilder().append(ChatColor.LIGHT_PURPLE);
                if (uuids.isEmpty()) {
                    list.append("没有玩家拥有超级药水效果!");
                } else {
                    for (UUID uuid : uuids) {
                        list.append(Bukkit.getOfflinePlayer(uuid).getName()).append(' ');
                    }
                }
                sender.sendMessage(list.toString());
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
                            StringBuilder message = new StringBuilder("刷新超级药水: ");
                            for (Daxi.Type t : effects) {
                                message.append(" ").append(t);
                            }
                            Bukkit.getScheduler().runTask(TranscEndence.getInstance(), () -> Daxi.reapplyEffects(p));
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + "末地科技 > " + ChatColor.RED +
                                message);

                        } else {
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + "末地科技 > " + ChatColor.RED +
                                "该玩家没有超级药水效果!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "末地科技 > " + ChatColor.RED +
                            "该玩家不存在!");
                    }
                } else {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "末地科技 > " + ChatColor.RED +
                        "权限不足!");
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
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + "末地科技 > " + ChatColor.RED +
                                "已为" + p.getDisplayName() + "打开超级药水刷新功能");
                        } else {
                            toggledPlayers.add(uuid);
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + "末地科技 > " + ChatColor.RED +
                            		"已为" + p.getDisplayName() + "关闭超级药水刷新功能");
                        }
                    } else {
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "末地科技 > " + ChatColor.RED +
                            "该玩家不存在!");
                    }
                } else {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "末地科技 > " + ChatColor.RED +
                        "没有权限!");
                }
            }
        } else {
            sendHelp(sender);
        }
        return true;
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColors.color("&a末地科技 &2v" + TranscEndence.getVersion()));

        sender.sendMessage(ChatColors.color("&3/te guide &b") + "获取末地科技指南书");
        sender.sendMessage(ChatColors.color("&3/te walkthrough &b") + "获取末地科技帮助链接");
        if (sender.hasPermission("te.command.reapply")) {
            sender.sendMessage(ChatColors.color("&3/te reapply <name> &b") + "为 <name> 刷新超级药水效果");
        }
        if (sender.hasPermission("te.command.toggle")) {
            sender.sendMessage(ChatColors.color("&3/te toggle <name> &b") + "为 <name> 开关超级药水刷新功能");
        }
    }

}

