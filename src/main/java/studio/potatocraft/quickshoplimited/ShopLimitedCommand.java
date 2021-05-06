package studio.potatocraft.quickshoplimited;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.event.CalendarEvent;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.shop.ShopExtraManager;
import org.maxgamer.quickshop.util.MsgUtil;

import java.util.List;
import java.util.Locale;

public class ShopLimitedCommand implements CommandProcesser {
    @Override
    public void onCommand(CommandSender commandSender, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            MsgUtil.sendMessage(commandSender, "Only player can run this command");
            return;
        }
        if (strings.length < 1) {
            MsgUtil.sendMessage(commandSender, ChatColor.RED + MsgUtil.getMessage("wrong-args", commandSender));
            return;
        }
        final BlockIterator bIt = new BlockIterator((Player) commandSender, 10);
        Shop shop = null;

        if (!bIt.hasNext()) {
            MsgUtil.sendMessage(commandSender, MsgUtil.getMessage("not-looking-at-shop", commandSender));
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
            MsgUtil.sendMessage(commandSender, MsgUtil.getMessage("not-looking-at-shop", commandSender));
            return;
        }
        ShopExtraManager manager = shop.getExtraManager(QuickShopLimited.instance);
        switch (strings[0]){
            case "set":
                try {
                    int limitAmount = Integer.parseInt(strings[1]);
                    if (limitAmount > 0) {
                        manager.set("limit", String.valueOf(limitAmount));
                        MsgUtil.sendMessage(commandSender, ChatColor.GREEN + QuickShopLimited.instance.getConfig().getString("success-setup"));
                    } else {
                        manager.set("limit", null);
                        manager.set("data", null);
                        MsgUtil.sendMessage(commandSender, ChatColor.GREEN + QuickShopLimited.instance.getConfig().getString("success-reset"));
                    }
                    manager.save();
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(ChatColor.RED + MsgUtil.getMessage("not-a-integer", commandSender, strings[0]));
                }
                return;
            case "unset":
                manager.set("limit", null);
                manager.set("data", null);
                MsgUtil.sendMessage(commandSender, ChatColor.GREEN + QuickShopLimited.instance.getConfig().getString("success-reset"));
                manager.save();
                return;
            case "reset":
                manager.set("data", null);
                manager.save();
                MsgUtil.sendMessage(commandSender, ChatColor.GREEN + QuickShopLimited.instance.getConfig().getString("success-reset"));
                return;
            case "resetperiod":
                try {
                    CalendarEvent.CalendarTriggerType type = CalendarEvent.CalendarTriggerType.valueOf(strings[1].toUpperCase(Locale.ROOT));
                    manager.set("period", type.name());
                    MsgUtil.sendMessage(commandSender, ChatColor.GREEN + QuickShopLimited.instance.getConfig().getString("success-setup"));
                    manager.save();
                    return;
                }catch (IllegalArgumentException ignored){
                    MsgUtil.sendMessage(commandSender, ChatColor.RED + MsgUtil.getMessage("wrong-args", commandSender));
                    return;
                }
        }

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return null;
    }
}
