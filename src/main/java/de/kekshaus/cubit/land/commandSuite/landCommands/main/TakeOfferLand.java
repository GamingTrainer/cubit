package de.kekshaus.cubit.land.commandSuite.landCommands.main;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.kekshaus.cubit.land.Landplugin;
import de.kekshaus.cubit.land.api.regionAPI.region.RegionData;
import de.kekshaus.cubit.land.api.sqlAPI.handler.OfferData;
import de.kekshaus.cubit.land.commandSuite.ILandCmd;

public class TakeOfferLand implements ILandCmd {

	private Landplugin plugin;

	private String permNode;

	public TakeOfferLand(Landplugin plugin, String permNode) {
		this.plugin = plugin;
		this.permNode = permNode;
	}

	@Override
	public boolean runCmd(final CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			/* This is not possible from the server console */
			sender.sendMessage(plugin.getLanguageManager().noConsoleMode);
			return true;
		}

		/* Build and get all variables */
		Player player = (Player) sender;

		/* Permission Check */
		if (!player.hasPermission(this.permNode)) {
			sender.sendMessage(plugin.getLanguageManager().errorNoPermission);
			return true;
		}

		final Location loc = player.getLocation();
		final Chunk chunk = loc.getChunk();
		RegionData regionData = plugin.getLandManager().praseRegionData(loc.getWorld(), chunk.getX(), chunk.getZ());

		/*
		 * Check if the player has permissions for this land or hat landadmin
		 * permissions
		 */
		if (!plugin.getLandManager().hasLandPermission(regionData, player.getUniqueId())) {
			sender.sendMessage(plugin.getLanguageManager().errorNoLandPermission.replace("{regionID}",
					regionData.getRegionName()));
			return true;
		}
		if (!plugin.getSqlManager().isOffered(regionData.getRegionName(), loc.getWorld())) {
			sender.sendMessage("Not offered!");
			return true;
		}

		OfferData offerData = plugin.getSqlManager().getOfferData(regionData.getRegionName(), loc.getWorld());
		if (!plugin.getVaultManager().hasEnougToBuy(player.getUniqueId(), offerData.getValue())) {
			sender.sendMessage(plugin.getLanguageManager().notEnoughMoney.replace("{cost}", "" + offerData.getValue()));
		}

		if (!plugin.getLandManager().changeLandOwner(regionData, loc.getWorld(), player.getUniqueId())) {
			/* If this task failed! This should never happen */
			sender.sendMessage(plugin.getLanguageManager().errorInTask.replace("{error}", "TAKEOFFER-UPDATEOWNER"));
			plugin.getLogger()
					.warning(plugin.getLanguageManager().errorInTask.replace("{error}", "TAKEOFFER-UPDATEOWNER"));
			return true;
		}
		if (!plugin.getSqlManager().removeOfferData(regionData.getRegionName(), loc.getWorld())) {
			/* If this task failed! This should never happen */
			sender.sendMessage(plugin.getLanguageManager().errorInTask.replace("{error}", "TAKEOFFER-REMOVEOFFER"));
			plugin.getLogger()
					.warning(plugin.getLanguageManager().errorInTask.replace("{error}", "TAKEOFFER-REMOVEOFFER"));
			return true;
		}
		sender.sendMessage("Du hast das GS abgekauft!");

		return true;
	}

}