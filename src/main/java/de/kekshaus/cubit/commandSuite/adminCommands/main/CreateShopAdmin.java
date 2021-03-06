package de.kekshaus.cubit.commandSuite.adminCommands.main;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.kekshaus.cubit.api.classes.enums.LandTypes;
import de.kekshaus.cubit.api.classes.interfaces.ICommand;
import de.kekshaus.cubit.api.databaseAPI.OfferData;
import de.kekshaus.cubit.plugin.Landplugin;

public class CreateShopAdmin implements ICommand {

	private Landplugin plugin;

	private String permNode;

	public CreateShopAdmin(Landplugin plugin, String permNode) {
		this.plugin = plugin;
		this.permNode = permNode;

	}

	@Override
	public boolean runCmd(final Command cmd, final CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			/* This is not possible from the server console */
			sender.sendMessage(plugin.getYamlManager().getLanguage().noConsoleMode);
			return true;
		}

		/* Build and get all variables */
		Player player = (Player) sender;

		/* Permission Check */
		if (!player.hasPermission(this.permNode)) {
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorNoPermission);
			return true;
		}

		final Location loc = player.getLocation();
		final Chunk chunk = loc.getChunk();
		final String regionName = Landplugin.inst().getRegionManager().buildLandName(LandTypes.SHOP.toString(),
				chunk.getX(), chunk.getZ());

		/*
		 * Check if the player has permissions for this land or hat landadmin
		 * permissions
		 */

		if (!plugin.getRegionManager().isValidRegion(loc.getWorld(), chunk.getX(), chunk.getZ())) {
			if (!plugin.getRegionManager().createRegion(loc, player.getUniqueId(), LandTypes.SHOP)) {
				/* If this task failed! This should never happen */
				sender.sendMessage(
						plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-REGION"));
				plugin.getLogger()
						.warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-REGION"));
				return true;
			}
		} else {
			if (!plugin.getRegionManager().restoreDefaultSettings(
					plugin.getRegionManager().praseRegionData(loc.getWorld(), chunk.getX(), chunk.getZ()),
					loc.getWorld(), null)) {
				/* If this task failed! This should never happen */
				sender.sendMessage(
						plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "RESTORE-REGION"));
				plugin.getLogger().warning(
						plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "RESTORE-REGION"));
				return true;
			}
		}

		if (!plugin.getBlockManager().placeLandBorder(chunk,
				Landplugin.inst().getYamlManager().getSettings().landSellMaterialBorder)) {
			/* If this task failed! This should never happen */
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-BLOCK"));
			plugin.getLogger()
					.warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-BLOCK"));
			return true;
		}

		if (!plugin.getParticleManager().sendBuy(player, loc)) {
			/* If this task failed! This should never happen */
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-PARTICLE"));
			plugin.getLogger()
					.warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-PARTICLE"));
			return true;
		}

		double value = Landplugin.inst().getYamlManager().getSettings().shopBasePrice;
		if (args.length >= 2) {
			if (!NumberUtils.isNumber(args[1])) {
				sender.sendMessage(plugin.getYamlManager().getLanguage().noNumberFound);
				return true;
			}
			value = Double.parseDouble(args[1]);
		}
		OfferData offerData = new OfferData(regionName, loc.getWorld());
		offerData.setValue(value);
		if (!plugin.getDatabaseManager().setOfferData(offerData)) {
			/* If this task failed! This should never happen */
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "OFFER-ADD"));
			plugin.getLogger()
					.warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "OFFER-ADD"));
			return true;
		}

		sender.sendMessage(plugin.getYamlManager().getLanguage().createShopLand.replace("{regionID}", regionName));

		return true;
	}

}
