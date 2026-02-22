package com.kunfury.blepfishing.ui.buttons.admin.tournamentEdit;

import com.kunfury.blepfishing.config.ConfigHandler;
import com.kunfury.blepfishing.helpers.ItemHandler;
import com.kunfury.blepfishing.objects.FishType;
import com.kunfury.blepfishing.objects.FishingArea;
import com.kunfury.blepfishing.objects.TournamentType;
import com.kunfury.blepfishing.ui.objects.MenuButton;
import com.kunfury.blepfishing.ui.panels.admin.tournaments.AdminTournamentEditFishTypesPanel;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TournamentEditFishAreaChoiceBtn extends MenuButton {

    TournamentType tournamentType;
    FishingArea area;
    int page;

    public TournamentEditFishAreaChoiceBtn(TournamentType tournamentType, FishingArea area, int page) {
        this.tournamentType = tournamentType;
        this.area = area;
        this.page = page;
    }

    @Override
    public ItemStack buildItemStack(Player player) {
        List<String> areaFishIds = FishType.GetAll().stream()
                .filter(f -> f.AreaIds.contains(area.Id))
                .map(f -> f.Id)
                .collect(Collectors.toList());

        boolean allSelected = tournamentType.FishTypeIds.containsAll(areaFishIds);

        Material mat = allSelected ? Material.GREEN_CONCRETE : Material.YELLOW_CONCRETE;
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        assert m != null;

        m.setDisplayName(area.Name);

        ArrayList<String> lore = new ArrayList<>();
        lore.add(allSelected ? ChatColor.GREEN + "All Selected" : ChatColor.YELLOW + "Toggle All");
        lore.add("");
        lore.add(ChatColor.GRAY + "Click to toggle all fish from this area.");
        m.setLore(lore);

        m = setButtonId(m, getId());

        PersistentDataContainer dataContainer = m.getPersistentDataContainer();
        dataContainer.set(ItemHandler.TournamentAreaId, PersistentDataType.STRING, area.Id);
        dataContainer.set(ItemHandler.TourneyTypeId, PersistentDataType.STRING, tournamentType.Id);
        getDataContainer(m).set(pageKey, PersistentDataType.INTEGER, page);

        item.setItemMeta(m);
        return item;
    }

    @Override
    protected void click_left() {
        FishingArea clickedArea = FishingArea.FromId(ItemHandler.getTagString(ClickedItem, ItemHandler.TournamentAreaId));
        if (clickedArea == null) return;

        List<String> areaFishIds = FishType.GetAll().stream()
                .filter(f -> f.AreaIds.contains(clickedArea.Id))
                .map(f -> f.Id)
                .collect(Collectors.toList());

        boolean allSelected = tournamentType.FishTypeIds.containsAll(areaFishIds);

        if (allSelected) {
            tournamentType.FishTypeIds.removeAll(areaFishIds);
        } else {
            for (String fishId : areaFishIds) {
                if (!tournamentType.FishTypeIds.contains(fishId)) {
                    tournamentType.FishTypeIds.add(fishId);
                }
            }
        }

        tournamentType.ResetCatchList();
        ConfigHandler.instance.tourneyConfig.Save();
        new AdminTournamentEditFishTypesPanel(tournamentType, page).Show(player);
    }
}