package de.kekshaus.cubit.land.plugin;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import de.kekshaus.cubit.land.Landplugin;

public class LandConfig {

	private Landplugin plugin;
	private FileConfiguration configFile;

	/* Public config values */

	/* Land module */
	public double landBasePrice;
	public double landTaxAddition;
	public double landSellPercent;
	public double landDeprecatedMember;
	public double landDeprecatedOther;
	public boolean landUseMaterialBorder;
	public Material landBuyMaterialBorder;
	public Material landSellaterialBorder;

	/* Database module */
	public boolean sqlUse;
	public String sqlDataBase;
	public String sqlHostname;
	public int sqlPort;
	public String sqlUser;
	public String sqlPassword;

	public LandConfig(Landplugin plugin) {
		this.plugin = plugin;
		initConfig();
	}

	private void initConfig() {
		saveConfig();
		initContent();
	}

	private void saveConfig() {
		this.plugin.saveDefaultConfig();
		this.plugin.saveConfig();
		this.plugin.reloadConfig();
		this.configFile = this.plugin.getConfig();
	}

	private void initContent() {
		setContent();
		saveConfig();
		loadContent();
	}

	private void setContent() {

		/* Land module */
		checkContent("module.land.basePrice", 235.00D);
		checkContent("module.land.taxAddition", 5.00D);
		checkContent("module.land.sellPercentInDecimal", 0.5D);
		checkContent("module.land.deprecatedBuyupMember", 30.0D);
		checkContent("module.land.deprecatedBuyupOther", 45.00D);
		checkContent("module.land.useMaterialBorder", true);
		checkContent("module.land.buyMaterialBorder", Material.TORCH);
		checkContent("module.land.sellMaterialBorder", Material.REDSTONE_TORCH_ON);

		/* Database module */
		checkContent("module.database.useSql", false);
		checkContent("module.database.databaseName", "cubit");
		checkContent("module.database.hostName", "localhost");
		checkContent("module.database.port", 3306);
		checkContent("module.database.userName", "yourSqlUser");
		checkContent("module.database.password", "yourSqlPassword");

	}

	private void loadContent() {
		this.sqlUse = (boolean) this.configFile.get("module.database.useSql");
		this.sqlDataBase = (String) this.configFile.get("module.database.databaseName");
		this.sqlHostname = (String) this.configFile.get("module.database.hostName");
		this.sqlPort = (int) this.configFile.get("module.database.port");
		this.sqlUser = (String) this.configFile.get("module.database.userName");
		this.sqlPassword = (String) this.configFile.get("module.database.password");
	}

	private void checkContent(String path, Object defaultValue) {
		if (!containsContent(path)) {
			addContent(path, defaultValue);
		}
	}

	private void addContent(String path, Object defaultValue) {
		this.configFile.set(path, defaultValue);
	}

	private boolean containsContent(String path) {
		if (this.configFile.contains(path)) {
			return true;
		}
		return false;
	}

}