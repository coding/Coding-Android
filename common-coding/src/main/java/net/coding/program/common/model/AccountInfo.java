package net.coding.program.common.model;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.loopj.android.http.PersistentCookieStore;

import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.LoginBackground;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.model.topic.TopicData;
import net.coding.program.common.module.maopao.MaopaoDraft;
import net.coding.program.network.constant.Friend;
import net.coding.program.network.model.wiki.WikiDraft;

import org.json.JSONArray;
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

    private static final String GLOBAL_MARKED_MARKETING = "GLOBAL_MARKED_MARKETING";

    private static final String USER_MAOPAO = "USER_MAOPAO";
    private static final String MAOPAO_DRAFT = "MAOPAO_DRAFT";
    private static final String USER_RELOGIN_INFO = "USER_RELOGIN_INFO2"; // 修改了数据类型，由Pair改为UserObject
    // 上次成功登录时用户输入的用户名，可能是邮箱或个性后缀
    private static final String GLOBAL_LAST_LOGIN_NAME = "GLOBAL_LAST_LOGIN_NAME";
    private static final String GLOBAL_LAST_COMPANY_NAME = "GLOBAL_LAST_COMPANY_NAME";
    private static final String USER_TASK_PROJECTS = "USER_TASK_PROJECTS";
    private static final String USER_TASKS = "USER_TASKS_%d_%d";
    private static final String USER_NO_SEND_MESSAGE = "USER_NO_SEND_MESSAGE";
    private static final String BACKGROUNDS = "BACKGROUNDS";
    private static final String ACCOUNT_SETTING = "FILE_PUSH";
    private static final String KEY_NEED_PUSH = "KEY_NEED_PUSH";
    private static final String KEY_ENTERPRISE = "KEY_ENTERPRISE";

    private static final String KEY_CUSTOM_HOST = "KEY_CUSTOM_HOST";
    private static final String KEY_MAOPAO_BANNER = "KEY_MAOPAO_BANNER";
    private static final String KEY_MALL_BANNER = "KEY_MALL_BANNER";

    private static final String KEY_SEARCH_PROJECT_HISTORY = "KEY_SEARCH_PROJECT_HISTORY";

    private static final String KEY_CACHE_GET_REQUEST = "KEY_CACHE_GET_REQUEST";

    private static final String GLOBAL_LAST_PRIVATE_HOST = "GLOBAL_LAST_PRIVATE_HOST";

    // 每添加一个
    private static final String MARK_GUIDE_FEATURES = "MARK_GUIDE_500"; // 修改这个值就可以了

    // 兼容网站的偷懒逻辑
    private static final String HIDE_BOARD_IDS = "HIDE_BOARD_IDS"; // 是否显示初始化界面由本地保存 (⊙ˍ⊙)

    public static void loginOut(Context ctx) {
        File dir = ctx.getFilesDir();
        String[] fileNameList = dir.list();
        for (String item : fileNameList) {
            File file = new File(dir, item);
            if (file.exists() && !file.isDirectory()) {
                file.delete();
            }
        }

        clearCookie(ctx);

        NotificationManager notificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void clearCookie(Context ctx) {
        PersistentCookieStore cookieStore = new PersistentCookieStore(ctx);
        cookieStore.clear();
    }

    public static void saveAccount(Context ctx, UserObject data) {
        GlobalData.sUserObject = data;
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

    public static JSONObject getGetRequestCacheData(Context context, String request) {
        JSONObject json = getGetRequestCache(context, request);
        if (json.has("data")) {
            return json.optJSONObject("data");
        }

        return json;
    }


    public static JSONArray getGetRequestCacheListData(Context context, String request) {
        JSONObject json = getGetRequestCache(context, request);
        if (json.has("data")) {
            return json.optJSONArray("data");
        }

        return new JSONArray();
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
        // 4.0 特有, 下个版本删除, MARK_GUIDE_FEATURES 一定要修改
        return false;

//        Boolean result = new DataCache<Boolean>().loadGlobalObject(context, MARK_GUIDE_FEATURES);
//        if (result == null) {
//            return true;
//        }
//
//        return result;
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

    public static void saveFriends(Context ctx, ArrayList<UserObject> data, Friend type) {
        String name = friendTypeToFileName(type);
        new DataCache<UserObject>().save(ctx, data, name);
    }

    public static ArrayList<UserObject> loadFriends(Context ctx, Friend type) {
        String fileName = friendTypeToFileName(type);
        return new DataCache<UserObject>().load(ctx, fileName);
    }

    private static String friendTypeToFileName(Friend type) {
        if (type == Friend.Follow) {
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


    public static void saveSearchProjectHistory(Context context, ArrayList<String> data) {
        new DataCache<String>().saveGlobal(context, data, KEY_SEARCH_PROJECT_HISTORY);
    }

    public static ArrayList<String> loadSearchProjectHistory(Context context) {
        return new DataCache<String>().loadGlobal(context, KEY_SEARCH_PROJECT_HISTORY);
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
            if (item.length >= 2 &&
                    (item[1].equals("Coding") || item[1].equals("CodingEnterprise")) &&
                    item[0].equals(globalKey)) {
                return uriString;
            }
        }

        return "";
    }

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


    public static void setNeedPush(Context ctx, boolean push) {
        SharedPreferences sp = ctx.getSharedPreferences(ACCOUNT_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_NEED_PUSH, push);
        editor.apply();
    }

    public static void setCheckLoginBackground(Context ctx) {
        Calendar calendar = Calendar.getInstance();
        SharedPreferences.Editor editor = ctx.getSharedPreferences(GLOBAL_SETTING, Context.MODE_PRIVATE).edit();
        editor.putLong(GLOBAL_SETTING_BACKGROUND, calendar.getTimeInMillis());
        editor.apply();
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

    public static void saveMaopaoDraft(Context ctx, MaopaoDraft draft) {
        if (draft.isEmpty()) {
            new DataCache<MaopaoDraft>().delete(ctx, MAOPAO_DRAFT);
        } else {
            ArrayList<MaopaoDraft> data = new ArrayList<>();
            data.add(draft);
            new DataCache<MaopaoDraft>().save(ctx, data, MAOPAO_DRAFT);
        }
    }

    private enum DraftType {
        topic, wiki
    }

    @NonNull
    private static String createSaveName(DraftType type, String projectPath, int topicId) {
        return SimpleSHA1.sha1(type.name() + projectPath + topicId);
    }

    public static void saveTopicDraft(Context ctx, TopicData draft, String projectPath, int topicId) {
        ArrayList<TopicData> data = new ArrayList<>();
        data.add(draft);
        new DataCache<TopicData>().save(ctx, data, createSaveName(DraftType.topic, projectPath, topicId));
    }

    public static ArrayList<TopicData> loadTopicDraft(Context ctx, String projectPath, int topicId) {
        return new DataCache<TopicData>().load(ctx, createSaveName(DraftType.topic, projectPath, topicId));
    }

    public static void deleteTopicDraft(Context ctx, String projectPath, int topicId) {
        new DataCache<TopicData>().delete(ctx, createSaveName(DraftType.topic, projectPath, topicId));
    }

    public static void saveWikiDraft(Context ctx, WikiDraft draft, String projectPath, int wikiId) {
        ArrayList<WikiDraft> data = new ArrayList<>();
        data.add(draft);
        new DataCache<WikiDraft>().save(ctx, data, createSaveName(DraftType.wiki, projectPath, wikiId));
    }

    public static ArrayList<WikiDraft> loadWikiDraft(Context ctx, String projectPath, int wikiId) {
        return new DataCache<WikiDraft>().load(ctx, createSaveName(DraftType.wiki, projectPath, wikiId));
    }

    public static void deleteWikiDraft(Context ctx, String projectPath, int wikiId) {
        new DataCache<WikiDraft>().delete(ctx, createSaveName(DraftType.wiki, projectPath, wikiId));
    }

    public static MaopaoDraft loadMaopaoDraft(Context ctx) {
        ArrayList<MaopaoDraft> data = new DataCache<MaopaoDraft>().load(ctx, MAOPAO_DRAFT);
        if (data.isEmpty()) {
            return new MaopaoDraft();
        } else {
            return data.get(0);
        }
    }

    public static void saveReloginInfo(Context ctx, UserObject user) {
        DataCache<UserObject> dateCache = new DataCache<>();
        ArrayList<UserObject> listData = dateCache.loadGlobal(ctx, USER_RELOGIN_INFO);
        for (int i = 0; i < listData.size(); ++i) {
            if (listData.get(i).global_key.equals(user.global_key)) {
                listData.remove(i);
                --i;
            }
        }

        listData.add(user);
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

    public static void saveLastCompanyName(Context context, String name) {
        new DataCache<String>().saveGlobal(context, name, GLOBAL_LAST_COMPANY_NAME);
    }

    public static String loadLastCompanyName(Context context) {
        String s = new DataCache<String>().loadGlobalObject(context, GLOBAL_LAST_COMPANY_NAME);
        if (s == null) {
            return "";
        }

        return s;
    }

    public static void saveLastPrivateHost(Context context, String host) {
        if (!host.startsWith("http://") && !host.startsWith("https://")) {
            host = "http://" + host;
        }

        new DataCache<String>().saveGlobal(context, host, GLOBAL_LAST_PRIVATE_HOST);
    }

    public static String loadLastPrivateHost(Context context) {
        String host = new DataCache<String>().loadGlobalObject(context, GLOBAL_LAST_PRIVATE_HOST);
        if (host == null) {
            return "";
        }

        if (!host.startsWith("http://") && !host.startsWith("https://")) {
            host = "http://" + host;
        }

        return host;
    }

    public static ArrayList<ProjectObject> loadTaskProjects(Context context) {
        return new DataCache<ProjectObject>().load(context, USER_TASK_PROJECTS);
    }

    public static void saveHideInitBoard(Context context, Integer boardId) {
        ArrayList<Integer> ids = new DataCache<Integer>().load(context, HIDE_BOARD_IDS);
        for (Integer item : ids) {
            if (item.equals(boardId)) {
                return;
            }
        }

        ids.add(boardId);
        new DataCache<Integer>().save(context, ids, HIDE_BOARD_IDS);
    }

    public static boolean hideInitBoard(Context context, Integer boardId) {
        ArrayList<Integer> ids = new DataCache<Integer>().load(context, HIDE_BOARD_IDS);
        for (Integer item : ids) {
            if (item.equals(boardId)) {
                return true;
            }
        }

        return false;
    }

    public static void removehideInitBoard(Context context, Integer boardId) {
        ArrayList<Integer> ids = new DataCache<Integer>().load(context, HIDE_BOARD_IDS);
        for (Integer item : ids) {
            if (item.equals(boardId)) {
                ids.remove(item);
            }
        }

        new DataCache<Integer>().save(context, ids, HIDE_BOARD_IDS);
    }


    public static void saveTasks(Context context, ArrayList<SingleTask> data, int projectId, int userId) {
        new DataCache<SingleTask>().save(context, data, String.format(USER_TASKS, projectId, userId));
    }

    public static ArrayList<SingleTask> loadTasks(Context context, int projectId, int userId) {
        return new DataCache<SingleTask>().load(context, String.format(USER_TASKS, projectId, userId));
    }

    public static void saveNoSendMessage(Context context, MyMessage message) {
        ArrayList<MyMessage> allMessages = loadNoSendMessage(context);
        allMessages.add(message);
        new DataCache<MyMessage>().save(context, allMessages, USER_NO_SEND_MESSAGE);
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

    public static void saveMallBanners(Context context, ArrayList<MallBannerObject> data) {
        new DataCache<MallBannerObject>().saveGlobal(context, data, KEY_MALL_BANNER);
    }

    public static ArrayList<MallBannerObject> getMallBanners(Context context) {
        return new DataCache<MallBannerObject>().loadGlobal(context, KEY_MALL_BANNER);
    }

    public static void removeNoSendMessage(Context context, long createTime) {
        ArrayList<MyMessage> allMessages = loadNoSendMessage(context);
        for (int i = 0; i < allMessages.size(); ++i) {
            MyMessage item = allMessages.get(i);
            if (item.getCreateTime() == createTime) {
                allMessages.remove(i);
                break;
            }
        }

        new DataCache<MyMessage>().save(context, allMessages, USER_NO_SEND_MESSAGE);
    }

    public static ArrayList<MyMessage> loadNoSendMessage(Context context, String globalKey) {
        ArrayList<MyMessage> allMessages = loadNoSendMessage(context);
        ArrayList<MyMessage> messages = new ArrayList<>();
        for (MyMessage item : allMessages) {
            if (item.friend.global_key.equals(globalKey)) {
                messages.add(item);
            }
        }

        return messages;
    }

    public static ArrayList<MyMessage> loadNoSendMessage(Context context) {
        return new DataCache<MyMessage>().load(context, USER_NO_SEND_MESSAGE);
    }

    public static String loadRelogininfo(Context ctx, String key) {
        ArrayList<UserObject> listData = new DataCache<UserObject>().loadGlobal(ctx, USER_RELOGIN_INFO);
        for (UserObject item : listData) {
            if (item.email.equals(key)
                    || item.phone.equals(key)
                    || item.global_key.equals(key)) {
                return item.global_key;
            }
        }
        return "";
    }

    public static String[] loadAllRelogininfo(Context ctx) {
        ArrayList<UserObject> listData = new DataCache<UserObject>().loadGlobal(ctx, USER_RELOGIN_INFO);
        ArrayList<String> array = new ArrayList<>();
        for (UserObject item : listData) {
            array.add(item.email);
            array.add(item.phone);
            array.add(item.global_key);
        }
        String[] data = new String[array.size()];
        return array.toArray(data);
    }

    public static MarkedMarketingData loadGlobalMarkedMarketing(Context context) {
        String global_key = GlobalData.sUserObject.global_key;

        ArrayList<MarkedMarketingData> allUser = new DataCache<MarkedMarketingData>().load(context, GLOBAL_MARKED_MARKETING);
        for (MarkedMarketingData item : allUser) {
            if (item.mGlobalKey.equals(global_key)) {
                return item;
            }
        }

        return new MarkedMarketingData(global_key);
    }

    public static void saveGlobalMarkedMarketing(Context context, MarkedMarketingData data) {
        ArrayList<MarkedMarketingData> allUser = new DataCache<MarkedMarketingData>().load(context, GLOBAL_MARKED_MARKETING);
        boolean find = false;
        for (int i = 0; i < allUser.size(); ++i) {
            MarkedMarketingData item = allUser.get(i);
            if (data.mGlobalKey.equals(item.mGlobalKey)) {
                find = true;
                item.mReadData = data.mReadData;
                break;
            }
        }

        if (!find) {
            allUser.add(data);
        }

        new DataCache<MarkedMarketingData>().save(context, allUser, GLOBAL_MARKED_MARKETING);
    }

    public static void saveBackgrounds(Context ctx, ArrayList<LoginBackground.PhotoItem> data) {
        new DataCache<LoginBackground.PhotoItem>().saveGlobal(ctx, data, BACKGROUNDS);
    }

    public static ArrayList<LoginBackground.PhotoItem> loadBackgrounds(Context ctx) {
        return new DataCache<LoginBackground.PhotoItem>().loadGlobal(ctx, BACKGROUNDS);
    }

    protected static class DataCache<T> {

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
}
