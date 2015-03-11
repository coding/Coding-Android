package net.coding.program.maopao;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
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

/**
 * Created by Neutra on 2015/3/11.
 */
public class LocationProvider {

    private LocationClient locationClient;

    private LocationProvider(Context context) {
        SDKInitializer.initialize(context.getApplicationContext());
        locationClient = new LocationClient(context.getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
        option.setIsNeedAddress(true);//返回的定位结果需要包含地址信息
        option.setAddrType("all");
        option.setLocationNotify(false);
        option.setNeedDeviceDirect(false);//返回的定位结果不需要包含手机机头的方向
        locationClient.setLocOption(option);
    }

    private static LocationProvider instance;

    public static LocationProvider getInstance(Context context) {
        if (instance != null) return instance;
        instance = new LocationProvider(context);
        return instance;
    }

    public void requestLocation(final LocationResultListener listener) {
        requestLocation(listener, 0);
    }

    public void requestLocation(final LocationResultListener listener, final int retry) {
        locationClient.start();
        locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                locationClient.unRegisterLocationListener(this);
                String city = bdLocation.getCity();
                boolean success = true;
                if (TextUtils.isEmpty(city)) {
                    city = null;
                    success = false;
                    if (retry < 3) {
                        Log.e("AAA", "request location failure retry " + retry + " times");
                        requestLocation(listener, retry + 1);
                        return;
                    }
                } else {
                    // 大多数情况下，将"广州市"直接显示成"广州"
                    city = city.replaceFirst("市$", "");
                }
                locationClient.stop();
                listener.onLocationResult(success, city, bdLocation.getLatitude(), bdLocation.getLongitude());
            }
        });
        // 0：正常发起了定位。
        // 1：服务没有启动。
        // 2：没有监听函数。
        // 6：请求间隔过短。 前后两次请求定位时间间隔不能小于1000ms。
        int code = locationClient.requestLocation();
        if (code == 6) {
            locationClient.requestOfflineLocation();
        }
    }

    public static interface LocationResultListener {
        void onLocationResult(boolean success, String city, double latitude, double longitude);
    }

    public void requestNearbyLocations(String keyword, double latitude, double longitude,
                                       int page, final NearbyLocationsResultListener listener) {
        final PoiSearch search = PoiSearch.newInstance();
        final LatLng location = new LatLng(latitude, longitude);
        search.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (poiResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    listener.onNearbyLocationsResult(false, new ArrayList<LocationObject>(0), -1);
                } else {
                    List<LocationObject> list = convert(poiResult.getAllPoi());
                    int nextPage = poiResult.getCurrentPageNum() < poiResult.getTotalPageNum()
                            ? poiResult.getCurrentPageNum() : -1;
                    listener.onNearbyLocationsResult(true, list, nextPage);
                }
                search.destroy();
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            }
        });
        search.searchNearby(new PoiNearbySearchOption().keyword(keyword).location(location)
                .pageCapacity(20).pageNum(page).radius(50000).sortType(PoiSortType.distance_from_near_to_far));
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

    public static interface NearbyLocationsResultListener {
        void onNearbyLocationsResult(boolean success, List<LocationObject> locations, int nextPage);
    }
}
