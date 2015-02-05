package net.coding.program.common;

import net.coding.program.model.Maopao;
import net.coding.program.model.TaskObject;
import net.coding.program.model.TopicObject;

import java.util.HashMap;

/**
 * Created by chaochen on 15/1/27.
 */
public class CommentBackup {

    public enum Type {
        Maopao, Topic, Task
    }

    public static class BackupParam {
        Type type;
        int id;
        int owner_id;

        public BackupParam(Type type, int id, int owner_id) {
            this.type = type;
            this.id = id;
            this.owner_id = owner_id;
        }

        public static BackupParam create(Object object) {
            if (object == null) {
                return null;
            }

            if (object instanceof Maopao.Comment) {
                Maopao.Comment maopaoComment = (Maopao.Comment) object;
                int ownerId = maopaoComment.owner_id;
                if (maopaoComment.id == 0) { // 表示直接回复冒泡，没有@某人
                    ownerId = 0;
                }
                return new BackupParam(Type.Maopao, maopaoComment.tweet_id, ownerId);
            } else if (object instanceof TopicObject) {
                TopicObject topicObject = (TopicObject) object;

                int parentId = topicObject.parent_id;
                if (parentId == 0) {
                    parentId = topicObject.id;
                }
                return new BackupParam(Type.Topic, parentId, topicObject.owner_id);

            } else if (object instanceof TaskObject.TaskComment) {
                TaskObject.TaskComment comment = (TaskObject.TaskComment) object;
                return new BackupParam(Type.Task, comment.taskId, comment.owner_id);
            } else if (object instanceof BackupParam) {
                return (BackupParam) object;
            }

            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BackupParam)) return false;

            BackupParam param = (BackupParam) o;

            if (id != param.id) return false;
            if (owner_id != param.owner_id) return false;
            if (type != param.type) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + id;
            result = 31 * result + owner_id;
            return result;
        }
    }

    private CommentBackup() {
    }

    private static CommentBackup sCommentBackup;

    public static CommentBackup getInstance() {
        if (sCommentBackup == null) {
            sCommentBackup = new CommentBackup();
        }
        return sCommentBackup;
    }

    private HashMap<BackupParam, String> mData = new HashMap();

    public void save(BackupParam param, String comment) {
        mData.put(param, comment);
    }

    public String load(BackupParam param) {
        String comment = mData.get(param);
        if (comment == null) {
            comment = "";
        }

        return comment;
    }

    public void delete(BackupParam param) {
        mData.remove(param);
    }
}
