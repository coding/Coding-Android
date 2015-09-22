package net.coding.program.model;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import net.coding.program.common.Global;
import net.coding.program.common.LoginBackground;
import net.coding.program.login.ZhongQiuGuideActivity;
import net.coding.program.maopao.MaopaoAddActivity;
import net.coding.program.message.MessageListActivity;
import net.coding.program.user.UsersListActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by cc191954 on 14-8-7.
 * 保存数据到本地，包括用户数据和全局数据.
 */
public class AccountInfo {

    private static final String ACCOUNT = "ACCOUNT";
    private static final String PROJECTS = "PROJECTS";
    private static final String MESSAGE_USERS = "MESSAGE_USERS";
    private static final String CACHE_FRIEND_FOLLOW = "CACHE_FRIEND_FOLLOW";
    private static final String CACHE_FRIEND_FANS = "CACHE_FRIEND_FANS";
    private static final String AUTH_URI_DATAS = "AUTH_URI_DATAS";
    private static final String PROJECT_MEMBER = "PROJECT_MEMBER";
    private static final String MESSAGE_DRAFT = "MESSAGE_DRAFT";
    private static final String GLOBAL_SETTING = "GLOBAL_SETTING";
    private static final String GLOBAL_SETTING_BACKGROUND = "GLOBAL_SETTING_BACKGROUND";
    private static final String USER_MAOPAO = "USER_MAOPAO";
    private static final String MAOPAO_DRAFT = "MAOPAO_DRAFT";
    private static final String USER_RELOGIN_INFO = "USER_RELOGIN_INFO";
    // 上次成功登录时用户输入的用户名，可能是邮箱或个性后缀
    private static final String GLOBAL_LAST_LOGIN_NAME = "GLOBAL_LAST_LOGIN_NAME";
    private static final String USER_TASK_PROJECTS = "USER_TASK_PROJECTS";
    private static final String USER_TASKS = "USER_TASKS_%d_%d";
    private static final String USER_NO_SEND_MESSAGE = "USER_NO_SEND_MESSAGE";
    private static final String BACKGROUNDS = "BACKGROUNDS";
    private static final String FILE_PUSH = "FILE_PUSH";
    private static final String KEY_NEED_PUSH = "KEY_NEED_PUSH";

    private static final String KEY_CUSTOM_HOST = "KEY_CUSTOM_HOST";
    private static final String KEY_MAOPAO_BANNER = "KEY_MAOPAO_BANNER";

    private static final String KEY_CACHE_GET_REQUEST = "KEY_CACHE_GET_REQUEST";

    // 每添加一个
    private static final String MARK_GUIDE_32 = "MARK_GUIDE_32"; // 标记3.2中的引导页面
    private static final String MARK_GUIDE_FEATURES = "MARK_GUIDE_325"; // 修改这个值就可以了


    public static void loginOut(Context ctx) {
        File dir = ctx.getFilesDir();
        String[] fileNameList = dir.list();
        for (String item : fileNameList) {
            File file = new File(dir, item);
            if (file.exists() && !file.isDirectory()) {
                file.delete();
            }
        }

        AccountInfo.setNeedPush(ctx, true);

        NotificationManager notificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
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

    public static boolean isLogin(Context ctx) {
        File file = new File(ctx.getFilesDir(), ACCOUNT);
        return file.exists();
    }

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

    public static JSONObject getGetRequestCache(Context context, String request) {
        String s = new DataCache<String>().loadObject(context, Global.encodeUtf8(request), KEY_CACHE_GET_REQUEST);
        try {
            return new JSONObject(s);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public static void saveGetRequestCache(Context context, String request, JSONObject json) {
        new DataCache<String>().save(context, json.toString(), Global.encodeUtf8(request), KEY_CACHE_GET_REQUEST);
    }

    @SuppressWarnings("unchecked")
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
            data = new ArrayList<>();
        }

        return data;
    }

    public static boolean needDisplayGuide(Context context) {
        if (!ZhongQiuGuideActivity.isZhongqiu()) {
            return false;
        }

        Boolean result = new DataCache<Boolean>().loadGlobalObject(context, MARK_GUIDE_FEATURES);
        if (result == null) {
            return true;
        }

        return result;
    }

    public static void markGuideReaded(Context context) {
        new DataCache<Boolean>().saveGlobal(context, Boolean.FALSE, MARK_GUIDE_FEATURES);
    }

    public static boolean isCacheProjects(Context ctx) {
        File file = new File(ctx.getFilesDir(), PROJECTS);
        return file.exists();
    }

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

    @SuppressWarnings("unchecked")
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
            data = new ArrayList<>();
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

    @SuppressWarnings("unchecked")
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
            data = new ArrayList<>();
        }

        return data;
    }

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

    public static void saveAuthDatas(Context context, ArrayList<String> data) {
        new DataCache<String>().saveGlobal(context, data, AUTH_URI_DATAS);
    }

    public static ArrayList<String> loadAuthDatas(Context context) {
        return new DataCache<String>().loadGlobal(context, AUTH_URI_DATAS);
    }

    public static String loadAuth(Context context, String globalKey) {
        if (globalKey == null || globalKey.isEmpty()) {
            return "";
        }

        globalKey = "/" + globalKey;
        ArrayList<String> uris = loadAuthDatas(context);
        for (String uriString : uris) {
            Uri uri = Uri.parse(uriString);
            String[] item = uri.getPath().split("@");
            if (item.length >= 2 && item[1].equals("Coding") && item[0].equals(globalKey)) {
                return uriString;
            }
        }

        return "";
    }

//    public static void saveProjectMembers(Context ctx, ArrayList<TaskObject.Members> data, int projectId) {
//        new DataCache<TaskObject.Members>().save(ctx, data, PROJECT_MEMBER + projectId);
//    }

//    public static ArrayList<TaskObject.Members> loadProjectMembers(Context ctx, int projectId) {
//        return new DataCache<TaskObject.Members>().load(ctx, PROJECT_MEMBER + projectId);
//    }

    // input 为 "" 时，删除上次的输入
    public static void saveMessageDraft(Context ctx, String input, String globalkey) {
        if (input.isEmpty()) { //
            new DataCache<String>().delete(ctx, MESSAGE_DRAFT + globalkey);
        } else {
            ArrayList<String> data = new ArrayList<>();
            data.add(input);
            new DataCache<String>().save(ctx, data, MESSAGE_DRAFT + globalkey);
        }
    }

    public static String loadMessageDraft(Context ctx, String globalKey) {
        ArrayList<String> data = new DataCache<String>().load(ctx, MESSAGE_DRAFT + globalKey);
        if (data.isEmpty()) {
            return "";
        } else {
            return data.get(0);
        }
    }

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

    public static void setCheckLoginBackground(Context ctx) {
        Calendar calendar = Calendar.getInstance();
        SharedPreferences.Editor editor = ctx.getSharedPreferences(GLOBAL_SETTING, Context.MODE_PRIVATE).edit();
        editor.putLong(GLOBAL_SETTING_BACKGROUND, calendar.getTimeInMillis());
        editor.commit();
    }

    // 距离上次检查24小时后再检查
    public static boolean needCheckLoginBackground(Context ctx) {
        long last = ctx.getSharedPreferences(GLOBAL_SETTING, Context.MODE_PRIVATE)
                .getLong(GLOBAL_SETTING_BACKGROUND, 0);
        return (Calendar.getInstance().getTimeInMillis() - last) > 1000 * 3600 * 24;
    }

    public static void saveMaopao(Context ctx, ArrayList<Maopao.MaopaoObject> data, String type, int id) {
        new DataCache<Maopao.MaopaoObject>().save(ctx, data, USER_MAOPAO + type + id);
    }

    public static ArrayList<Maopao.MaopaoObject> loadMaopao(Context ctx, String type, int id) {
        return new DataCache<Maopao.MaopaoObject>().load(ctx, USER_MAOPAO + type + id);
    }

    public static void saveMaopaoDraft(Context ctx, MaopaoAddActivity.MaopaoDraft draft) {
        if (draft.isEmpty()) {
            new DataCache<MaopaoAddActivity.MaopaoDraft>().delete(ctx, MAOPAO_DRAFT);
        } else {
            ArrayList<MaopaoAddActivity.MaopaoDraft> data = new ArrayList<>();
            data.add(draft);
            new DataCache<MaopaoAddActivity.MaopaoDraft>().save(ctx, data, MAOPAO_DRAFT);
        }
    }

    public static MaopaoAddActivity.MaopaoDraft loadMaopaoDraft(Context ctx) {
        ArrayList<MaopaoAddActivity.MaopaoDraft> data = new DataCache<MaopaoAddActivity.MaopaoDraft>().load(ctx, MAOPAO_DRAFT);
        if (data.isEmpty()) {
            return new MaopaoAddActivity.MaopaoDraft();
        } else {
            return data.get(0);
        }
    }

    public static void saveReloginInfo(Context ctx, String email, String globayKey) {
        DataCache<Pair> dateCache = new DataCache<>();
        ArrayList<Pair> listData = dateCache.loadGlobal(ctx, USER_RELOGIN_INFO);
        for (int i = 0; i < listData.size(); ++i) {
            if (listData.get(i).second.equals(globayKey)) {
                listData.remove(i);
                --i;
            }
        }

        listData.add(new Pair(email, globayKey));
        dateCache.saveGlobal(ctx, listData, USER_RELOGIN_INFO);
    }

    public static void saveLastLoginName(Context context, String name) {
        new DataCache<String>().saveGlobal(context, name, GLOBAL_LAST_LOGIN_NAME);
    }

    public static String loadLastLoginName(Context context) {
        String s = new DataCache<String>().loadGlobalObject(context, GLOBAL_LAST_LOGIN_NAME);
        if (s == null) {
            return "";
        }

        return s;
    }

    public static void saveTaskProjects(Context context, ArrayList<ProjectObject> data) {
        new DataCache<ProjectObject>().save(context, data, USER_TASK_PROJECTS);
    }

    public static ArrayList<ProjectObject> loadTaskProjects(Context context) {
        return new DataCache<ProjectObject>().load(context, USER_TASK_PROJECTS);
    }

    public static void saveTasks(Context context, ArrayList<TaskObject.SingleTask> data, int projectId, int userId) {
        new DataCache<TaskObject.SingleTask>().save(context, data, String.format(USER_TASKS, projectId, userId));
    }

    public static ArrayList<TaskObject.SingleTask> loadTasks(Context context, int projectId, int userId) {
        return new DataCache<TaskObject.SingleTask>().load(context, String.format(USER_TASKS, projectId, userId));
    }

    public static void saveNoSendMessage(Context context, MessageListActivity.MyMessage message) {
        ArrayList<MessageListActivity.MyMessage> allMessages = loadNoSendMessage(context);
        allMessages.add(message);
        new DataCache<MessageListActivity.MyMessage>().save(context, allMessages, USER_NO_SEND_MESSAGE);
    }

    public static void saveCustomHost(Context context, CustomHost data) {
        new DataCache<CustomHost>().saveGlobal(context, data, KEY_CUSTOM_HOST);
    }

    public static CustomHost getCustomHost(Context context) {
        CustomHost host = new DataCache<CustomHost>().loadGlobalObject(context, KEY_CUSTOM_HOST);
        if (host == null) {
            host = new CustomHost();
        }

        return host;
    }

    public static void removeCustomHost(Context context) {
        new DataCache<String>().deleteGlobal(context, KEY_CUSTOM_HOST);
    }

    public static void saveMaopaoBanners(Context context, ArrayList<BannerObject> data) {
        new DataCache<BannerObject>().saveGlobal(context, data, KEY_MAOPAO_BANNER);
    }

    public static ArrayList<BannerObject> getMaopaoBanners(Context context) {
        return new DataCache<BannerObject>().loadGlobal(context, KEY_MAOPAO_BANNER);
    }

    public static void removeNoSendMessage(Context context, long createTime) {
        ArrayList<MessageListActivity.MyMessage> allMessages = loadNoSendMessage(context);
        for (int i = 0; i < allMessages.size(); ++i) {
            MessageListActivity.MyMessage item = allMessages.get(i);
            if (item.getCreateTime() == createTime) {
                allMessages.remove(i);
                break;
            }
        }

        new DataCache<MessageListActivity.MyMessage>().save(context, allMessages, USER_NO_SEND_MESSAGE);
    }

    public static ArrayList<MessageListActivity.MyMessage> loadNoSendMessage(Context context, String globalKey) {
        ArrayList<MessageListActivity.MyMessage> allMessages = loadNoSendMessage(context);
        ArrayList<MessageListActivity.MyMessage> messages = new ArrayList<>();
        for (MessageListActivity.MyMessage item : allMessages) {
            if (item.friend.global_key.equals(globalKey)) {
                messages.add(item);
            }
        }

        return messages;
    }

    public static ArrayList<MessageListActivity.MyMessage> loadNoSendMessage(Context context) {
        return new DataCache<MessageListActivity.MyMessage>().load(context, USER_NO_SEND_MESSAGE);
    }

    public static String loadRelogininfo(Context ctx, String key) {
        ArrayList<Pair> listData = new DataCache<Pair>().loadGlobal(ctx, USER_RELOGIN_INFO);
        if (key.contains("@")) {
            for (Pair item : listData) {
                if (item.first.equals(key)) {
                    return item.second;
                }
            }

        } else {
            for (Pair item : listData) {
                if (item.second.equals(key)) {
                    return item.second;
                }
            }
        }

        return "";
    }

    public static String[] loadAllRelogininfo(Context ctx) {
        ArrayList<Pair> listData = new DataCache<Pair>().loadGlobal(ctx, USER_RELOGIN_INFO);
        String[] s = new String[listData.size() * 2];
        for (int i = 0; i < listData.size(); ++i) {
            Pair item = listData.get(i);
            s[i * 2] = item.first;
            s[i * 2 + 1] = item.second;
        }

        return s;
    }

    public static void saveBackgrounds(Context ctx, ArrayList<LoginBackground.PhotoItem> data) {
        new DataCache<LoginBackground.PhotoItem>().saveGlobal(ctx, data, BACKGROUNDS);
    }

    public static ArrayList<LoginBackground.PhotoItem> loadBackgrounds(Context ctx) {
        return new DataCache<LoginBackground.PhotoItem>().loadGlobal(ctx, BACKGROUNDS);
    }

    static class DataCache<T> {

        public final static String FILDER_GLOBAL = "FILDER_GLOBAL";

        public void save(Context ctx, ArrayList<T> data, String name) {
            save(ctx, data, name, "");
        }

        public void saveGlobal(Context ctx, Object data, String name) {
            save(ctx, data, name, FILDER_GLOBAL);
        }

        private void deleteFile(File folder, String name) {
            File file = new File(folder, name);
            if (file.exists()) {
                file.delete();
            }
        }

        public void delete(Context ctx, String name) {
            deleteFile(ctx.getFilesDir(), name);
        }

        public void deleteGlobal(Context ctx, String name) {
            File globalFolder = new File(ctx.getFilesDir(), FILDER_GLOBAL);
            if (!globalFolder.exists()) {
                return;
            }

            deleteFile(globalFolder, name);
        }

        private void save(Context ctx, Object data, String name, String folder) {
            if (ctx == null) {
                return;
            }

            File file;
            if (!folder.isEmpty()) {
                File fileDir = new File(ctx.getFilesDir(), folder);
                if (!fileDir.exists() || !fileDir.isDirectory()) {
                    fileDir.mkdir();
                }
                file = new File(fileDir, name);
            } else {
                file = new File(ctx.getFilesDir(), name);
            }

            if (file.exists()) {
                file.delete();
            }

            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(data);
                oos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ArrayList<T> load(Context ctx, String name) {
            return load(ctx, name, "");
        }

        public ArrayList<T> loadGlobal(Context ctx, String name) {
            return load(ctx, name, FILDER_GLOBAL);
        }

        @SuppressWarnings("unchecked")
        public T loadGlobalObject(Context ctx, String name) {
            String folder = FILDER_GLOBAL;
            T data = null;

            File file;
            if (!folder.isEmpty()) {
                File fileDir = new File(ctx.getFilesDir(), folder);
                if (!fileDir.exists() || !fileDir.isDirectory()) {
                    fileDir.mkdir();
                }
                file = new File(fileDir, name);
            } else {
                file = new File(ctx.getFilesDir(), name);
            }

            if (file.exists()) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                    data = (T) ois.readObject();
                    ois.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return data;
        }

        @SuppressWarnings("unchecked")
        private T loadObject(Context ctx, String name, String folder) {
            T data = null;
            File file;
            if (!folder.isEmpty()) {
                File fileDir = new File(ctx.getFilesDir(), folder);
                if (!fileDir.exists() || !fileDir.isDirectory()) {
                    fileDir.mkdir();
                }
                file = new File(fileDir, name);
            } else {
                file = new File(ctx.getFilesDir(), name);
            }

            if (file.exists()) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                    data = (T) ois.readObject();
                    ois.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return data;
        }

        @SuppressWarnings("unchecked")
        private ArrayList<T> load(Context ctx, String name, String folder) {
            ArrayList<T> data = null;

            File file;
            if (!folder.isEmpty()) {
                File fileDir = new File(ctx.getFilesDir(), folder);
                if (!fileDir.exists() || !fileDir.isDirectory()) {
                    fileDir.mkdir();
                }
                file = new File(fileDir, name);
            } else {
                file = new File(ctx.getFilesDir(), name);
            }

            if (file.exists()) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                    data = (ArrayList<T>) ois.readObject();
                    ois.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (data == null) {
                data = new ArrayList<>();
            }

            return data;
        }
    }

    public static class CustomHost implements Serializable {
        private String host = "";
        private String code = "";

        public CustomHost() {
        }

        public CustomHost(String host, String code) {
            this.host = host;
            this.code = code;
        }

        public String getHost() {
            return host;
        }

        public String getCode() {
            return code;
        }
    }

    static class Pair implements Serializable {
        public String first;
        public String second;

        Pair(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }

}
