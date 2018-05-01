package net.coding.program.task.add;

import net.coding.program.common.model.SingleTask;
import net.coding.program.common.model.UserObject;
import net.coding.program.network.model.task.BoardList;

/**
 * Created by chenchao on 15/7/7.
 * 任务数据
 */
class TaskParams {
    String content = "";
    int status;
    int ownerId;
    int priority;
    String deadline = "";
    BoardList taskBoard; // 只保存数据，不保存版本

    UserObject owner;

    public TaskParams(SingleTask singleTask) {
        content = singleTask.content;
        status = singleTask.status;
        ownerId = singleTask.owner_id;
        priority = singleTask.priority;
        owner = singleTask.owner;
        deadline = singleTask.deadline;
        taskBoard = singleTask.taskBoardList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskParams)) return false;

        TaskParams that = (TaskParams) o;

        if (ownerId != that.ownerId) return false;
        if (priority != that.priority) return false;
        if (status != that.status) return false;
        if (!content.equals(that.content)) return false;
        if (!deadline.equals(that.deadline)) return false;

//        if (taskBoard != null) {
//            if (that.taskBoard != null) {
//                if (taskBoard.id != that.taskBoard.id) {
//                    return false;
//                }
//            } else {
//                return false;
//            }
//        } else {
//            if (that.taskBoard != null) {
//                return false;
//            }
//        }

        return owner.global_key.equals(that.owner.global_key);

    }

    @Override
    public int hashCode() {
        int result = content.hashCode();
        result = 31 * result + status;
        result = 31 * result + ownerId;
        result = 31 * result + priority;
        result = 31 * result + deadline.hashCode();
        result = 31 * result + owner.hashCode();
        return result;
    }
}
