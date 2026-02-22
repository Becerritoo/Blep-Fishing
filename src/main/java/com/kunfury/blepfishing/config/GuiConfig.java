package com.kunfury.blepfishing.config;

import com.kunfury.blepfishing.BlepFishing;
import com.kunfury.blepfishing.helpers.Formatting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GuiConfig {

    public FileConfiguration config;
    private File configFile;

    public GuiConfig() {
        configFile = new File(BlepFishing.instance.getDataFolder(), "gui.yml");

        if(!configFile.exists()){
            BlepFishing.getPlugin().saveResource("gui.yml", false);
            configFile = new File(BlepFishing.instance.getDataFolder(), "gui.yml");
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void createDefaultConfig() {
        try {
            configFile.createNewFile();
            FileWriter writer = new FileWriter(configFile);
            writer.write("# Blep Fishing GUI Configuration\n");
            writer.write("#\n");
            writer.write("# PlayerPanel:\n");
            writer.write("#   Title: The title of the GUI.\n");
            writer.write("#   Size: The size of the GUI (must be a multiple of 9).\n");
            writer.write("#   FillerSlots: Slots to fill with the background item.\n");
            writer.write("#   BackgroundItem: Material and ModelData for the background.\n");
            writer.write("#\n");
            writer.write("#   Buttons:\n");
            writer.write("#     You can edit the DisplayName and Lore for each button directly.\n");
            writer.write("#     Placeholders like {amount} will be replaced automatically.\n");
            writer.write("#     To change the icon, uncomment the 'Material' and 'CustomModelData' lines.\n");
            writer.write("#\n");
            writer.write("#     Button Options:\n");
            writer.write("#       Slot: The slot number (0-53).\n");
            writer.write("#       Type: The type of button (Admin, Tournament, Fish, SellAll, ClaimRewards, Custom).\n");
            writer.write("#       Item:\n");
            writer.write("#         Material: The item material (e.g., DIAMOND).\n");
            writer.write("#         Name: The display name of the item.\n");
            writer.write("#         Lore: A list of lore lines.\n");
            writer.write("#         CustomModelData: (Optional) Custom model data for resource packs.\n");
            writer.write("#       Commands: A list of commands to run when clicked.\n");
            writer.write("#         - \"[player] command\" (Runs as player)\n");
            writer.write("#         - \"[console] command\" (Runs as console)\n");
            writer.write("#         - \"[close]\" (Closes the menu)\n");
            writer.write("#         - \"command\" (Runs as console by default)\n");
            writer.write("#         - \"{player}\" is replaced by the player's name.\n");
            writer.write("#       Sound:\n");
            writer.write("#         Name: The sound name (e.g., ENTITY_EXPERIENCE_ORB_PICKUP).\n");
            writer.write("#         Volume: The volume (default 1.0).\n");
            writer.write("#         Pitch: The pitch (default 1.0).\n");
            writer.write("#\n");

            writer.write("PlayerPanel:\n");
            writer.write("  Title: \"&1Blep Fishing\"\n");
            writer.write("  Size: 27\n");
            writer.write("  FillerSlots: \"0-3, 5-8, 9-10, 12, 14, 16-17, 18-21, 23-26\"\n");
            writer.write("  BackgroundItem:\n");
            writer.write("    Material: \"LIGHT_GRAY_STAINED_GLASS_PANE\"\n");
            writer.write("    CustomModelData: 0\n");
            writer.write("  Buttons:\n");

            // Admin Button
            writer.write("    Admin:\n");
            writer.write("      Slot: 4\n");
            writer.write("      Type: Admin\n");
            writer.write("      Item:\n");
            writer.write("        Name: \"" + Formatting.GetLanguageString("UI.Admin.Buttons.panel") + "\"\n");
            writer.write("        # Material: NETHER_STAR\n");
            writer.write("        # CustomModelData: 0\n");
            writer.write("        # Lore:\n");
            writer.write("        #   - \"&7Click to manage plugin\"\n");

            // Tournament Button
            writer.write("    Tournament:\n");
            writer.write("      Slot: 11\n");
            writer.write("      Type: Tournament\n");
            writer.write("      Item:\n");
            writer.write("        Name: \"Fishing Tournaments\"\n");
            writer.write("        # Material: DARK_OAK_HANGING_SIGN\n");
            writer.write("        # CustomModelData: 0\n");
            writer.write("        # Lore:\n");
            writer.write("        #   - \"&7View active tournaments\"\n");

            // Fish Button
            writer.write("    Fish:\n");
            writer.write("      Slot: 13\n");
            writer.write("      Type: Fish\n");
            writer.write("      Item:\n");
            writer.write("        Name: \"" + Formatting.GetLanguageString("UI.Player.Buttons.Base.Fish.name") + "\"\n");
            writer.write("        Lore:\n");
            writer.write("          - \"" + Formatting.GetLanguageString("UI.Player.Buttons.Base.Fish.total") + "\"\n");
            writer.write("        # Material: SALMON\n");
            writer.write("        # CustomModelData: 0\n");

            // Sell All Button
            writer.write("    SellAll:\n");
            writer.write("      Slot: 15\n");
            writer.write("      Type: SellAll\n");
            writer.write("      Item:\n");
            writer.write("        Name: \"" + Formatting.GetLanguageString("UI.Player.Buttons.Base.Sell All.name") + "\"\n");
            writer.write("        Lore:\n");
            writer.write("          - \"" + Formatting.GetLanguageString("UI.Player.Buttons.Base.Sell All.lore") + "\"\n");
            writer.write("        # Material: GOLD_INGOT\n");
            writer.write("        # CustomModelData: 0\n");

            // Claim Rewards Button
            writer.write("    ClaimRewards:\n");
            writer.write("      Slot: 22\n");
            writer.write("      Type: ClaimRewards\n");
            writer.write("      Item:\n");
            writer.write("        Name: \"" + Formatting.GetLanguageString("UI.Player.Buttons.Base.Claim Rewards.name") + "\"\n");
            writer.write("        Lore:\n");
            writer.write("          - \"" + Formatting.GetLanguageString("UI.Player.Buttons.Base.Claim Rewards.lore") + "\"\n");
            writer.write("        # Material: DIAMOND\n");
            writer.write("        # CustomModelData: 0\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerPanelTitle() {
        return config.getString("PlayerPanel.Title", "Blep Fishing");
    }

    public int getPlayerPanelSize() {
        return config.getInt("PlayerPanel.Size", 27);
    }

    public ConfigurationSection getPlayerPanelButtons() {
        return config.getConfigurationSection("PlayerPanel.Buttons");
    }

    public String getFillerSlots() {
        return config.getString("PlayerPanel.FillerSlots", "");
    }

    public ConfigurationSection getBackgroundItemConfig() {
        return config.getConfigurationSection("PlayerPanel.BackgroundItem");
    }

    public void Save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}