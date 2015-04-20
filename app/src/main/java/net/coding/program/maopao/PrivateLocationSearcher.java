package net.coding.program.maopao;

import android.content.Context;

import com.baidu.mapapi.model.LatLng;

import net.coding.program.model.LocationObject;

import java.util.List;

/**
 * Created by Neutra on 2015/3/14.
 */
public class PrivateLocationSearcher extends LocationSearcher {

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
        BaiduLbsLoader.searchCustom(context, getKeyword(), latitude, longitude, page, callback);
    }

    @Override
    public void destory() {
        callback = null;
    }
}
