package net.coding.program.maopao;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;

import net.coding.program.model.LocationObject;

import java.util.List;

/**
 * Created by Neutra on 2015/3/14.
 */
public abstract class LocationSearcher {
    private boolean isSearching = false;
    private boolean isComplete = false;
    private int keywordVersion;
    private int searchingVersion;
    private int page;
    private String keyword = "";

    public String getKeyword() {
        return keyword;
    }
    public boolean isKeywordEmpty() {
        return TextUtils.isEmpty(keyword);
    }

    public void setKeyword(String keyword) {
        keyword = keyword == null? "": keyword.toString();
        Log.e("LocationSearcher",  String.format("setKeyword %s => %s", this.keyword, keyword));
        if(!keyword.equals(this.keyword)) {
            this.keyword = keyword;
            ++keywordVersion;
            page = 0;
            isSearching = false;
            isComplete = false;
            Log.e("LocationSearcher",  String.format("setKeyword reset states ,keywordVersion = %d", keywordVersion));
        }
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void search() {
        if (isComplete || isSearching) return;
        isSearching = true;
        searchingVersion = keywordVersion;
        Log.e("LocationSearcher",  String.format("search() searchingVersion = %d", searchingVersion));
        doSearch(page);
    }

    protected boolean shouldSkipThisResult(){
        isSearching = false;
        return searchingVersion != keywordVersion;
    }

    public void configure(Context context, LatLng latLng, final SearchResultListener listener){
        isSearching = false;
        isComplete = false;
        page = 0;
        doConfigure(context, latLng, listener);
    }

    protected void scheduleNextPage(){
        ++page;
    }

    protected void setComplete(boolean isComplete){
        this.isComplete = isComplete;
    }

    protected abstract void doConfigure(Context context, LatLng latLng, SearchResultListener listener);
    protected abstract void doSearch(int page);
    public abstract void destory() ;

    public static interface SearchResultListener {
        void onSearchResult(List<LocationObject> locations);
    }
}
