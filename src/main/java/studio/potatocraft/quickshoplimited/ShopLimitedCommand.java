package studio.potatocraft.quickshoplimited;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.QuickShopAPI;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.api.event.CalendarEvent;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;

import java.util.Locale;

public class ShopLimitedCommand implements CommandHandler<Player> {
    private QuickShopAPI api;
    public ShopLimitedCommand(QuickShopAPI api){
        this.api = api;
    }
    @Override
    public void onCommand(Player commandSender, String s, String[] strings) {
        if (strings.length < 1) {
            api.getTextManager().of(commandSender,"command.wrong-args").send();
            return;
        }
        final BlockIterator bIt = new BlockIterator(commandSender, 10);
        Shop shop = null;

        if (!bIt.hasNext()) {
            api.getTextManager().of(commandSender,"not-looking-at-shop").send();
            return;
        }
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop searching = QuickShop.getInstance().getShopManager().getShop(b.getLocation());
            if (searching == null) {
                continue;
            }
            shop = searching;
            break;
        }
        if (shop == null) {
            api.getTextManager().of(commandSender,"not-looking-at-shop").send();
            return;
        }
        ConfigurationSection manager = shop.getExtra(QuickShopLimited.instance);
        switch (strings[0]){
            case "set":
                try {
                    int limitAmount = Integer.parseInt(strings[1]);
                    if (limitAmount > 0) {
                        manager.set("limit", limitAmount);
                        MsgUtil.sendDirectMessage(commandSender,ChatColor.GREEN + QuickShopLimited.instance.getConfig().getString("success-setup"));
                    } else {
                        manager.set("limit", null);
                        manager.set("data", null);
                        MsgUtil.sendDirectMessage(commandSender,ChatColor.GREEN + QuickShopLimited.instance.getConfig().getString("success-reset"));
                    }
                    shop.setExtra(QuickShopLimited.instance,manager);
                } catch (NumberFormatException e) {
                    api.getTextManager().of(commandSender,"not-a-integer",strings[1]).send();
                }
                return;
            case "unset":
                manager.set("limit", null);
                manager.set("data", null);
                MsgUtil.sendDirectMessage(commandSender,ChatColor.GREEN + QuickShopLimited.instance.getConfig().getString("success-reset"));
                shop.setExtra(QuickShopLimited.instance,manager);
                return;
            case "reset":
                manager.set("data", null);
                shop.setExtra(QuickShopLimited.instance,manager);
                MsgUtil.sendDirectMessage(commandSender,ChatColor.GREEN + QuickShopLimited.instance.getConfig().getString("success-reset"));
                return;
            case "period":
                try {
                    CalendarEvent.CalendarTriggerType type = CalendarEvent.CalendarTriggerType.valueOf(strings[1].toUpperCase(Locale.ROOT));
                    manager.set("period", type.name());
                    MsgUtil.sendDirectMessage(commandSender,ChatColor.GREEN + QuickShopLimited.instance.getConfig().getString("success-setup"));
                    shop.setExtra(QuickShopLimited.instance,manager);
                }catch (IllegalArgumentException ignored){
                    api.getTextManager().of(commandSender,"command.wrong-args",strings[1]).send();
                }
                return;
        }

    }
}
