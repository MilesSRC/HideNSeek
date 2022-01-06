package net.fnafmc.hideandseek;

import net.fnafmc.hideandseek.commands.EventCommand;
import net.fnafmc.hideandseek.listeners.EventListener;
import net.fnafmc.hideandseek.utils.EventDataManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private static Plugin plugin;
    private static EventDataManager eventDataManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        eventDataManager = new EventDataManager();

        // Commands
        getServer().getPluginCommand("event").setExecutor(new EventCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static EventDataManager getEventDataManager() {
        return eventDataManager;
    }
}
