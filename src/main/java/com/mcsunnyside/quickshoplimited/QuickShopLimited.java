package com.mcsunnyside.quickshoplimited;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandContainer;
import org.maxgamer.quickshop.event.ShopPurchaseEvent;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;

import java.util.Map;

public final class QuickShopLimited extends JavaPlugin implements Listener {
    public static QuickShopLimited instance;
    private CommandContainer container;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        this.container = CommandContainer.builder()
                .prefix("limit")
                .permission("quickshop.limited")
                .description(getConfig().getString("command-description"))
                .executor(new ShopLimitedCommand())
                .build();
        QuickShop.getInstance().getCommandManager().registerCmd(container);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        QuickShop.getInstance().getCommandManager().unregisterCmd(container);
    }

    @EventHandler(ignoreCancelled = true)
    public void shopPurchase(ShopPurchaseEvent event) {
        Shop shop = event.getShop();
        Map<String, String> storage = shop.getExtra(this);
        if (storage.isEmpty() || storage.get("limit") == null || storage.get("limit").isEmpty() || Integer.parseInt(storage.get("limit")) < 1) {
            return;
        }
        int limit = Integer.parseInt(storage.get("limit"));
        int playerUsedLimit = Integer.parseInt(storage.getOrDefault(event.getPlayer().getUniqueId().toString(), "0"));
        if (playerUsedLimit + event.getAmount() > limit) {
            event.getPlayer().sendMessage(ChatColor.RED + MsgUtil.fillArgs(getConfig().getString("reach-the-limit"), String.valueOf(limit - playerUsedLimit), String.valueOf(event.getAmount())));
            event.setCancelled(true);
            return;
        }
        playerUsedLimit += event.getAmount();
        storage.put(event.getPlayer().getUniqueId().toString(), String.valueOf(playerUsedLimit));
        shop.setExtra(this, storage);
        event.getPlayer().sendTitle(ChatColor.GREEN + getConfig().getString("message.title"), ChatColor.AQUA + MsgUtil.fillArgs(getConfig().getString("message.subtitle", String.valueOf(limit - playerUsedLimit))));
    }
}
