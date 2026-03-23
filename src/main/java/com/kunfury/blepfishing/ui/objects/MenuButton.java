package com.kunfury.blepfishing.ui.objects;

import com.kunfury.blepfishing.BlepFishing;
import com.kunfury.blepfishing.helpers.Formatting;
import com.kunfury.blepfishing.helpers.Utilities;
import com.kunfury.blepfishing.helpers.ItemHandler;
import com.kunfury.blepfishing.objects.FishType;
import com.kunfury.blepfishing.objects.TournamentType;
import com.kunfury.blepfishing.ui.MenuHandler;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class MenuButton {

    protected static final NamespacedKey dayKey = new NamespacedKey(BlepFishing.getPlugin(), "blep.tournamentEdit.day");
    protected static final NamespacedKey timeKey = new NamespacedKey(BlepFishing.getPlugin(), "blep.tournamentEdit.time");
    protected static final NamespacedKey panelKey = new NamespacedKey(BlepFishing.getPlugin(), "blep.panel");
    protected static final NamespacedKey pageKey = new NamespacedKey(BlepFishing.getPlugin(), "blep.page");

    protected ItemStack ClickedItem;
    protected Player player;
    private PersistentDataContainer dataContainer;
    private List<String> commands;
    private String uniqueId;

    public String getId(){
        if (uniqueId != null) return uniqueId;
        return getClass().getName();
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
        MenuHandler.SetupButton(this);
    }

    public MenuButton(){
        MenuHandler.SetupButton(this);
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    protected void setButtonTitle(ItemMeta m, String title){
        m.setDisplayName(Formatting.LocalizeLiteralText(ChatColor.AQUA + title));
    }


    public void perform(InventoryClickEvent e){
        ClickedItem = e.getCurrentItem();
        player = (Player) e.getWhoClicked();
        ClickType clickType = e.getClick();

        if (commands != null && !commands.isEmpty()) {
            for (String command : commands) {
                String cmd = command.replace("{player}", player.getName()).replace("%player_name%", player.getName());
                if (cmd.equalsIgnoreCase("[close]")) {
                    player.closeInventory();
                } else if (cmd.toLowerCase().startsWith("[player] ")) {
                    player.performCommand(cmd.substring(9));
                } else if (cmd.toLowerCase().startsWith("[console] ")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.substring(10));
                } else if (cmd.toLowerCase().startsWith("[message] ")) {
                    player.sendMessage(Formatting.formatColor(cmd.substring(10)));
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        }

        if(clickType.isLeftClick()){
            if(clickType.isShiftClick()){
                click_left_shift();
            }else{
                click_left();
            }
        }
        if(clickType.isRightClick()){
            if(clickType.isShiftClick()){
                click_right_shift();
            }else{
                click_right();
            }
        }
    }

    public String getPermission(){
        return null;
    }

    public ItemStack getItemStack(Player player){
        ItemStack item = buildItemStack(player);
        ItemMeta m = item.getItemMeta();
        assert m != null;
        m = setButtonId(m, getId());

        if (m.hasDisplayName()) {
            m.setDisplayName(Formatting.LocalizeLiteralText(m.getDisplayName()));
        }
        if (m.hasLore() && m.getLore() != null) {
            List<String> localizedLore = new ArrayList<>();
            for (String loreLine : m.getLore()) {
                localizedLore.add(Formatting.LocalizeLiteralText(loreLine));
            }
            m.setLore(localizedLore);
        }


        item.setItemMeta(m);

        return item;
    }

    protected abstract ItemStack buildItemStack(Player player);

    public ItemStack getCustomItemStack(String name, List<String> lore, Material material, Player player){
        ItemStack item = getItemStack(player);

        if(material != null)
            item.setType(material);

        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        if(name != null)
            meta.setDisplayName(Formatting.LocalizeLiteralText(name));

        List<String> itemLore = new ArrayList<>();

        if(lore != null)
            for (String loreLine : lore) {
                itemLore.add(Formatting.LocalizeLiteralText(loreLine));
            }

        if(Utilities.DebugMode){
            itemLore.add("");
            itemLore.add(getId());
        }



        meta.setLore(itemLore);


        item.setItemMeta(meta);

        return item;
    }

    public ItemStack getBackButton(Player player){
        return getCustomItemStack(Formatting.GetLanguageString("UI.System.Buttons.goBack"), new ArrayList<>(), Material.REDSTONE, player);
    }

    protected void click_left(){

    }

    protected  void click_right(){
        click_left();
    }

    protected void click_left_shift(){
        click_left();
    }

    protected void click_right_shift(){
        click_right();
    }

    protected ItemMeta setButtonId(ItemMeta buttonMeta, String buttonId){
        buttonMeta.getPersistentDataContainer().set(ItemHandler.ButtonIdKey, PersistentDataType.STRING, buttonId);
        return buttonMeta;
    }

    protected PersistentDataContainer getDataContainer(ItemMeta meta){
        if(dataContainer == null)
            dataContainer = meta.getPersistentDataContainer();
        return dataContainer;
    }

    protected FishType getFishType(){
        String typeId = ItemHandler.getTagString(ClickedItem, ItemHandler.FishTypeId);
        return FishType.FromId(typeId);
    }


    protected TournamentType getTournamentType(){
        String typeId = ItemHandler.getTagString(ClickedItem, ItemHandler.TourneyTypeId);
        return TournamentType.FromId(typeId);
    }

    protected int getPage(){
        return ItemHandler.getTagInt(ClickedItem, pageKey);
    }
    protected Panel getPanel(){
        String panelId = ItemHandler.getTagString(ClickedItem, panelKey);
        for(Panel panel : MenuHandler.Panels.values()){
            if(panel.getId().equals(panelId)){
                return panel;
            }
        }
        return null;
    }

    private static final HashMap<UUID, Conversation> Conversations = new HashMap<>();

    public static void CancelConversation(Player player){
        var playerId = player.getUniqueId();


        if(!Conversations.containsKey(playerId)){
            player.sendMessage(Formatting.GetFormattedMessage("Messages.Errors.noValidConversation"));
            return;
        }


        var convo = Conversations.get(playerId);
        Conversations.remove(playerId);
        convo.abandon();
//        player.sendMessage( Formatting.GetMessagePrefix() + "Active Conversation Cancelled");
        Panel.OpenLast(player);
    }

    private ConversationFactory getFactory(Prompt prompt){
        return new ConversationFactory(BlepFishing.getPlugin())
                .withFirstPrompt(getLocalizedPrompt(prompt))
                .withModality(true)
                .withTimeout(60)
                .withEscapeSequence("cancel")
                .thatExcludesNonPlayersWithMessage(Formatting.GetFormattedMessage("Messages.Errors.playerOnlyConversationFactory"));
    }

    protected Conversation getConversation(Player player, Prompt prompt){
        TextComponent message = new TextComponent(Formatting.GetMessagePrefix() +  ChatColor.RED + Formatting.GetLanguageString("UI.System.Conversation.cancelButton"));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bf CancelConversation"));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(Formatting.GetLanguageString("UI.System.Conversation.cancelHover"))));

        player.spigot().sendMessage(message);

        Conversation convo = getFactory(prompt).buildConversation(player);
        Conversations.put(player.getUniqueId(), convo);
        return convo;
    }

    private Prompt getLocalizedPrompt(Prompt prompt) {
        if (prompt == null || prompt == Prompt.END_OF_CONVERSATION) {
            return prompt;
        }

        return new Prompt() {
            @Override
            public String getPromptText(ConversationContext context) {
                return Formatting.LocalizeLiteralText(prompt.getPromptText(context));
            }

            @Override
            public boolean blocksForInput(ConversationContext context) {
                return prompt.blocksForInput(context);
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                Prompt next = prompt.acceptInput(context, input);
                if (next == Prompt.END_OF_CONVERSATION || next == null) {
                    return next;
                }
                return getLocalizedPrompt(next);
            }
        };
    }
}
