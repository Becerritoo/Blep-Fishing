package com.kunfury.blepfishing.ui;

import com.kunfury.blepfishing.config.ConfigHandler;
import com.kunfury.blepfishing.helpers.ItemHandler;
import com.kunfury.blepfishing.ui.objects.MenuButton;
import com.kunfury.blepfishing.ui.objects.Panel;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;

public class MenuHandler {
    private static ItemStack backgroundItem;

    public static HashMap<String, MenuButton> MenuButtons = new HashMap<>();
    public static HashMap<String, Panel> Panels = new HashMap<>();

    public static void SetupButton(MenuButton btn){
        var btnId = btn.getId();

        MenuButtons.put(btnId, btn);
    }

    public static void SetupPanel(Panel panel){
        var panelId = panel.getId();
        if(Panels.containsKey(panelId))
            return;

        Panels.put(panelId, panel);
    }

    public static void reload() {
        backgroundItem = null; // Reset to force recreation with new config values
    }

    public static ItemStack getBackgroundItem(){
        if(backgroundItem == null){
            ConfigurationSection config = ConfigHandler.instance.guiConfig.getBackgroundItemConfig();
            Material material = Material.LIGHT_GRAY_STAINED_GLASS_PANE;
            int customModelData = 0;

            if (config != null) {
                material = Material.getMaterial(config.getString("Material", "LIGHT_GRAY_STAINED_GLASS_PANE"));
                if (material == null) material = Material.LIGHT_GRAY_STAINED_GLASS_PANE;
                customModelData = config.getInt("CustomModelData", 0);
            }

            backgroundItem = new ItemStack(material, 1);
            ItemMeta meta = backgroundItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(" ");
                if (customModelData != 0) {
                    meta.setCustomModelData(customModelData);
                }
                meta.getPersistentDataContainer().set(ItemHandler.ButtonIdKey, PersistentDataType.STRING, "_background");
                backgroundItem.setItemMeta(meta);
            }
        }
        return backgroundItem;
    }
}