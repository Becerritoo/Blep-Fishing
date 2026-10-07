package com.kunfury.blepfishing.plugins;

import com.kunfury.blepfishing.helpers.ItemHandler;
import com.kunfury.blepfishing.helpers.Utilities;
import com.kunfury.blepfishing.objects.equipment.FishBag;
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
        List<ItemStack> fishBagItems = new ArrayList<>();
        for(int slot = 0; slot < inventory.getSize(); slot++){
            ItemStack item = inventory.getItem(slot);
            if(ItemHandler.hasTag(item, ItemHandler.FishIdKey)){
                fishItems.add(item);
                continue;
            }

            if(FishBag.IsBag(item)){
                fishBagItems.add(item);
                inventory.clear(slot);
            }
        }

        try{
            if(!fishItems.isEmpty() || !fishBagItems.isEmpty())
                Utilities.SellFishItems(player, fishItems, fishBagItems);
        }finally{
            fishBagItems.forEach(item -> player.getInventory().addItem(item).values()
                    .forEach(leftover -> player.getWorld().dropItem(player.getLocation(), leftover)));
        }
    }
}
