package de.kekshaus.cubit.commandSuite.adminCommands.main;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.kekshaus.cubit.api.classes.enums.LandTypes;
import de.kekshaus.cubit.api.classes.interfaces.ICommand;
import de.kekshaus.cubit.plugin.Landplugin;

public class CreateServerAdmin implements ICommand {

	private Landplugin plugin;
	private String permNode;

	public CreateServerAdmin(Landplugin plugin, String permNode) {
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
		final String regionID = plugin.getRegionManager().buildLandName(LandTypes.SERVER.toString(), chunk.getX(),
				chunk.getZ());

		/* Check if this is a valid buyTask */
		if (plugin.getRegionManager().isValidRegion(loc.getWorld(), chunk.getX(), chunk.getZ())) {
			sender.sendMessage(plugin.getYamlManager().getLanguage().isAlreadyLand.replace("{regionID}", regionID));
			return true;
		}

		if (!plugin.getRegionManager().createRegion(loc, null, LandTypes.SERVER)) {
			/* If this task failed! This should never happen */
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-REGION"));
			plugin.getLogger()
					.warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-REGION"));
			return true;
		}

		if (!plugin.getParticleManager().sendBuy(player, loc)) {
			/* If this task failed! This should never happen */
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-PARTICLE"));
			plugin.getLogger()
					.warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "CREATE-PARTICLE"));
			return true;
		}

		/* Task was successfully. Send BuyMessage */
		sender.sendMessage(plugin.getYamlManager().getLanguage().buySuccess.replace("{regionID}", regionID));
		return true;
	}

}
