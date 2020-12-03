package com.mcsunnyside.quickshoplimited;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandContainer;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;

import java.util.Map;

public class ShopLimitedCommand implements CommandProcesser {
    @Override
    public void onCommand(CommandSender commandSender, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            MsgUtil.sendMessage(commandSender, "Only player can run this command");
            return;
        }
        if (strings.length < 1) {
            MsgUtil.sendMessage(commandSender, ChatColor.RED+MsgUtil.getMessage("wrong-args",commandSender));
            return;
        }
        try{
            int limitAmount = Integer.parseInt(strings[0]);

            final BlockIterator bIt = new BlockIterator((Player) commandSender, 10);

            if (!bIt.hasNext()) {
                MsgUtil.sendMessage( commandSender, MsgUtil.getMessage("not-looking-at-shop",  commandSender));
                return;
            }

            while (bIt.hasNext()) {
                final Block b = bIt.next();
                final Shop shop = QuickShop.getInstance().getShopManager().getShop(b.getLocation());

                if (shop == null) {
                    continue;
                }

                Map<String ,String > map = shop.getExtra(QuickShopLimited.instance);
                if(limitAmount > 0) {
                    map.put("limit", String.valueOf(limitAmount));
                    MsgUtil.sendMessage(commandSender,ChatColor.GREEN+QuickShopLimited.instance.getConfig().getString("success-setup"));
                }else{
                    map.clear();
                    MsgUtil.sendMessage(commandSender,ChatColor.GREEN+QuickShopLimited.instance.getConfig().getString("success-reset"));
                }
                shop.setExtra(QuickShopLimited.instance,map);

                MsgUtil.sendMessage(commandSender,ChatColor.GREEN+QuickShopLimited.instance.getConfig().getString("success-setup"));

                return;
            }

            MsgUtil.sendMessage(commandSender, MsgUtil.getMessage("not-looking-at-shop", commandSender));


        }catch (NumberFormatException e){
            commandSender.sendMessage(ChatColor.RED+MsgUtil.getMessage("not-a-integer",commandSender,strings[0]));
        }
    }
}
