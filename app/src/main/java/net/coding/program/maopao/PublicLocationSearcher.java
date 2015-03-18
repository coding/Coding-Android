package net.coding.program.maopao;

import android.content.Context;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.utils.DistanceUtil;

import net.coding.program.model.LocationObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Neutra on 2015/3/12.
 */
public class PublicLocationSearcher extends LocationSearcher {
    private Context context;
    private double latitude, longitude;
    private BaiduLbsLoader.LbsResultListener callback;

    @Override
    protected void doConfigure(Context context, LatLng latLng, final SearchResultListener listener) {
        this.context = context;
        latitude = latLng == null ? 0 : latLng.latitude;
        longitude = latLng == null ? 0 : latLng.longitude;

        this.callback = new BaiduLbsLoader.LbsResultListener() {
            @Override
            public void onSearchResult(boolean success, List<LocationObject> list, boolean hasMore) {
                if (shouldSkipThisResult()) return;
                if (!success) {
                    setComplete(true);
                    listener.onSearchResult(null);
                    return;
                }
                setComplete(!hasMore);
                scheduleNextPage();
                listener.onSearchResult(list);
            }
        };
    }

    protected void doSearch(int page) {
        BaiduLbsLoader.searchPublic(context, getKeyword(), latitude, longitude, page, callback);
    }

    @Override
    public void destory() {
        callback = null;
    }
}
