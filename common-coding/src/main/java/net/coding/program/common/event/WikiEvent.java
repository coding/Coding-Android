package net.coding.program.common.event;

import net.coding.program.network.model.wiki.Wiki;

public class WikiEvent {

    public EventAction action;
    public Wiki wiki = null;

    public WikiEvent(EventAction action, Wiki wiki) {
        this.action = action;
        this.wiki = wiki;
    }

    public WikiEvent() {
    }
}
