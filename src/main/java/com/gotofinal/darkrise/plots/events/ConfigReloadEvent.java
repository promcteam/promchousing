package com.gotofinal.darkrise.plots.events;

import com.gotofinal.darkrise.plots.config.ConfigHandler;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is fired every time the configuration is reloaded.
 */
public class ConfigReloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final ConfigHandler config;

    public ConfigReloadEvent(final ConfigHandler config) {
        this.config = config;
    }


    public ConfigHandler getConfig() {
        return this.config;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("config", this.config).toString();
    }
}
