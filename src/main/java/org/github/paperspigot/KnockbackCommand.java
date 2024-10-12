//FlamePaper - Customizable-knockback
package org.github.paperspigot;

import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class KnockbackCommand extends Command {
    private static final List<String> ARGUMENTS = ImmutableList.of("friction", "horizontal", "vertical", "vertical-limit", "extra-horizontal", "extra-vertical", "reset", "show");

    // Default values
    private final double knockbackFriction, knockbackHorizontal, knockbackVertical, knockbackVerticalLimit,
            knockbackExtraHorizontal, knockbackExtraVertical;

    public KnockbackCommand(String name, double knockbackFriction, double knockbackHorizontal,
                                        double knockbackVertical, double knockbackVerticalLimit,
                                        double knockbackExtraHorizontal, double knockbackExtraVertical) {
        super(name);
        this.description = "Modify the knockback configuration";
        this.usageMessage = "/knockback " +
                "<friction|horizontal|vertical|vertical-limit|extra-horizontal|extra-vertical|reset|show> <value>";
        this.setPermission("bukkit.command.knockback");
        this.knockbackFriction = knockbackFriction;
        this.knockbackHorizontal = knockbackHorizontal;
        this.knockbackVertical = knockbackVertical;
        this.knockbackVerticalLimit = knockbackVerticalLimit;
        this.knockbackExtraHorizontal = knockbackExtraHorizontal;
        this.knockbackExtraVertical = knockbackExtraVertical;
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) {
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return true;
        }


        try {
            switch (args[0].toLowerCase()) {
                case "friction": {
                    double oldVal = top.speedcubing.paper.CubingPaperConfig.knockbackFriction;
                    double newVal = parseValue(args);
                    top.speedcubing.paper.CubingPaperConfig.knockbackFriction = newVal;
                    updated(sender, "friction", oldVal, newVal);
                    break;
                }
                case "horizontal": {
                    double oldVal = top.speedcubing.paper.CubingPaperConfig.knockbackHorizontal;
                    double newVal = parseValue(args);
                    top.speedcubing.paper.CubingPaperConfig.knockbackHorizontal = newVal;
                    updated(sender, "horizontal knockback", oldVal, newVal);
                    break;
                }
                case "vertical": {
                    double oldVal = top.speedcubing.paper.CubingPaperConfig.knockbackVertical;
                    double newVal = parseValue(args);
                    top.speedcubing.paper.CubingPaperConfig.knockbackVertical = newVal;
                    updated(sender, "vertical knockback", oldVal, newVal);
                    break;
                }
                case "vertical-limit": {
                    double oldVal = top.speedcubing.paper.CubingPaperConfig.knockbackVerticalLimit;
                    double newVal = parseValue(args);
                    top.speedcubing.paper.CubingPaperConfig.knockbackVerticalLimit = newVal;
                    updated(sender, "vertical limit", oldVal, newVal);
                    break;
                }
                case "extra-horizontal": {
                    double oldVal = top.speedcubing.paper.CubingPaperConfig.knockbackExtraHorizontal;
                    double newVal = parseValue(args);
                    top.speedcubing.paper.CubingPaperConfig.knockbackExtraHorizontal = newVal;
                    updated(sender, "horizontal (extra)", oldVal, newVal);
                    break;
                }
                case "extra-vertical": {
                    double oldVal = top.speedcubing.paper.CubingPaperConfig.knockbackExtraVertical;
                    double newVal = parseValue(args);
                    top.speedcubing.paper.CubingPaperConfig.knockbackExtraVertical = newVal;
                    updated(sender, "vertical (extra)", oldVal, newVal);
                    break;
                }
                case "reset":
                    top.speedcubing.paper.CubingPaperConfig.knockbackFriction = knockbackFriction;
                    top.speedcubing.paper.CubingPaperConfig.knockbackHorizontal = knockbackHorizontal;
                    top.speedcubing.paper.CubingPaperConfig.knockbackVertical = knockbackVertical;
                    top.speedcubing.paper.CubingPaperConfig.knockbackVerticalLimit = knockbackVerticalLimit;
                    top.speedcubing.paper.CubingPaperConfig.knockbackExtraHorizontal = knockbackExtraHorizontal;
                    top.speedcubing.paper.CubingPaperConfig.knockbackExtraVertical = knockbackExtraVertical;
                    sender.sendMessage(ChatColor.GREEN + "Knockback config reset to values from file.");
                    break;
                case "show":
                    sender.sendMessage(ChatColor.GOLD + "Knockback Configuration");
                    sendValue(sender, "Friction", top.speedcubing.paper.CubingPaperConfig.knockbackFriction);
                    sendValue(sender, "Horizontal Knockback", top.speedcubing.paper.CubingPaperConfig.knockbackHorizontal);
                    sendValue(sender, "Vertical Knockback", top.speedcubing.paper.CubingPaperConfig.knockbackVertical);
                    sendValue(sender, "Vertical Limit", top.speedcubing.paper.CubingPaperConfig.knockbackVerticalLimit);
                    sendValue(sender, "Horizontal (Extra)", top.speedcubing.paper.CubingPaperConfig.knockbackExtraHorizontal);
                    sendValue(sender, "Vertical (Extra)", top.speedcubing.paper.CubingPaperConfig.knockbackExtraVertical);
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            }
        } catch (RuntimeException ex) {
            sender.sendMessage(ChatColor.RED + ex.getMessage());
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], ARGUMENTS, new ArrayList<>(ARGUMENTS.size()));
        }
        return ImmutableList.of();
    }

    private double parseValue(String[] args) {
        if (args.length != 2)
            throw new RuntimeException("Please specify a single value to set the option to.");

        try {
            return Double.parseDouble(args[1]);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid value specified!");
        }
    }

    private void updated(CommandSender viewer, String name, double oldVal, double newVal) {
        viewer.sendMessage(ChatColor.GREEN + "Updated " + ChatColor.GOLD + name + ChatColor.GREEN + " from " +
                ChatColor.BLUE + oldVal + ChatColor.GREEN + " to " + ChatColor.BLUE + newVal);
    }

    private void sendValue(CommandSender viewer, String name, double value) {
        viewer.sendMessage(ChatColor.AQUA + ChatColor.BOLD.toString() + name + ChatColor.RESET +
                ": " + ChatColor.BLUE + value);
    }
}