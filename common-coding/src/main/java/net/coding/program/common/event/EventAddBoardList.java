package net.coding.program.common.event;

import net.coding.program.network.model.task.BoardList;

public class EventAddBoardList {

    public BoardList data;

    public EventAddBoardList(BoardList data) {
        this.data = data;
    }
}
