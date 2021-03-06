package de.kekshaus.cubit.commandSuite.landCommands.main;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.kekshaus.cubit.api.classes.enums.LandTypes;
import de.kekshaus.cubit.api.classes.interfaces.ICommand;
import de.kekshaus.cubit.api.regionAPI.region.RegionData;
import de.kekshaus.cubit.plugin.Landplugin;

public class SellLand implements ICommand {

	private Landplugin plugin;
	private String permNode;
	private boolean isAdmin;

	public SellLand(Landplugin plugin, String permNode, boolean isAdmin) {
		this.plugin = plugin;
		this.permNode = permNode;

		this.isAdmin = isAdmin;

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

		/* Check if this is a valid sellTask */
		if (!plugin.getRegionManager().isValidRegion(loc.getWorld(), chunk.getX(), chunk.getZ())) {
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorNoLandFound);
			return true;
		}

		RegionData regionData = plugin.getRegionManager().praseRegionData(loc.getWorld(), chunk.getX(), chunk.getZ());
		final String regionID = regionData.getRegionName();

		if (regionData.getLandType() != LandTypes.WORLD) {
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorNoValidLandFound.replace("{type}",
					LandTypes.WORLD.toString()));
			return true;
		}

		if (!plugin.getRegionManager().hasLandPermission(regionData, player.getUniqueId()) && this.isAdmin == false) {
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorNoLandPermission.replace("{regionID}",
					regionData.getRegionName()));
			return true;
		}

		double economyValue = plugin.getVaultManager().calculateLandCost(player.getUniqueId(), loc.getWorld(), false);

		if (!plugin.getVaultManager().transferMoney(null, regionData.getOwnersUUID()[0], economyValue)) {
			/* If this task failed! This should never happen */
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-ECONOMY"));
			plugin.getLogger()
					.warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-ECONOMY"));
			return true;
		}

		if (!plugin.getRegionManager().removeLand(regionData, loc.getWorld())) {
			/* If this task failed! This should never happen */
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "DELETE-REGION"));
			plugin.getLogger()
					.warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "DELETE-REGION"));
			return true;
		}

		if (!plugin.getBlockManager().placeLandBorder(chunk,
				Landplugin.inst().getYamlManager().getSettings().landSellMaterialBorder)) {
			/* If this task failed! This should never happen */
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-BLOCK"));
			plugin.getLogger()
					.warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-BLOCK"));
			return true;
		}
		if (!plugin.getDatabaseManager().removeOfferData(regionData.getRegionName(), loc.getWorld())) {
			/* If this task failed! This should never happen */
			sender.sendMessage(
					plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "SELL-REMOVEOFFER"));
			plugin.getLogger()
					.warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "SELL-REMOVEOFFER"));
			return true;
		}

		if (!plugin.getParticleManager().sendSell(player, loc)) {
			/* If this task failed! This should never happen */
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-PARTICLE"));
			plugin.getLogger()
					.warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-PARTICLE"));
			return true;
		}

		/* Task was successfully. Send BuyMessage */
		sender.sendMessage(plugin.getYamlManager().getLanguage().sellSuccess.replace("{regionID}", regionID));
		return true;
	}

}
