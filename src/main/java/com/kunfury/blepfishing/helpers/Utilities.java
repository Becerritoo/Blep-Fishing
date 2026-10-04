package com.kunfury.blepfishing.helpers;

import com.kunfury.blepfishing.BlepFishing;
import com.kunfury.blepfishing.config.ConfigHandler;
import com.kunfury.blepfishing.objects.FishObject;
import com.kunfury.blepfishing.objects.TournamentObject;
import com.kunfury.blepfishing.objects.TournamentType;
import com.kunfury.blepfishing.objects.equipment.FishBag;
import com.kunfury.blepfishing.plugins.PluginHandler;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Utilities {

    public static boolean DebugMode = false;
    private static final Map<UUID, PendingFishBagSale> pendingFishBagSales = new HashMap<>();

    private static final class PendingFishBagSale {
        private final int bagId;

        private PendingFishBagSale(int bagId) {
            this.bagId = bagId;
        }
    }

    public static int getFreeSlots(Inventory inventory){
        int freeSlots = 0;
        for (ItemStack it : inventory.getStorageContents()) {
            if (it == null) freeSlots++;
        }
        return freeSlots;
    }

    static boolean running;
    public static void RunTimers(){
        if(!running){
            running = true;

            //Tournament Checker
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(ConfigHandler.instance.tourneyConfig.Enabled()){
                        TournamentType.CheckCanStart();
                        TournamentObject.CheckActive();
                    }
                }

            }.runTaskTimer(BlepFishing.getPlugin(), 0, 1200); //Runs every 60 seconds

        }
    }

    public static int getInventorySize(int baseAmt){

        baseAmt = (((int) Math.ceil(baseAmt / 9.0) ) * 9);

        if(baseAmt > 54)
            baseAmt = 54;

        return baseAmt;
    }

    public static long TimeToLong(LocalDateTime time){
        return time.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static LocalDateTime TimeFromLong(long milli){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(milli), ZoneOffset.UTC);
    }

    public static void Severe(String message){
        Bukkit.getLogger().severe("BlepFishing: " + message);
    }

    public static boolean GiveItem(Player player, ItemStack item, boolean drop){
        if(player == null || !player.isOnline()){
            return false;
        }
        for(var badItem : player.getInventory().addItem(item).values()){
            if(drop){
                player.getWorld().dropItem(player.getLocation(), badItem);
                return true;
            }
            else
                return false;
        }
        return true;
    }

    public static void Announce(String message){
        for(var p : Bukkit.getOnlinePlayers()){
            p.sendMessage(message);
        }
    }

    public static void AnnounceNether(String message){
        for(var p : Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().getName().contains("_nether"))
                .toList()){
            p.sendMessage(message);
        }
    }

    public static void AnnounceEnd(String message){
        for(var p : Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().getName().contains("_the_end"))
                .toList()){
            p.sendMessage(message);
        }
    }

    public static void Announce(TextComponent textComponent){
        for(var p : Bukkit.getOnlinePlayers()){
            p.spigot().sendMessage(textComponent);
        }
    }

    public static void SendPlayerMessage(Player player, String message) {
        player.sendMessage(Formatting.GetMessagePrefix() + message);
    }

    public static void SendPlayerMessage(CommandSender sender, String message) {
        sender.sendMessage(Formatting.GetMessagePrefix() + message);
    }

    public static void SellAllFish(Player player) {
        if(!BlepFishing.hasEconomy())
            return;

        List<FishObject> fishList = new ArrayList<>();
        List<ItemStack> fishItems = new ArrayList<>();

        for(var i : player.getInventory().getContents()){
            if(!ItemHandler.hasTag(i, ItemHandler.FishIdKey))
                continue;

            FishObject fish = FishObject.GetFromItem(i);
            if(fish == null){
                Severe("Tried to sell invalid fish");
                continue;
            }
            fishList.add(fish);
            fishItems.add(i);

        }

        if(fishList.isEmpty()){
            player.sendMessage(Formatting.GetFormattedMessage("System.noFish"));
            return;
        }

        if(!SellFishList(player, fishList))
            return;

        fishItems.forEach(i -> i.setAmount(0));

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, .3f, 1f);
    }

    public static void SellFish(Player player) {
        if(!BlepFishing.hasEconomy())
            return;

        ItemStack sellItem = player.getInventory().getItemInMainHand();

        FishBag fishBag = FishBag.GetBag(sellItem);
        if(fishBag != null){
            SellFishBag(player, fishBag);
            return;
        }

        FishObject fish = FishObject.GetFromItem(sellItem);

        if(fish == null){
            Utilities.SendPlayerMessage(player, Formatting.GetLanguageString("System.noFish"));
            return;
        }
        if(!DepositFishSale(player, fish.Value))
            return;

        sellItem.setAmount(0);

        player.sendMessage(Formatting.GetMessagePrefix() +
                Formatting.GetLanguageString("Economy.soldFish")
                        .replace("{fish}", fish.getFormattedName())
                        .replace("{value}", Formatting.DoubleFormat(fish.Value)));

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, .3f, 1f);
    }

    private static boolean SellFishList(Player player, List<FishObject> fishList){
        if(!BlepFishing.hasEconomy())
            return false;

        double totalValue = 0;

        for(var fish : fishList)
            totalValue += fish.Value;


        if(!DepositFishSale(player, totalValue))
            return false;

        player.sendMessage(Formatting.GetFormattedMessage("Economy.soldAllFish")
                        .replace("{amount}", String.valueOf(fishList.size()))
                        .replace("{value}", Formatting.DoubleFormat(totalValue)));
        return true;
    }

    private static boolean DepositFishSale(Player player, double amount){
        try {
            EconomyResponse response = BlepFishing.getEconomy().depositPlayer(player, amount);
            if(response.transactionSuccess())
                return true;

            Severe("Unable to deposit fish sale: " + response.errorMessage);
        } catch (RuntimeException ex) {
            Severe("Unable to deposit fish sale: " + ex.getMessage());
        }

        player.sendMessage(Formatting.GetFormattedMessage("Economy.saleFailed"));
        return false;
    }

    public static void SellFishBag(Player player, FishBag fishBag){
        if(!BlepFishing.hasEconomy())
            return;

        UUID playerId = player.getUniqueId();
        PendingFishBagSale pendingSale = pendingFishBagSales.get(playerId);
        if(pendingSale == null || pendingSale.bagId != fishBag.Id){
            PendingFishBagSale newPendingSale = new PendingFishBagSale(fishBag.Id);
            pendingFishBagSales.put(playerId, newPendingSale);

            player.sendMessage(Formatting.GetFormattedMessage("Economy.sellBagConfirm"));

            Bukkit.getScheduler().runTaskLater(BlepFishing.getPlugin(),
                    () -> pendingFishBagSales.remove(playerId, newPendingSale), 300);
            return;
        }

        pendingFishBagSales.remove(playerId, pendingSale);

        var fishList = fishBag.getFish();
        if(fishList.isEmpty()){
            player.sendMessage(Formatting.GetFormattedMessage("System.noFish"));
            return;
        }

        if(!SellFishList(player, fishList))
            return;

        fishList.forEach(f -> f.setFishBagId(null));
        fishBag.RequestUpdate();
        fishBag.UpdateBagItem();
    }
}
