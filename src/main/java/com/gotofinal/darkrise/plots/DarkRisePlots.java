package com.gotofinal.darkrise.plots;

import com.gotofinal.darkrise.plots.commands.GeneralCommands;
import com.gotofinal.darkrise.plots.config.ConfigHandler;
import com.gotofinal.darkrise.plots.deeds.Deed;
import com.gotofinal.darkrise.plots.deeds.GlobalPlotsManager;
import com.gotofinal.darkrise.plots.deeds.Plot;
import com.gotofinal.darkrise.plots.deeds.PlotManager;
import com.gotofinal.darkrise.plots.util.bungee.BungeeListener;
import com.gotofinal.darkrise.plots.util.bungee.JoinListener;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.travja.darkrise.core.ConfigManager;
import me.travja.darkrise.core.bungee.BungeeUtil;
import me.travja.darkrise.core.legacy.killme.chat.placeholder.PlaceholderType;
import me.travja.darkrise.core.legacy.util.Init;
import me.travja.darkrise.core.legacy.util.message.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public class DarkRisePlots extends JavaPlugin {
    private static DarkRisePlots instance;
    private BukkitTask task;
    private GlobalPlotsManager globalPlotsManager;
    private ConfigHandler configHandler;
    private CommandsManager<CommandSender> commandsManager;

    public ConfigHandler getConfigHandler() {
        return this.configHandler;
    }

    private void setupCommands() {
        this.commandsManager = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(final CommandSender sender, final String permission) {
                return sender.hasPermission(permission);
            }
        };
        this.commandsManager.setInjector(new SimpleInjector(this));
        final CommandsManagerRegistration registration = new CommandsManagerRegistration(this, this.commandsManager);
        registration.register(GeneralCommands.class);
    }

    {
        instance = this;
    }

    public GlobalPlotsManager getGlobalPlotsManager() {
        return this.globalPlotsManager;
    }

    public Plot getPlot(final Player player) {
        final PlotManager mgr = getGlobalPlotsManager().getPlotManager(player.getWorld());
        if (mgr == null) {
            return null;
        }
        for (final Plot plot : mgr.getPlots().values()) {
            if (plot.isOwner(player)) {
                return plot;
            }
        }
        return null;
    }

    public static DarkRisePlots getInstance() {
        return instance;
    }

    @Override
    public void reloadConfig() {
        this.configHandler = new ConfigHandler(this);
    }

    public static final PlaceholderType<ProtectedRegion> PROTECTED_REGION = PlaceholderType.create("wgregion", ProtectedRegion.class);
    public static final PlaceholderType<Plot> PLOT = PlaceholderType.create("plot", Plot.class);
    public static final PlaceholderType<Deed> DEED = PlaceholderType.create("deed", Deed.class);

    @Override
    public void onLoad() {
        reloadConfig();
        this.getServer().getMessenger().registerIncomingPluginChannel(this, BungeeUtil.CHANNEL, new BungeeListener());
        PROTECTED_REGION.registerItem("id", ProtectedRegion::getId);
        PROTECTED_REGION.registerItem("priority", ProtectedRegion::getPriority);
        PROTECTED_REGION.registerItem("type", r -> r.getType().getName());
        PROTECTED_REGION.registerItem("members", r -> r.getMembers().toUserFriendlyString());
        PROTECTED_REGION.registerItem("owners", r -> r.getOwners().toUserFriendlyString());
        PLOT.registerItem("name", Plot::getName);
        PLOT.registerItem("owner", Plot::getOwner);
        PLOT.registerItem("deedName", p -> p.getDeed().getDisplayName());
        PLOT.registerItem("players", p -> p.getPlayers().toString());
        PLOT.registerItem("isOwned", Plot::isOwned);
        PLOT.registerItem("members", p -> p.getProtectedRegion().getMembers().toUserFriendlyString());
        PLOT.registerItem("owners", p -> p.getProtectedRegion().getOwners().toUserFriendlyString());
        DEED.registerItem("name", Deed::getName);
        DEED.registerItem("description", Deed::getDescription);
        DEED.registerItem("displayName", Deed::getDisplayName);
        DEED.registerItem("dropChance", Deed::getDropChance);
        DEED.registerItem("extensionTime", Deed::getExtensionTime);
        DEED.registerItem("friends", Deed::getFriends);
        DEED.registerItem("initialExtensionTime", Deed::getInitialExtensionTime);
        DEED.registerItem("maximumExtensionTime", Deed::getMaximumExtensionTime);
        DEED.registerItem("tax", Deed::getTax);

        PROTECTED_REGION.registerChild("parent", PROTECTED_REGION, ProtectedRegion::getParent);
        PLOT.registerChild("region", PROTECTED_REGION, Plot::getProtectedRegion);
        PLOT.registerChild("home", Init.LOCATION, Plot::getHome);
        PLOT.registerChild("sign", Init.LOCATION, Plot::getSignLocation);
        PLOT.registerChild("region", DEED, Plot::getDeed);
        FileConfiguration lang = ConfigManager.loadConfigFile(new File(getDataFolder() + File.separator + "lang", "lang_en.yml"), getResource("lang/lang_en.yml"));
        MessageUtil.load(lang, this);
        super.onLoad();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {

        try {
            this.commandsManager.execute(cmd.getName(), args, sender, sender);
        } catch (final CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (final MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (final CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (final WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (final CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }


    @Override
    public void onEnable() {
        super.onEnable();
        this.setupCommands();
        this.globalPlotsManager = new GlobalPlotsManager(this);
        this.globalPlotsManager.reloadConfig();
        this.getServer().getPluginManager().registerEvents(new JoinListener(), this);
    }

    @Override
    public void onDisable() {
        if (this.globalPlotsManager != null) {
            this.globalPlotsManager.unloadAll();
        }
        super.onDisable();
    }
}
