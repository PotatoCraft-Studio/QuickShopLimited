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

import java.util.Map;

public final class QuickShopLimited extends JavaPlugin implements Listener {
    public static QuickShopLimited instance;
    private CommandContainer container;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        this.container = CommandContainer.builder()
                .prefix("limit")
                .permission("quickshop.limited")
                .description("设置每个玩家在此商店最大购买物品数量上限")
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
            event.getPlayer().sendMessage(ChatColor.RED + "您在此商店的购买数量已达上限，您还能购买：" + (limit - playerUsedLimit) + ", 您正在尝试购买：" + event.getAmount());
            event.setCancelled(true);
            return;
        }
        playerUsedLimit += event.getAmount();
        storage.put(event.getPlayer().getUniqueId().toString(), String.valueOf(playerUsedLimit));
        shop.setExtra(this,storage);
        event.getPlayer().sendTitle(ChatColor.GREEN + "购买成功", ChatColor.GOLD + "在此商店还可购买: " + ChatColor.AQUA + (limit - playerUsedLimit) + "件物品");
    }
}
