package net.coding.program.common.event;

public class EventBoardRefreshRequest {
    public final int listId;
    public final int page;

    public EventBoardRefreshRequest(int listId, int page) {
        this.listId = listId;
        this.page = page;
    }
}
