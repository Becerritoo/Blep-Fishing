package com.kunfury.blepfishing.ui.panels.admin.tournaments;

import com.kunfury.blepfishing.objects.FishingArea;
import com.kunfury.blepfishing.ui.buttons.admin.areas.AdminAreaBtn;
import com.kunfury.blepfishing.ui.buttons.admin.tournamentEdit.TournamentEditFishAreaChoiceBtn;
import com.kunfury.blepfishing.ui.objects.MenuButton;
import com.kunfury.blepfishing.ui.objects.Panel;
import com.kunfury.blepfishing.ui.buttons.admin.tournamentEdit.AdminTournamentButton;
import com.kunfury.blepfishing.ui.buttons.admin.tournamentEdit.TournamentEditFishTypeChoiceBtn;
import com.kunfury.blepfishing.objects.FishType;
import com.kunfury.blepfishing.objects.TournamentType;
import com.kunfury.blepfishing.ui.objects.panels.PaginationPanel;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AdminTournamentEditFishTypesPanel extends PaginationPanel<Object> {

    TournamentType type;
    public AdminTournamentEditFishTypesPanel(TournamentType type, int page){
        super(type.Name + " Fish Types", 54, page, new AdminTournamentButton(type));
        this.type = type;
    }


    @Override
    protected List<Object> loadContents() {
        List<Object> contents = new ArrayList<>();

        List<FishingArea> areas = new ArrayList<>(FishingArea.GetAll());
        areas.sort(Comparator.comparing(a -> a.Name));
        contents.addAll(areas);

        List<FishType> fishTypes = new ArrayList<>(FishType.GetAll());
        fishTypes.sort(Comparator.comparing(f -> f.Name));
        contents.addAll(fishTypes);

        return contents;
    }

    @Override
    protected MenuButton getButton(Object object, Player player) {
        if (object instanceof FishType) {
            return new TournamentEditFishTypeChoiceBtn(type, (FishType) object, Page);
        }
        if (object instanceof FishingArea) {
            return new TournamentEditFishAreaChoiceBtn(type, (FishingArea) object, Page);
        }
        return null; // Should not happen
    }
}
