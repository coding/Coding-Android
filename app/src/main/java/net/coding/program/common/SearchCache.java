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
    public static final int SERACH_CACHE_COUNT = 3;

    private List<String> searchCacheList = null;
    private Context mContext;

    private static SearchCache mInstance = null;
    private static Object mSyncObject = new Object();

    private SearchCache(Context context) {
        mContext = context;
        if (searchCacheList == null) {
            SharedPreferences preferences = mContext.getSharedPreferences(SEARCH_CACHE, Context.MODE_PRIVATE);
            LinkedHashSet<String> keySet = (LinkedHashSet<String>) preferences.getStringSet(SEARCH_CACHE_KEY, new LinkedHashSet<String>());
            searchCacheList = new ArrayList<String>();
            for (String key : keySet) {
                searchCacheList.add(key);
            }
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
            saveDataToCache();
        }
    }

    public void remove(String searchKey) {
        if (searchCacheList != null && !TextUtils.isEmpty(searchKey)) {
            searchCacheList.remove(searchKey);
            saveDataToCache();
        }
    }

    public List<String> getSearchCacheList() {
        return searchCacheList;
    }

    public void clearCache() {
        searchCacheList.clear();
        saveDataToCache();
    }

    private void saveDataToCache() {
        LinkedHashSet<String> hashSet = new LinkedHashSet<String>();
        for (String key : searchCacheList) {
            hashSet.add(key);
        }
        SharedPreferences.Editor edit = mContext.getSharedPreferences(SEARCH_CACHE, Context.MODE_PRIVATE).edit();
        edit.putStringSet(SEARCH_CACHE_KEY, hashSet);
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

}
