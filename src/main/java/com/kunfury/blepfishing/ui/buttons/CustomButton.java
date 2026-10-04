package com.kunfury.blepfishing.ui.buttons;

import com.kunfury.blepfishing.helpers.Formatting;
import com.kunfury.blepfishing.ui.objects.MenuButton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CustomButton extends MenuButton {

    private final String sound;
    private final float volume;
    private final float pitch;

    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int customModelData;

    public CustomButton(Material material, String name, List<String> lore, int customModelData, List<String> commands, String sound, float volume, float pitch) {
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.customModelData = customModelData;

        setCommands(commands);
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    protected ItemStack buildItemStack(Player player) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(Formatting.ResolveConfigText(name));
            meta.setLore(Formatting.ResolveConfigLore(lore));

            if (customModelData != 0) {
                meta.setCustomModelData(customModelData);
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void perform(InventoryClickEvent e) {
        super.perform(e);
        Player player = (Player) e.getWhoClicked();

        if (sound != null && !sound.isEmpty()) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(sound), volume, pitch);
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().warning("[BlepFishing] Invalid sound in gui.yml: " + sound);
            }
        }
    }
}
