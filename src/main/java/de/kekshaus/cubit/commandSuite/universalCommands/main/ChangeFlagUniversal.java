package de.kekshaus.cubit.commandSuite.universalCommands.main;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.kekshaus.cubit.api.classes.enums.LandTypes;
import de.kekshaus.cubit.api.classes.interfaces.ICommand;
import de.kekshaus.cubit.api.classes.interfaces.IProtectionFlag;
import de.kekshaus.cubit.api.regionAPI.region.RegionData;
import de.kekshaus.cubit.plugin.Landplugin;

public class ChangeFlagUniversal implements ICommand {

	private Landplugin plugin;
	private IProtectionFlag packet;
	private boolean isAdmin;
	private String permNode;
	private LandTypes type;

	public ChangeFlagUniversal(Landplugin plugin, IProtectionFlag packet, String permNode, LandTypes type,
			boolean isAdmin) {
		this.plugin = plugin;
		this.packet = packet;
		this.isAdmin = isAdmin;
		this.permNode = permNode;
		this.type = type;

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
		RegionData regionData = plugin.getRegionManager().praseRegionData(loc.getWorld(), chunk.getX(), chunk.getZ());

		/*
		 * Check if the player has permissions for this land or hat landadmin
		 * permissions
		 */
		if (!plugin.getRegionManager().isValidRegion(loc.getWorld(), chunk.getX(), chunk.getZ())) {
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorNoLandFound);
			return true;
		}

		if (regionData.getLandType() != type && type != LandTypes.NOTYPE) {
			sender.sendMessage(
					plugin.getYamlManager().getLanguage().errorNoValidLandFound.replace("{type}", type.toString()));
			return true;
		}

		if (!plugin.getRegionManager().hasLandPermission(regionData, player.getUniqueId()) && this.isAdmin == false) {
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorNoLandPermission.replace("{regionID}",
					regionData.getRegionName()));
			return true;
		}

		/* Command based switch */
		if (args.length < 2) {
		} else if (args[1].toString().equalsIgnoreCase("on")) {

			packet.switchState(regionData, true, true);
			String stateString = plugin.getRegionManager().getStringState(packet.getState(regionData));
			sender.sendMessage(plugin.getYamlManager().getLanguage().flagSwitchSuccess
					.replace("{flag}", packet.getPacketName()).replace("{value}", stateString));

			return true;
		} else if (args[1].toString().equalsIgnoreCase("off")) {

			packet.switchState(regionData, false, true);
			String stateString = plugin.getRegionManager().getStringState(packet.getState(regionData));
			sender.sendMessage(plugin.getYamlManager().getLanguage().flagSwitchSuccess
					.replace("{flag}", packet.getPacketName()).replace("{value}", stateString));

			return true;
		}
		/* Switch flag-state to the opposite value */
		packet.switchState(regionData, true);
		String stateString = plugin.getRegionManager().getStringState(packet.getState(regionData));

		if (!plugin.getParticleManager().changeFlag(player, loc)) {
			/* If this task failed! This should never happen */
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-PARTICLE"));
			plugin.getLogger()
					.warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-PARTICLE"));
			return true;
		}

		sender.sendMessage(plugin.getYamlManager().getLanguage().flagSwitchSuccess
				.replace("{flag}", packet.getPacketName()).replace("{value}", stateString));

		return true;
	}

}
