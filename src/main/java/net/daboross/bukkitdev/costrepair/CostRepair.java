/*
 * Author: Dabo Ross
 * Website: www.daboross.net
 * Email: daboross@daboross.net
 */
package net.daboross.bukkitdev.costrepair;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
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
    private Economy economyHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        costHelper = new CostHelper(this);
        setupVault(Bukkit.getPluginManager());
    }

    private void setupVault(PluginManager pm) {
        if (pm.isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            economyHandler = rsp.getProvider();
            if (economyHandler == null) {
                getLogger().log(Level.INFO, "Vault found, but Economy handler not found.");
            } else {
                getLogger().log(Level.INFO, "Vault and Economy handler found.");
            }
        } else {
            getLogger().log(Level.INFO, "Vault not found.");
        }
        if (economyHandler == null) {
            pm.disablePlugin(this);
        }
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
                        playersWaitingConfirmation.remove(p.getName());
                        repairItemConfirmed(p);
                    } else {
                        repairItemConfirmationNeeded(p);
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

    private void repairItemConfirmed(Player p) {
        ItemStack itemStack = p.getItemInHand();
        String itemName = costHelper.getName(itemStack);
        int cost = costHelper.getCost(itemStack);
        if (cost <= 0 || itemName == null) {
            p.sendMessage(ERRCOLOR + "The item " + ERRCOLOR2 + (itemName == null ? itemStack.getType().toString() : itemName) + ERRCOLOR + " can not be repaired.");
        } else {
            if (economyHandler.has(p.getName(), cost)) {
                EconomyResponse ecoResponse = economyHandler.withdrawPlayer(p.getName(), cost);
                if (ecoResponse.type == EconomyResponse.ResponseType.SUCCESS) {
                    itemStack.setDurability((short) 1);
                    p.sendMessage(COLOR + "Your " + COLOR2 + itemName + COLOR + " has been repaired at a cost of " + COLOR2 + costHelper.getMoneySymbol() + cost);
                } else if (ecoResponse.type == EconomyResponse.ResponseType.FAILURE) {
                    p.sendMessage(ERRCOLOR + "Could not withdraw " + ERRCOLOR2 + costHelper.getMoneySymbol() + cost + ERRCOLOR + " from your economy account");
                    p.sendMessage(ERRCOLOR + "Returned with error: " + ERRCOLOR2 + ecoResponse.errorMessage);
                } else if (ecoResponse.type == EconomyResponse.ResponseType.NOT_IMPLEMENTED) {
                    p.sendMessage(ERRCOLOR + "Economy does not support withdrawing money.");
                    p.sendMessage(ERRCOLOR + "Could not repair your " + ERRCOLOR2 + itemName);
                }
            } else {
                p.sendMessage(ERRCOLOR + "You do not have enough money to repair " + ERRCOLOR2 + itemName);
                p.sendMessage(ERRCOLOR2 + cost + ERRCOLOR + " is needed to repair " + ERRCOLOR2 + costHelper.getMoneySymbol() + itemName);
            }
        }
    }

    private void repairItemConfirmationNeeded(Player p) {
        ItemStack itemStack = p.getItemInHand();
        String itemName = costHelper.getName(itemStack);
        int cost = costHelper.getCost(itemStack);
        if (cost <= 0 || itemName == null) {
            p.sendMessage(ERRCOLOR + "The item " + ERRCOLOR2 + (itemName == null ? itemStack.getType().toString() : itemName) + ERRCOLOR + " can not be repaired.");
        } else {
            if (economyHandler.has(p.getName(), cost)) {
                p.sendMessage(COLOR + "Reparing your " + COLOR2 + itemName + COLOR + " will cost " + COLOR2 + costHelper.getMoneySymbol() + cost);
                p.sendMessage(COLOR + "Type " + COLOR2 + "/" + CMD_NAME + COLOR + " again in the next 10 seconds to confirm repair.");
                addConfirmationNeededPlayer(p);
            } else {
                p.sendMessage(ERRCOLOR + "You do not have enough money to repair " + ERRCOLOR2 + itemName);
                p.sendMessage(ERRCOLOR2 + costHelper.getMoneySymbol() + cost + ERRCOLOR + " is needed to repair " + ERRCOLOR2 + itemName);
            }
        }
    }

    private void addConfirmationNeededPlayer(Player p) {
        final String name = p.getName();
        playersWaitingConfirmation.add(name);
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                playersWaitingConfirmation.remove(name);
            }
        }, 200);
    }
}
