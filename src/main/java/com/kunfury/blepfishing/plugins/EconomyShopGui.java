package com.kunfury.blepfishing.plugins;

import com.kunfury.blepfishing.helpers.ItemHandler;
import com.kunfury.blepfishing.helpers.Utilities;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EconomyShopGui implements Listener {
    private static final String SELL_GUI_HOLDER = "me.gypopo.economyshopgui.objects.SellGUI";

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSellGuiClose(InventoryCloseEvent event){
        if(!(event.getPlayer() instanceof Player player))
            return;

        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if(holder == null || !SELL_GUI_HOLDER.equals(holder.getClass().getName()))
            return;

        List<ItemStack> fishItems = new ArrayList<>();
        for(ItemStack item : inventory.getContents()){
            if(ItemHandler.hasTag(item, ItemHandler.FishIdKey))
                fishItems.add(item);
        }

        if(!fishItems.isEmpty())
            Utilities.SellFishItems(player, fishItems);
    }
}
