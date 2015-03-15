package net.coding.program.maopao;

import android.content.Context;

import com.baidu.mapapi.model.LatLng;

import net.coding.program.model.LocationObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
            if (locations != null) resultBuffer.addAll(locations);
            if (!isSearching()) {
                // onSearchResult后可能立刻就会重新搜索， 所以要输出的列表放到另一个对象中
                List<LocationObject> temp = new ArrayList<>(resultBuffer);
                resultBuffer.clear();
                Collections.sort(temp, new Comparator<LocationObject>() {
                    @Override
                    public int compare(LocationObject lhs, LocationObject rhs) {
                        return lhs.distance - rhs.distance;
                    }
                });
                listener.onSearchResult(temp);
            }
        }
    };

    private List<LocationObject> resultBuffer = new ArrayList<>();

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
        resultBuffer.clear();
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
        if (isSearching()) return;
        resultBuffer.clear();
        privateSearcher.search();
        for (LocationSearcher item : publicSearchers) {
            item.search();
        }
    }

    public synchronized boolean isSearching() {
        for (LocationSearcher searcher : publicSearchers) {
            if (!searcher.isSearching()) {
                return false;
            }
        }
        return privateSearcher.isSearching();
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
