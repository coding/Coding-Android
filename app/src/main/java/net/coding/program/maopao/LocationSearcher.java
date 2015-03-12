package net.coding.program.maopao;

import android.content.Context;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;

import net.coding.program.model.LocationObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Neutra on 2015/3/12.
 */
public class LocationSearcher {
    // todo: 因不确定能否多次调用，故下面一行代码放在了LocationProvider里保证只初始化一次
    private static boolean sdkInitialized = false;
    private boolean complete = false;
    private LatLng latLng;
    private int page;
    private PoiSearch poiSearch;
    private boolean isSearching = false;
    private int version;
    private int searchingVersion;
    public String keyword = "";

    public void destory() {
        if (poiSearch != null) poiSearch.destroy();
    }

    public LocationSearcher keyword(String keyword) {
        this.keyword = keyword;
        ++version;
        page = 0;
        return this;
    }
    public String keyword() {
        return keyword;
    }

    public void configure(Context context, LatLng latLng, final SearchResultListener listener) {
        isSearching = false;
        complete = false;
        this.latLng = latLng;

        if (!sdkInitialized) {
            SDKInitializer.initialize(context.getApplicationContext());
            sdkInitialized = true;
        }

        destory();
        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                isSearching = false;
                if(searchingVersion != version) {
                    listener.onSearchResult(new ArrayList<LocationObject>());
                } else if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    ++page;
                    List<LocationObject> list = convert(poiResult.getAllPoi());
                    complete = poiResult.getCurrentPageNum() >= poiResult.getTotalPageNum();
                    listener.onSearchResult(list);
                } else {
                    // todo: check searcher error
                    complete = true;
                    listener.onSearchResult(null);
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            }
        });
    }

    public void search() {
        if (isSearching) return;
        isSearching = true;
        searchingVersion = version;
        poiSearch.searchNearby(new PoiNearbySearchOption().keyword(keyword).location(latLng)
                .pageNum(page).pageCapacity(10)
                .radius(1000).sortType(PoiSortType.distance_from_near_to_far));
    }

    public boolean isComplete() {
        return complete;
    }

    private static List<LocationObject> convert(List<PoiInfo> src) {
        List<LocationObject> dest;
        if (src == null || src.size() == 0) {
            dest = new ArrayList<LocationObject>(0);
        } else {
            dest = new ArrayList<LocationObject>(src.size());
            for (PoiInfo item : src) {
                LocationObject converted = convert(item);
                if (converted != null) {
                    dest.add(converted);
                }
            }
        }
        return dest;
    }

    private static LocationObject convert(PoiInfo src) {
        LocationObject locationObject = new LocationObject(src.uid, src.name, src.address);
        if (src.location != null) {
            locationObject.latitude = src.location.latitude;
            locationObject.longitude = src.location.longitude;
        }
        return locationObject;
    }

    public static interface SearchResultListener {
        void onSearchResult(List<LocationObject> locations);
    }
}
