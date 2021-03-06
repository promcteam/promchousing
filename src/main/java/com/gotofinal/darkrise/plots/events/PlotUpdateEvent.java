package com.gotofinal.darkrise.plots.events;

import com.gotofinal.darkrise.plots.deeds.Plot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlotUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Plot plot;

    public PlotUpdateEvent(Plot plot) {
        this.plot = plot;
    }

    public Plot getPlot() {
        return plot;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

}
