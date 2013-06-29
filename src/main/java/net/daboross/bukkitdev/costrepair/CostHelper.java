/*
 * Author: Dabo Ross
 * Website: www.daboross.net
 * Email: daboross@daboross.net
 */
package net.daboross.bukkitdev.costrepair;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author daboross
 */
public class CostHelper {

    private final CostRepair main;

    public CostHelper(CostRepair main) {
        this.main = main;
    }

    public int getCost(ItemStack itemStack) {
        int id = itemStack.getTypeId();
        FileConfiguration fc = main.getConfig();
        String name = fc.getString("item-names." + id);
        if (name == null) {
            return -1;
        }
        return fc.getInt("");
    }

    public String getName(ItemStack itemStack) {
        int id = itemStack.getTypeId();
        FileConfiguration fc = main.getConfig();
        String name = fc.getString("item-names." + id);
        return name;
    }

    public String getMoneySymbol() {
        return main.getConfig().getString("money-symbol");
    }
}
