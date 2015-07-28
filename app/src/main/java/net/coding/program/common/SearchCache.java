package net.coding.program.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by david on 15-7-22.
 * <p/>
 * 冒泡搜索的缓存数据类
 */
public class SearchCache {

    private static final String SEARCH_CACHE = SearchCache.class.getName() + "_search_cache";
    private static final String SEARCH_CACHE_KEY = SearchCache.class.getName() + "_search_cache_key";
    private static final String SEARCH_CACHE_SIZE = SearchCache.class.getName() + "_search_cache_size";
    public static final int SERACH_CACHE_COUNT = 8;

    private List<String> searchCacheList = null;
    private Context mContext;

    private static SearchCache mInstance = null;
    private static Object mSyncObject = new Object();

    private SearchCache(Context context) {
        mContext = context;
        if (searchCacheList == null) {
           loadCache();
        }
    }

    public void add(String searchKey) {
        if (searchCacheList != null && !TextUtils.isEmpty(searchKey)) {
            if (!searchCacheList.contains(searchKey))
                searchCacheList.add(0, searchKey);
            else {
                searchCacheList.remove(searchKey);
                searchCacheList.add(0, searchKey);
            }
            if (searchCacheList.size() > SERACH_CACHE_COUNT) {
                searchCacheList.remove(SERACH_CACHE_COUNT);
            }
            saveCache();
        }
    }

    public void remove(String searchKey) {
        if (searchCacheList != null && !TextUtils.isEmpty(searchKey)) {
            searchCacheList.remove(searchKey);
            saveCache();
        }
    }

    public List<String> getSearchCacheList() {
        return searchCacheList;
    }

    public void clearCache() {
        searchCacheList.clear();
        saveCache();
    }

    private void saveCache() {
        SharedPreferences.Editor edit = mContext.getSharedPreferences(SEARCH_CACHE, Context.MODE_PRIVATE).edit();
        edit.putInt(SEARCH_CACHE_SIZE, searchCacheList.size());
        for (int i = 0; i < searchCacheList.size(); i++) {
            edit.putString(SEARCH_CACHE_KEY + "_" + i, searchCacheList.get(i));
        }
        edit.commit();
    }


    public static SearchCache getInstance(Context context) {
        if (context != null) {
            if (mInstance == null) {
                synchronized (mSyncObject) {
                    if (mInstance == null) {
                        mInstance = new SearchCache(context);
                    }
                }
            }
        }
        return mInstance;
    }

    private void loadCache() {
        if (searchCacheList == null)
            searchCacheList = new ArrayList<String>();
        searchCacheList.clear();
        SharedPreferences preferences = mContext.getSharedPreferences(SEARCH_CACHE, Context.MODE_PRIVATE);
        int size = preferences.getInt(SEARCH_CACHE_SIZE, 0);
        for (int i = 0; i < size; i++) {
            searchCacheList.add(preferences.getString(SEARCH_CACHE_KEY + "_" + i, ""));
        }
    }

}
