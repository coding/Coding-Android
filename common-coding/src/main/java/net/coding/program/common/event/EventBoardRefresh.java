package net.coding.program.common.event;

public class EventBoardRefresh {
    public final int listId;
    public final int page;
    public final boolean result;

    public EventBoardRefresh(int listId, int page, boolean result) {
        this.listId = listId;
        this.page = page;
        this.result = result;
    }
}
