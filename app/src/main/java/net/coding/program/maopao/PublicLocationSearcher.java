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

import net.coding.program.model.LocationObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Neutra on 2015/3/12.
 */
public class PublicLocationSearcher extends LocationSearcher {
    protected LatLng latLng;
    private PoiSearch poiSearch;

    @Override
    public void destory() {
        if (poiSearch != null) poiSearch.destroy();
    }

    @Override
    protected void doConfigure(Context context, LatLng latLng, final LocationSearcher.SearchResultListener listener) {
        this.latLng = latLng;
        destory();
        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if(shouldSkipThisResult()) {
                    return;
                }
                if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    scheduleNextPage();
                    List<LocationObject> list = convert(poiResult.getAllPoi());
                    setComplete(poiResult.getCurrentPageNum() >= poiResult.getTotalPageNum());
                    listener.onSearchResult(list);
                } else {
                    // todo: check what happen
                    setComplete(true);
                    listener.onSearchResult(null);
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            }
        });
    }

    public void doSearch(int page) {
        poiSearch.searchNearby(new PoiNearbySearchOption()
                .keyword(getKeyword()).location(latLng)
                .pageNum(page).pageCapacity(10)
                .radius(1000).sortType(PoiSortType.distance_from_near_to_far));
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
}
