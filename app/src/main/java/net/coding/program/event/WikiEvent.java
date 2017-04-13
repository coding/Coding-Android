package net.coding.program.event;

import net.coding.program.network.model.wiki.Wiki;

public class WikiEvent {

    public WikiEvent(EventAction action, Wiki wiki) {
        this.action = action;
        this.wiki = wiki;
    }

    public WikiEvent() {
    }

    public EventAction action;
    public Wiki wiki = null;
}
