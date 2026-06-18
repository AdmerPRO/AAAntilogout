package pl.admerpro.aaantylogout;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pl.admerpro.aaantylogout.command.AntylogoutCommand;
import pl.admerpro.aaantylogout.combat.CombatManager;
import pl.admerpro.aaantylogout.config.PluginSettings;
import pl.admerpro.aaantylogout.history.HistoryService;
import pl.admerpro.aaantylogout.listener.CombatListener;
import pl.admerpro.aaantylogout.listener.RestrictionListener;
import pl.admerpro.aaantylogout.region.RegionService;
import pl.admerpro.aaantylogout.util.MessageService;

public final class AAAntylogoutPlugin extends JavaPlugin {

    private PluginSettings settings;
    private MessageService messages;
    private HistoryService historyService;
    private RegionService regionService;
    private CombatManager combatManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadServices();
        registerListeners();
        registerCommand();
        combatManager.start();
        getLogger().info("AAAntylogout enabled.");
    }

    @Override
    public void onDisable() {
        if (combatManager != null) {
            combatManager.shutdown();
        }
        if (historyService != null) {
            historyService.save();
        }
        getLogger().info("AAAntylogout disabled.");
    }

    public void reloadPlugin() {
        reloadConfig();
        settings = PluginSettings.load(this);
        messages.update(settings);
        historyService.update(settings);
        regionService.update(settings);
        combatManager.update(settings);
    }

    public PluginSettings settings() {
        return settings;
    }

    public MessageService messages() {
        return messages;
    }

    public HistoryService historyService() {
        return historyService;
    }

    public CombatManager combatManager() {
        return combatManager;
    }

    private void loadServices() {
        settings = PluginSettings.load(this);
        messages = new MessageService(this, settings);
        historyService = new HistoryService(this, settings);
        regionService = new RegionService(this, settings);
        combatManager = new CombatManager(this, settings, messages, historyService, regionService);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new CombatListener(this, combatManager, messages), this);
        getServer().getPluginManager().registerEvents(new RestrictionListener(combatManager, regionService, messages), this);
    }

    private void registerCommand() {
        AntylogoutCommand command = new AntylogoutCommand(this);
        PluginCommand pluginCommand = getCommand("aaalo");
        if (pluginCommand == null) {
            getLogger().severe("Command aaalo is missing from plugin.yml.");
            return;
        }
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
    }
}
