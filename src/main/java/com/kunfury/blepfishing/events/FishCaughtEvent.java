package com.kunfury.blepfishing.events;

import com.kunfury.blepfishing.objects.FishObject;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FishCaughtEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final FishObject fish;
    private final Player player;

    public FishCaughtEvent(FishObject fish, Player player) {
        this.fish = fish;
        this.player = player;
    }

    public FishObject getFish() {
        return fish;
    }

    public Player getPlayer() {
        return player;
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