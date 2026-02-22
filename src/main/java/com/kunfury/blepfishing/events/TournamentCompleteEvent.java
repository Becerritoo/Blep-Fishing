package com.kunfury.blepfishing.events;

import com.kunfury.blepfishing.objects.FishObject;
import com.kunfury.blepfishing.objects.TournamentObject;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TournamentCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final TournamentObject tournament;
    private final List<FishObject> winningFish;

    public TournamentCompleteEvent(TournamentObject tournament, List<FishObject> winningFish) {
        this.tournament = tournament;
        this.winningFish = winningFish;
    }

    public TournamentObject getTournament() {
        return tournament;
    }

    public List<FishObject> getWinningFish() {
        return winningFish;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
