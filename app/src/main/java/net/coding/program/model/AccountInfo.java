package net.coding.program.model;

import android.content.Context;
import android.content.SharedPreferences;

import net.coding.program.user.UsersListActivity;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by cc191954 on 14-8-7.
 */
public class AccountInfo {

    private static final String ACCOUNT = "ACCOUNT";

    public static void loginOut(Context ctx) {
        File dir = ctx.getFilesDir();
        String[] fileNameList = dir.list();
        for (String item : fileNameList) {
            File file = new File(dir, item);
            if (file.exists()) {
                file.delete();
            }
        }

        AccountInfo.setNeedPush(ctx, true);

    }

    public static void saveAccount(Context ctx, UserObject data) {
        File file = new File(ctx.getFilesDir(), ACCOUNT);
        if (file.exists()) {
            file.delete();
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(ctx.openFileOutput(ACCOUNT, Context.MODE_PRIVATE));
            oos.writeObject(data);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static UserObject loadAccount(Context ctx) {
        UserObject data = null;
        File file = new File(ctx.getFilesDir(), ACCOUNT);
        if (file.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(ctx.openFileInput(ACCOUNT));
                data = (UserObject) ois.readObject();
                ois.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (data == null) {
            data = new UserObject();
        }

        return data;
    }

    private static final String PROJECTS = "PROJECTS";

    public static void saveProjects(Context ctx, ArrayList<ProjectObject> data) {
        if (ctx == null) {
            return;
        }

        File file = new File(ctx.getFilesDir(), PROJECTS);
        if (file.exists()) {
            file.delete();
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(ctx.openFileOutput(PROJECTS, Context.MODE_PRIVATE));
            oos.writeObject(data);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<ProjectObject> loadProjects(Context ctx) {
        ArrayList<ProjectObject> data = null;
        File file = new File(ctx.getFilesDir(), PROJECTS);
        if (file.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(ctx.openFileInput(PROJECTS));
                data = (ArrayList<ProjectObject>) ois.readObject();
                ois.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (data == null) {
            data = new ArrayList<ProjectObject>();
        }

        return data;
    }

    public static boolean isCacheProjects(Context ctx) {
        File file = new File(ctx.getFilesDir(), PROJECTS);
        return file.exists();
    }


    private static final String MESSAGE_USERS = "MESSAGE_USERS";

    public static void saveMessageUsers(Context ctx, ArrayList<Message.MessageObject> data) {
        if (ctx == null) {
            return;
        }

        File file = new File(ctx.getFilesDir(), MESSAGE_USERS);
        if (file.exists()) {
            file.delete();
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(ctx.openFileOutput(MESSAGE_USERS, Context.MODE_PRIVATE));
            oos.writeObject(data);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Message.MessageObject> loadMessageUsers(Context ctx) {
        ArrayList<Message.MessageObject> data = null;
        File file = new File(ctx.getFilesDir(), MESSAGE_USERS);
        if (file.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(ctx.openFileInput(MESSAGE_USERS));
                data = (ArrayList<Message.MessageObject>) ois.readObject();
                ois.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (data == null) {
            data = new ArrayList<Message.MessageObject>();
        }

        return data;
    }

    public static void saveMessages(Context ctx, String globalKey, ArrayList<Message.MessageObject> data) {
        if (ctx == null) {
            return;
        }

        File file = new File(ctx.getFilesDir(), globalKey);
        if (file.exists()) {
            file.delete();
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(ctx.openFileOutput(globalKey, Context.MODE_PRIVATE));
            oos.writeObject(data);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Message.MessageObject> loadMessages(Context ctx, String globalKey) {
        ArrayList<Message.MessageObject> data = null;
        File file = new File(ctx.getFilesDir(), globalKey);
        if (file.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(ctx.openFileInput(globalKey));
                data = (ArrayList<Message.MessageObject>) ois.readObject();
                ois.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (data == null) {
            data = new ArrayList<Message.MessageObject>();
        }

        return data;
    }

    static class DataCache<T> {

        public void save(Context ctx, ArrayList<T> data, String name) {
            if (ctx == null) {
                return;
            }

            File file = new File(ctx.getFilesDir(), name);
            if (file.exists()) {
                file.delete();
            }

            try {
                ObjectOutputStream oos = new ObjectOutputStream(ctx.openFileOutput(name, Context.MODE_PRIVATE));
                oos.writeObject(data);
                oos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ArrayList<T> load(Context ctx, String name) {
            ArrayList<T> data = null;
            File file = new File(ctx.getFilesDir(), name);
            if (file.exists()) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(ctx.openFileInput(name));
                    data = (ArrayList<T>) ois.readObject();
                    ois.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (data == null) {
                data = new ArrayList<T>();
            }

            return data;
        }
    }

    private static final String CACHE_FRIEND_FOLLOW = "CACHE_FRIEND_FOLLOW";
    private static final String CACHE_FRIEND_FANS = "CACHE_FRIEND_FANS";

    public static void saveFriends(Context ctx, ArrayList<UserObject> data, UsersListActivity.Friend type) {
        String name = friendTypeToFileName(type);
        new DataCache<UserObject>().save(ctx, data, name);
    }

    public static ArrayList<UserObject> loadFriends(Context ctx, UsersListActivity.Friend type) {
        String fileName = friendTypeToFileName(type);
        return new DataCache<UserObject>().load(ctx, fileName);
    }

    private static String friendTypeToFileName(UsersListActivity.Friend type) {
        if (type == UsersListActivity.Friend.Follow) {
            return CACHE_FRIEND_FOLLOW;
        }

        return CACHE_FRIEND_FANS;
    }

    private static final String PROJECT_MEMBER = "PROJECT_MEMBER";

    public static void saveProjectMembers(Context ctx, ArrayList<TaskObject.Members> data, String projectId) {
        new DataCache<TaskObject.Members>().save(ctx, data, PROJECT_MEMBER + projectId);
    }

    public static ArrayList<TaskObject.Members> loadProjectMembers(Context ctx, String projectId) {
        return new DataCache<TaskObject.Members>().load(ctx, PROJECT_MEMBER + projectId);
    }

    private static String FILE_PUSH = "FILE_PUSH";
    private static String KEY_NEED_PUSH = "KEY_NEED_PUSH";

    public static boolean getNeedPush(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(FILE_PUSH, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_NEED_PUSH, true);
    }

    public static void setNeedPush(Context ctx, boolean push) {
        SharedPreferences sp = ctx.getSharedPreferences(FILE_PUSH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_NEED_PUSH, push);
        editor.commit();
    }

    private static final String USER_MAOPAO = "USER_MAOPAO";
    public static void saveMaopao(Context ctx, ArrayList<Maopao.MaopaoObject> data, String type, String id) {
        new DataCache<Maopao.MaopaoObject>().save(ctx, data, USER_MAOPAO + type + id);
    }

    public static ArrayList<Maopao.MaopaoObject> loadMaopao(Context ctx, String type, String id) {
        return new DataCache<Maopao.MaopaoObject>().load(ctx, USER_MAOPAO + type + id);
    }
}
