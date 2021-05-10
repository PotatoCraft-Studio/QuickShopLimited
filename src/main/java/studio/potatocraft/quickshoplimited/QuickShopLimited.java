package studio.potatocraft.quickshoplimited;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.QuickShopAPI;
import org.maxgamer.quickshop.command.CommandContainer;
import org.maxgamer.quickshop.event.CalendarEvent;
import org.maxgamer.quickshop.event.QSReloadEvent;
import org.maxgamer.quickshop.event.ShopPurchaseEvent;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

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

    @EventHandler
    public void onQuickShopReload(QSReloadEvent event){
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
        ConfigurationSection storage = shop.getExtra(this);
        if (storage.getInt("limit") < 1) {
            return;
        }
        int limit = storage.getInt("limit");
        int playerUsedLimit = storage.getInt("data." + event.getPlayer().getUniqueId().toString(), 0);
        if (playerUsedLimit + event.getAmount() > limit) {
            event.getPlayer().sendMessage(ChatColor.RED + MsgUtil.fillArgs(getConfig().getString("reach-the-limit"), String.valueOf(limit - playerUsedLimit), String.valueOf(event.getAmount())));
            event.setCancelled(true);
            return;
        }
        playerUsedLimit += event.getAmount();
        storage.set("data." + event.getPlayer().getUniqueId().toString(), playerUsedLimit);
        shop.setExtra(QuickShopLimited.instance,storage);
        event.getPlayer().sendTitle(ChatColor.GREEN + getConfig().getString("message.title"),
                ChatColor.AQUA + MsgUtil.fillArgs(getConfig().getString("message.subtitle"),String.valueOf(limit - playerUsedLimit)));
    }

    @EventHandler(ignoreCancelled = true)
    public void scheduleEvent(CalendarEvent event) {
        QuickShopAPI.getShopAPI().getAllShops().forEach(shop -> {
            ConfigurationSection manager = shop.getExtra(this);
            int limit = manager.getInt("limit");
            if (limit < 1) {
                return;
            }
            if (StringUtils.isEmpty(manager.getString("period"))) {
                return;
            }
            try {
                if (event.getCalendarTriggerType().ordinal() >= CalendarEvent.CalendarTriggerType.valueOf(manager.getString("period")).ordinal()) {
                    manager.set("data", null);
                    shop.setExtra(QuickShopLimited.instance,manager);
                    Util.debugLog("Limit data has been reset. Shop -> " + shop);
                }
            }catch (IllegalArgumentException ignored){
                Util.debugLog("Limit data failed to reset. Shop -> " + shop+" type "+manager.getString("period")+" not exists.");
                manager.set("period", null);
                shop.setExtra(QuickShopLimited.instance,manager);
            }
        });
    }
}
