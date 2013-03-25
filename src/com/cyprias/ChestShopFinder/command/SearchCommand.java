package com.cyprias.ChestShopFinder.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.cyprias.ChestShopFinder.ChatUtils;
import com.cyprias.ChestShopFinder.Logger;
import com.cyprias.ChestShopFinder.Perm;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.database.Shop;
import com.cyprias.ChestShopFinder.utils.MathUtil;

public class SearchCommand implements Command {
	public static HashMap<String, List<Shop>> previousResults = new HashMap<String, List<Shop>>();
	
	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, String[] args) throws SQLException {
		
		if (args.length < 1 || args.length > 2){
			getCommands(sender, cmd);
			return true;
		}
		
		final ItemStack stock = Plugin.getItemStack(args[0]);
		if (stock == null || stock.getTypeId() == 0) {
			ChatUtils.error(sender, "Unknown item: " + args[0]);
			return true;
		}
		
		final Player p = (Player) sender;
		final double pX = p.getLocation().getX();
		final double pZ = p.getLocation().getZ();
		
		
		
		Plugin.getInstance().getServer().getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
			public void run() {
				List<Shop> shops = null;
				try {
					shops = Plugin.database.findItemNearby(stock, p.getLocation());
				} catch (SQLException e) {
					ChatUtils.error(sender, "Exception caught while executing this command.");
					e.printStackTrace();
					return;
				}
				if (shops == null || shops.size() == 0){
					ChatUtils.send(sender, "No shops found with that item.");
					return;
				}
				previousResults.put(sender.getName(), shops);
				// [1] Cyprias has 16 for $2 22 blocks north of you.
				String shopFormat = "�7[�f%s�7] �f%s �7has �f%s �7for $�f%s�7, �f%s �7blocks �f%s";
				String sDir;
				
				Shop shop;
				for (int i=0; i<shops.size(); i++){
					shop = shops.get(i);
					
					sDir = MathUtil.DegToDirection(MathUtil.AngleCoordsToCoords(pX, pZ, shop.location.getBlockX(), shop.location.getBlockZ()));

					ChatUtils.send(sender, String.format(shopFormat, i+1, shop.owner, shop.amount, shop.buyPrice, Plugin.Round(p.getLocation().distance(shop.location)), sDir));

				}
				
			}
		});
		
		return true;
	}
	
	public CommandAccess getAccess() {
		return CommandAccess.PLAYER;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.SEARCH, "/%s search", cmd);
	}

	public boolean hasValues() {
		return false;
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if (Plugin.hasPermission(sender, Perm.SEARCH))
			list.add("/%s serach");
	}
}
