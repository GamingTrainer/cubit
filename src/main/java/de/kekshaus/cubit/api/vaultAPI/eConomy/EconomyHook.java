package de.kekshaus.cubit.api.vaultAPI.eConomy;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import de.kekshaus.cubit.plugin.Landplugin;
import net.milkbowl.vault.economy.Economy;

public class EconomyHook {

	private Economy econ;

	public EconomyHook(Landplugin plugin, Economy econ) {
		this.econ = econ;

	}

	public boolean hasEnoughToBuy(UUID playerUUID, double value) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
		if ((econ.getBalance(player) - value) >= 0) {
			return true;
		}
		return false;
	}

	public void transferMoney(UUID senderUUID, UUID recieverUUID, double value) {
		if (senderUUID != null) {
			OfflinePlayer sender = Bukkit.getOfflinePlayer(senderUUID);
			econ.withdrawPlayer(sender, value);
		}
		if (recieverUUID != null) {
			OfflinePlayer reciever = Bukkit.getOfflinePlayer(recieverUUID);
			econ.depositPlayer(reciever, value);

		}
	}

	public String formateToEconomy(double value) {
		return econ.format(value);
	}

}
