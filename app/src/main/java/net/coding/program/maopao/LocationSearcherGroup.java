package net.coding.program.maopao;

import android.content.Context;

import com.baidu.mapapi.model.LatLng;

import net.coding.program.model.LocationObject;

import java.util.List;

/**
 * Created by Neutra on 2015/3/12.
 */
public class LocationSearcherGroup {
    private LocationSearcher[] searchers;
    private LocationSearcher.SearchResultListener listener;
    private LocationSearcher.SearchResultListener itemListener = new LocationSearcher.SearchResultListener() {
        @Override
        public void onSearchResult(List<LocationObject> locations) {
            if (locations != null) {
                listener.onSearchResult(locations);
            }
        }
    };

    public LocationSearcherGroup() {
        // 行政区划,房地产,公司企业,美食,休闲娱乐,宾馆,购物,旅游景点,生活服务,汽车服务,结婚,丽人,金融,运动健身,医疗,教育,培训机构,交通设施,自然地物,政府机构,门址,道路
        this("行政区划,房地产,公司企业,美食,休闲娱乐,宾馆,购物,旅游景点,生活服务,汽车服务,自然地物,丽人,金融,运动健身,医疗,教育,交通设施,政府机构,道路".split(","));
    }

    private LocationSearcherGroup(String[] keywords) {
        if (keywords == null || keywords.length < 1) throw new IllegalArgumentException("keywords");
        searchers = new LocationSearcher[keywords.length];
        for (int i = searchers.length - 1; i >= 0; --i) {
            searchers[i] = new LocationSearcher().keyword(keywords[i]);
        }
    }

    public synchronized void destory() {
        for (LocationSearcher searcher : searchers) {
            searcher.destory();
        }
    }

    public synchronized void configure(Context context, LatLng latLng, final LocationSearcher.SearchResultListener listener) {
        this.listener = listener;
        for (int i = searchers.length - 1; i >= 0; --i) {
            LocationSearcher searcher = searchers[i];
            searcher.configure(context, latLng, itemListener);
        }
    }

    public synchronized void search() {
        if (listener == null) return;
        for (int i = searchers.length - 1; i >= 0; --i) {
            searchers[i].search();
        }
    }

    public synchronized boolean isComplete() {
        for (int i = searchers.length - 1; i >= 0; --i) {
            if (!searchers[i].isComplete()) {
                return false;
            }
        }
        return true;
    }

}
