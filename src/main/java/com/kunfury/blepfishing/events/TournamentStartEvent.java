package com.kunfury.blepfishing.events;

import com.kunfury.blepfishing.objects.TournamentObject;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TournamentStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final TournamentObject tournament;

    public TournamentStartEvent(TournamentObject tournament) {
        this.tournament = tournament;
    }

    public TournamentObject getTournament() {
        return tournament;
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