/*
 * Author: Dabo Ross
 * Website: www.daboross.net
 * Email: daboross@daboross.net
 */
package net.daboross.bukkitdev.costrepair;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author daboross
 */
public class CostRepair extends JavaPlugin {

    private static final String CMD_NAME = "crepair";
    private static final String PERMISSION = "costrepair.use";
    private static final String COLOR = ChatColor.GREEN.toString();
    private static final String COLOR2 = ChatColor.DARK_GREEN.toString();
    private static final String ERRCOLOR = ChatColor.RED.toString();
    private static final String ERRCOLOR2 = ChatColor.DARK_RED.toString();
    private final Set<String> playersWaitingConfirmation = new HashSet<String>();
    private CostHelper costHelper;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        costHelper = new CostHelper(this);
    }

    @Override
    public void onDisable() {

        playersWaitingConfirmation.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase(CMD_NAME)) {
            if (sender.hasPermission(PERMISSION)) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    if (playersWaitingConfirmation.contains(p.getName())) {
                        repairItem(p);
                    }
                } else {
                    sender.sendMessage(ERRCOLOR + "You must be a player to use " + ERRCOLOR2 + "/" + label);
                }
            } else {
                sender.sendMessage(ERRCOLOR + "You don't have permission to use " + ERRCOLOR2 + "/" + label);
            }
        } else {
            sender.sendMessage(COLOR + "Command '" + COLOR2 + command.getName() + COLOR + "' unknown to CostRepair");
        }
        return true;
    }

    private void repairItem(Player p) {
        ItemStack itemStack = p.getItemInHand();
        int cost = costHelper.getCost(itemStack);
        if (cost <= 0) {
            p.sendMessage(ERRCOLOR + "The item " + ERRCOLOR2 + itemStack.getType().toString() + ERRCOLOR + " cannot be repaired.");
            return;
        }
        
    }
}
