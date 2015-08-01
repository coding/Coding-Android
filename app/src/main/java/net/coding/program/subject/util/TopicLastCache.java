package net.coding.program.subject.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15-7-24.
 *
 * 最近使用的话题缓存
 */
public class TopicLastCache {

    private static final String TOPIC_CACHE = TopicLastCache.class.getName() + "_search_cache";
    private static final String TOPIC_CACHE_KEY = TopicLastCache.class.getName() + "_search_cache_key";
    private static final String TOPIC_CACHE_SIZE = TopicLastCache.class.getName() + "_search_cache_size";
    public static final int TOPIC_CACHE_COUNT = 3;

    private List<String> topicLastCacheList = null;
    private Context mContext;

    private static TopicLastCache mInstance = null;
    private static Object mSyncObject = new Object();

    private TopicLastCache(Context context) {
        mContext = context;
        if (topicLastCacheList == null) {
            loadCache();
        }
    }

    public void add(String topic) {
        if (topicLastCacheList != null && !TextUtils.isEmpty(topic)) {
            if (!topicLastCacheList.contains(topic))
                topicLastCacheList.add(0, topic);
            else {
                topicLastCacheList.remove(topic);
                topicLastCacheList.add(0, topic);
            }
            if (topicLastCacheList.size() > TOPIC_CACHE_COUNT) {
                topicLastCacheList.remove(TOPIC_CACHE_COUNT);
            }
            saveCache();
        }
    }

    public void remove(String searchKey) {
        if (topicLastCacheList != null && !TextUtils.isEmpty(searchKey)) {
            topicLastCacheList.remove(searchKey);
            saveCache();
        }
    }

    public List<String> getTopicLastCacheList() {
        return topicLastCacheList;
    }

    public void clearCache() {
        topicLastCacheList.clear();
        saveCache();
    }

    private void saveCache() {
        SharedPreferences.Editor edit = mContext.getSharedPreferences(TOPIC_CACHE, Context.MODE_PRIVATE).edit();
        edit.putInt(TOPIC_CACHE_SIZE, topicLastCacheList.size());
        for (int i = 0; i < topicLastCacheList.size(); i++) {
            edit.putString(TOPIC_CACHE_KEY + "_" + i, topicLastCacheList.get(i));
        }
        edit.commit();
    }


    public static TopicLastCache getInstance(Context context) {
        if (context != null) {
            if (mInstance == null) {
                synchronized (mSyncObject) {
                    if (mInstance == null) {
                        mInstance = new TopicLastCache(context);
                    }
                }
            }
        }
        return mInstance;
    }

    private void loadCache() {
        if (topicLastCacheList == null)
            topicLastCacheList = new ArrayList<String>();
        topicLastCacheList.clear();
        SharedPreferences preferences = mContext.getSharedPreferences(TOPIC_CACHE, Context.MODE_PRIVATE);
        int size = preferences.getInt(TOPIC_CACHE_SIZE, 0);
        for (int i = 0; i < size; i++) {
            topicLastCacheList.add(preferences.getString(TOPIC_CACHE_KEY + "_" + i, ""));
        }
    }
}
