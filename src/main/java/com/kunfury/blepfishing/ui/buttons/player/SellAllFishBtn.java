package com.kunfury.blepfishing.ui.buttons.player;

import com.kunfury.blepfishing.helpers.Formatting;
import com.kunfury.blepfishing.ui.objects.MenuButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SellAllFishBtn extends MenuButton {

    public SellAllFishBtn(Player player){
        super();
        this.player = player;
    }

    @Override
    public ItemStack buildItemStack(Player player) {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta m = item.getItemMeta();
        assert m != null;

        List<String> lore = new ArrayList<>();
        lore.add(Formatting.GetLanguageString("UI.Player.Buttons.Base.Sell All.lore"));

        m.setLore(lore);
        m.setDisplayName(Formatting.GetLanguageString("UI.Player.Buttons.Base.Sell All.name"));
        item.setItemMeta(m);

        return item;
    }

    @Override
    protected void click_left() {
        player.performCommand("bf sellall");
        player.closeInventory();
    }
}