package net.coding.program.maopao;

import android.content.Context;

import com.baidu.mapapi.model.LatLng;

import net.coding.program.model.LocationObject;

import java.util.List;

/**
 * Created by Neutra on 2015/3/12.
 */
public class LocationSearcherGroup {
    private PublicLocationSearcher[] publicSearchers;
    private PrivateLocationSearcher privateSearcher;
    private LocationSearcher.SearchResultListener listener;
    private LocationSearcher.SearchResultListener itemListener = new PublicLocationSearcher.SearchResultListener() {
        @Override
        public void onSearchResult(List<LocationObject> locations) {
            listener.onSearchResult(locations);
        }
    };

    public LocationSearcherGroup(String[] keywords) {
        if (keywords == null || keywords.length < 1) throw new IllegalArgumentException("keywords");
        publicSearchers = new PublicLocationSearcher[keywords.length];
        for (int i = keywords.length - 1; i >= 0; --i) {
            publicSearchers[i] = new PublicLocationSearcher();
            publicSearchers[i].setKeyword(keywords[i]);
        }
        privateSearcher = new PrivateLocationSearcher();
        privateSearcher.setKeyword("");
    }

    public LocationSearcherGroup() {
        publicSearchers = new PublicLocationSearcher[]{new PublicLocationSearcher()};
        privateSearcher = new PrivateLocationSearcher();
    }

    public synchronized void destory() {
        privateSearcher.destory();
        for (LocationSearcher searcher : publicSearchers) {
            searcher.destory();
        }
    }

    public synchronized void configure(Context context, LatLng latLng, final PublicLocationSearcher.SearchResultListener listener) {
        this.listener = listener;
        privateSearcher.configure(context, latLng, itemListener);
        for (LocationSearcher item : publicSearchers) {
            item.configure(context, latLng, itemListener);
        }
    }

    public synchronized void search() {
        if (listener == null) return;
        privateSearcher.search();
        for (LocationSearcher item : publicSearchers) {
            item.search();
        }
    }

    public synchronized boolean isComplete() {
        for (LocationSearcher searcher : publicSearchers) {
            if (!searcher.isComplete()) {
                return false;
            }
        }
        return privateSearcher.isComplete();
    }

    public synchronized void setKeyword(String keyword) {
        for (LocationSearcher searcher : publicSearchers) {
            searcher.setKeyword(keyword);
        }
        privateSearcher.setKeyword(keyword);
    }

    public String getKeyword() {
        return privateSearcher.getKeyword();
    }

    public boolean isKeywordEmpty() {
        return privateSearcher.isKeywordEmpty();
    }
}
