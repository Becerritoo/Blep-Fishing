package com.kunfury.blepfishing.ui.panels.player;

import com.kunfury.blepfishing.BlepFishing;
import com.kunfury.blepfishing.config.ConfigHandler;
import com.kunfury.blepfishing.database.Database;
import com.kunfury.blepfishing.helpers.Formatting;
import com.kunfury.blepfishing.helpers.Utilities;
import com.kunfury.blepfishing.ui.MenuHandler;
import com.kunfury.blepfishing.ui.buttons.CustomButton;
import com.kunfury.blepfishing.ui.buttons.admin.AdminPanelButton;
import com.kunfury.blepfishing.ui.buttons.player.ClaimRewardsBtn;
import com.kunfury.blepfishing.ui.buttons.player.SellAllFishBtn;
import com.kunfury.blepfishing.ui.buttons.player.fish.PlayerFishPanelBtn;
import com.kunfury.blepfishing.ui.objects.MenuButton;
import com.kunfury.blepfishing.ui.objects.Panel;
import com.kunfury.blepfishing.ui.buttons.player.tournament.PlayerTournamentPanelBtn;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class PlayerPanel extends Panel {
    public PlayerPanel() {
        super("Player Panel", 27);
        Refresh = true;
    }

    @Override
    public void Show(Player player) {
        // Apply all custom settings locally before showing the panel
        String localizedTitle = ConfigHandler.instance.getLocalizedValue(
                "Text.Gui.PlayerPanel.Title",
                ConfigHandler.instance.guiConfig.getPlayerPanelTitle());
        this.Title = Formatting.ResolveConfigText(localizedTitle);
        this.InventorySize = Utilities.getInventorySize(ConfigHandler.instance.guiConfig.getPlayerPanelSize());

        // --- Custom Show Logic for PlayerPanel ---
        inv = Bukkit.createInventory(player, InventorySize, Title);
        BuildInventory(player);

        fillBackground(); // Use the local filler method

        player.openInventory(inv);
        Panels.put(player.getUniqueId(), this);

        if(Refresh){
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(!player.getOpenInventory().getTitle().equals(Title)){
                        cancel();
                        return;
                    }
                    // Re-apply custom settings on refresh
                    inv = Bukkit.createInventory(player, InventorySize, Title);
                    BuildInventory(player);
                    fillBackground();
                    player.openInventory(inv);
                }
            }.runTaskTimer(BlepFishing.getPlugin(), 0, 20);
        }
    }

    private void fillBackground() {
        String fillerSlotsStr = ConfigHandler.instance.guiConfig.getFillerSlots();
        if (fillerSlotsStr == null || fillerSlotsStr.isEmpty()) return;

        List<Integer> slots = parseSlots(fillerSlotsStr);
        for (int slot : slots) {
            if (slot >= 0 && slot < InventorySize && inv.getItem(slot) == null) {
                inv.setItem(slot, MenuHandler.getBackgroundItem());
            }
        }
    }

    private List<Integer> parseSlots(String slotString) {
        List<Integer> slots = new ArrayList<>();
        if (slotString == null || slotString.isEmpty()) return slots;
        String[] parts = slotString.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.contains("-")) {
                String[] range = part.split("-");
                if (range.length == 2) {
                    try {
                        int start = Integer.parseInt(range[0]);
                        int end = Integer.parseInt(range[1]);
                        for (int i = Math.min(start, end); i <= Math.max(start, end); i++) {
                            slots.add(i);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else {
                try {
                    slots.add(Integer.parseInt(part));
                } catch (NumberFormatException ignored) {}
            }
        }
        return slots;
    }


    @Override
    public void BuildInventory(Player player) {
        ConfigurationSection buttons = ConfigHandler.instance.guiConfig.getPlayerPanelButtons();

        if (buttons == null) return;

        inv.clear();

        for (String key : buttons.getKeys(false)) {
            ConfigurationSection btnConfig = buttons.getConfigurationSection(key);
            if (btnConfig == null) continue;

            String type = btnConfig.getString("Type");
            int slot = btnConfig.getInt("Slot");

            if (type == null) continue;

            MenuButton button = null;
            boolean condition = true;

            switch (type) {
                case "Admin":
                    button = new AdminPanelButton();
                    condition = player.hasPermission("bf.admin");
                    break;
                case "Tournament":
                    button = new PlayerTournamentPanelBtn();
                    condition = ConfigHandler.instance.tourneyConfig.Enabled();
                    break;
                case "Fish":
                    button = new PlayerFishPanelBtn(player);
                    break;
                case "SellAll":
                    button = new SellAllFishBtn(player);
                    break;
                case "ClaimRewards":
                    button = new ClaimRewardsBtn(player);
                    condition = Database.Rewards.HasRewards(player.getUniqueId().toString());
                    break;
                case "Custom":
                    createCustomButton(player, slot, btnConfig, key);
                    break;
            }

            if (button != null && condition) {
                // Set a unique ID for the button based on its config key to avoid collisions
                button.setUniqueId(button.getClass().getName() + "_" + key);

                inv.setItem(slot, getButtonItem(button, btnConfig, player, key));
            }
        }
    }

    private ItemStack getButtonItem(MenuButton button, ConfigurationSection config, Player player, String buttonKey) {
        ItemStack item = button.getItemStack(player); // Get default item

        ConfigurationSection itemConfig = config.getConfigurationSection("Item");
        if (itemConfig == null) {
            // Even if itemConfig is null, we should still check for commands
            if (config.contains("Commands")) {
                List<String> commands = config.getStringList("Commands");
                button.setCommands(commands);
            }
            return item;
        }

        // Change material first
        String materialName = itemConfig.getString("Material");
        if (materialName != null) {
            Material material = Material.getMaterial(materialName);
            if (material != null) item.setType(material);
        }

        // Now get the meta
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String name = ConfigHandler.instance.getLocalizedValue(
                "Text.Gui.PlayerPanel.Buttons." + buttonKey + ".Name",
                itemConfig.getString("Name"));
        if (name != null) meta.setDisplayName(Formatting.ResolveConfigText(name));

        List<String> lore = ConfigHandler.instance.getLocalizedList(
                "Text.Gui.PlayerPanel.Buttons." + buttonKey + ".Lore",
                itemConfig.getStringList("Lore"));
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(Formatting.ResolveConfigLore(lore));
        }

        if (itemConfig.contains("CustomModelData")) {
            meta.setCustomModelData(itemConfig.getInt("CustomModelData"));
        }

        item.setItemMeta(meta);

        // Let's inject the commands into the button if possible.
        if (config.contains("Commands")) {
            List<String> commands = config.getStringList("Commands");
            button.setCommands(commands);
        }

        return item;
    }

    private void createCustomButton(Player player, int slot, ConfigurationSection config, String buttonKey) {
        ConfigurationSection itemConfig = config.getConfigurationSection("Item");
        Material material = Material.STONE;
        String name = null;
        List<String> lore = new ArrayList<>();
        int customModelData = 0;

        if (itemConfig != null) {
            String materialName = itemConfig.getString("Material", "STONE");
            material = Material.getMaterial(materialName);
            if (material == null) material = Material.STONE;
            name = ConfigHandler.instance.getLocalizedValue(
                    "Text.Gui.PlayerPanel.Buttons." + buttonKey + ".Name",
                    itemConfig.getString("Name"));
            lore = ConfigHandler.instance.getLocalizedList(
                    "Text.Gui.PlayerPanel.Buttons." + buttonKey + ".Lore",
                    itemConfig.getStringList("Lore"));
            customModelData = itemConfig.getInt("CustomModelData", 0);
        }

        List<String> commands = config.getStringList("Commands");
        String sound = null;
        float volume = 1.0f;
        float pitch = 1.0f;

        if (config.contains("Sound")) {
            sound = config.getString("Sound.Name");
            volume = (float) config.getDouble("Sound.Volume", 1.0);
            pitch = (float) config.getDouble("Sound.Pitch", 1.0);
        }

        CustomButton btn = new CustomButton(material, name, lore, customModelData, commands, sound, volume, pitch);
        // Ensure custom buttons also have unique IDs based on slot or something to avoid collisions if needed,
        // though CustomButton might not be reused in the same way.
        // We can use the slot as part of the ID since it's unique per panel.
        btn.setUniqueId(CustomButton.class.getName() + "_" + slot);

        inv.setItem(slot, btn.getItemStack(player));
    }
}
