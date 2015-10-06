package net.coding.program.maopao;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.maopao.item.LocationItem;
import net.coding.program.model.LocationObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Neutra on 2015/3/11.
 */
@EActivity(R.layout.activity_choose_location)
@OptionsMenu(R.menu.location_search)
public class LocationSearchActivity extends BackActivity implements FootUpdate.LoadMore {

    // 行政区划,房地产,公司企业,美食,休闲娱乐,宾馆,购物,旅游景点,生活服务,汽车服务,结婚,丽人,金融,运动健身,医疗,教育,培训机构,交通设施,自然地物,政府机构,门址,道路
    private static final String RECOMMEND_KEYS = "美食$休闲娱乐$公司企业$旅游景点$道路$宾馆$生活服务$医疗";
    static public int sSearchPos = 0;
    @ViewById
    ListView listView;
    @Extra
    LocationObject selectedLocation;
    private ChooseAdapter chooseAdapter;
    private SearchAdapter searchAdapter;
    private LocationProvider locationProvider;
    private String currentCity = null;
    private String currentArea = null;
    private double latitude, longitude;
    private boolean isLoadingLocation = false;
    private SearchView searchView;
    private LocationObject emptyLocation = LocationObject.undefined();

    public static int getSearchPos() {
        return sSearchPos;
    }

    @AfterViews
    protected final void initLocationSearchActivity() {
        mFootUpdate.init(listView, mInflater, this);
        chooseAdapter = new ChooseAdapter();
        searchAdapter = new SearchAdapter();
        listView.setAdapter(chooseAdapter);

        reset();
        loadMore();
    }

    @Override
    public void loadMore() {
        mFootUpdate.showLoading();
        if (listView.getAdapter() == searchAdapter) {
            searchAdapter.loadMore();
        } else {
            chooseAdapter.loadMore();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chooseAdapter != null && chooseAdapter.searcher != null) {
            chooseAdapter.searcher.destory();
        }
        if (searchAdapter != null && searchAdapter.searcher != null) {
            searchAdapter.searcher.destory();
        }
    }

    private void reset() {
        currentCity = null;
        latitude = longitude = 0;
        chooseAdapter.searcher.configure(this, null, null);
        chooseAdapter.list.clear();
        chooseAdapter.list.add(emptyLocation);
        if (selectedLocation != null && selectedLocation.type != LocationObject.Type.Undefined) {
            chooseAdapter.list.add(selectedLocation);
        } else {
            selectedLocation = emptyLocation;
        }
        chooseAdapter.notifyDataSetChanged();
        searchAdapter.disabled();
    }

    private void reloadLocation() {
        if (isLoadingLocation) return;
        isLoadingLocation = true;
        if (locationProvider == null) {
            locationProvider = new LocationProvider(this);
        }
        locationProvider.requestLocation(new LocationProvider.LocationResultListener() {
            @Override
            public void onLocationResult(boolean success, String city, String area, double latitude, double longitude) {
                isLoadingLocation = false;
                if (LocationSearchActivity.this.isFinishing()) return;
                if (success) {
                    currentCity = city;
                    currentArea = area;
                    LocationSearchActivity.this.latitude = latitude;
                    LocationSearchActivity.this.longitude = longitude;
                    if (!(selectedLocation != null && selectedLocation.type == LocationObject.Type.City && selectedLocation.name.equals(currentCity))) {
                        chooseAdapter.list.add(1, LocationObject.city(currentCity, latitude, longitude));
                        chooseAdapter.notifyDataSetChanged();
                    }
                    LatLng latLng = new LatLng(latitude, longitude);
                    chooseAdapter.searcher.configure(LocationSearchActivity.this, latLng, chooseAdapter);
                    searchAdapter.searcher.configure(LocationSearchActivity.this, latLng, searchAdapter);
                    LocationSearchActivity.this.supportInvalidateOptionsMenu();
                    loadMore();
                } else {
                    currentCity = null;
                    mFootUpdate.showFail();
                }
            }
        });
    }

    @ItemClick(R.id.listView)
    void onItemClick(final LocationObject data) {
        if (data == null) return;
        if (data.type == LocationObject.Type.newCustom) {
            LocationEditActivity_.intent(this)
                    .name(data.name).city(currentCity).area(currentArea)
                    .latitude(latitude).longitude(longitude)
                    .startForResult(MaopaoAddActivity.RESULT_REQUEST_LOCATION);
            return;
        }
        data.city = currentCity;
        Intent intent = new Intent();
        intent.putExtra("location", data);
        setResult(RESULT_OK, intent);
        finish();
    }

    @OnActivityResult(MaopaoAddActivity.RESULT_REQUEST_LOCATION)
    void on_RESULT_REQUEST_LOCATION(int result, Intent intent, @OnActivityResult.Extra LocationObject location) {
        if (result == RESULT_OK) {
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void initActionView(MenuItem searchItem) {
        if (searchView != null) return;
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        try { // 更改搜索按钮的icon
            int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
            ImageView v = (ImageView) searchView.findViewById(searchImgId);
            v.setImageResource(R.drawable.ic_menu_search);
        } catch (Exception e) {
            Global.errorLog(e);
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                ++sSearchPos;
                searchAdapter.reload(searchView.getQuery().toString());
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_search);
        if (TextUtils.isEmpty(currentCity)) {
            menuItem.setVisible(false);
        } else {
            menuItem.setVisible(true);
            MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    if (listView != null && searchAdapter != null) {
                        listView.setAdapter(searchAdapter);
                        searchAdapter.reload("");
                        initActionView(item);
                        searchView.requestFocus();
                        mFootUpdate.dismiss();
                    }
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    if (listView != null && chooseAdapter != null) {
                        listView.setAdapter(chooseAdapter);
                    }
                    return true;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    private abstract class LocationAdapter extends BaseAdapter implements FootUpdate.LoadMore {
        ArrayList<LocationObject> list = new ArrayList<>();

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public LocationObject getItem(int position) {
            return position >= 0 && position < list.size() ? list.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.location_list_item, listView, false);
            }
            LocationItem locationItem = LocationItem.from(convertView);
            LocationObject data = getItem(position);
            bindItem(locationItem, position, data);
            return convertView;
        }

        protected abstract void bindItem(LocationItem locationItem, int position, LocationObject data);
    }

    private class ChooseAdapter extends LocationAdapter implements LocationSearcher.SearchResultListener {

        LocationSearcherGroup searcher = new LocationSearcherGroup(RECOMMEND_KEYS);

        @Override
        public void bindItem(LocationItem locationItem, int position, LocationObject data) {
            locationItem.bind(data, selectedLocation == data);
            if (position == list.size() - 1) {
                loadMore();
            }
        }

        @Override
        public void loadMore() {
            if (TextUtils.isEmpty(currentCity)) {
                reloadLocation();
            } else {
                searcher.search();
            }
        }

        @Override
        public void onSearchResult(List<LocationObject> locations) {
            if (LocationSearchActivity.this.isFinishing()) return;
            addToList(locations);
            notifyDataSetChanged();
            if (searcher.isComplete()) {
                mFootUpdate.dismiss();
            } else {
                mFootUpdate.showLoading();
            }
        }

        private void addToList(List<LocationObject> locations) {
            if (locations == null) return;
            if (selectedLocation != null) {
                if (selectedLocation.type == LocationObject.Type.City) {
                    String distinceKey = selectedLocation.name;
                    if (distinceKey != null) {
                        for (LocationObject item : locations) {
                            if (!TextUtils.isEmpty(item.address) && !distinceKey.equals(item.name)) {
                                list.add(item);
                            }
                        }
                        return;
                    }
                } else if (selectedLocation.type == LocationObject.Type.Normal) {
                    String distinceKeyName = selectedLocation.name;
                    String distinceKeyAddr = selectedLocation.address == null ? "" : selectedLocation.address;
                    if (distinceKeyName != null) {
                        for (LocationObject item : locations) {
                            if (!TextUtils.isEmpty(item.address) && !distinceKeyName.equals(item.name) && !distinceKeyAddr.equals(item.address)) {
                                list.add(item);
                            }
                        }
                        return;
                    }
                }
            }
            list.addAll(locations);
        }
    }

    private class SearchAdapter extends LocationAdapter implements LocationSearcher.SearchResultListener {
        static final int STATE_DISABLED = 0;
        static final int STATE_SEARCH = 1;
        static final int STATE_COMPLETE = 2;
        private LocationSearcherGroup searcher;
        private int state = STATE_DISABLED;
        private View footView;
        private TextView customTextView;

        SearchAdapter() {
            searcher = new LocationSearcherGroup();
            searcher.configure(LocationSearchActivity.this, new LatLng(latitude, longitude), this);
            footView = getLayoutInflater().inflate(R.layout.location_list_custom, listView, false);
            customTextView = (TextView) footView.findViewById(R.id.secondary);
        }

        void disabled() {
            state = STATE_DISABLED;
            list.clear();
            mFootUpdate.dismiss();
        }

        void reload(String keyword) {
            keyword = keyword == null ? "" : keyword.trim();
            if (!keyword.equals(searcher.getKeyword())) {
                state = STATE_SEARCH;
                list.clear();
                notifyDataSetChanged();
                mFootUpdate.dismiss();
                searcher.setKeyword(keyword);
                if (searcher.isKeywordEmpty()) {
                    mFootUpdate.dismiss();
                } else {
                    loadMore();
                }
            }
        }

        @Override
        public void loadMore() {
            if (state == STATE_SEARCH && !searcher.isKeywordEmpty()) {
                mFootUpdate.showLoading();
                searcher.search();
            }
        }

        private void complete() {
            if (state == STATE_SEARCH) {
                state = STATE_COMPLETE;
                mFootUpdate.dismiss();
                String keyword = searcher.getKeyword();
                if (!TextUtils.isEmpty(keyword)) {
                    boolean notFound = true;
                    for (LocationObject item : list) {
                        if (keyword.equals(item.name)) {
                            notFound = false;
                            break;
                        }
                    }
                    if (notFound) {
                        list.add(LocationObject.newCustom(keyword, latitude, longitude));
                        customTextView.setText("创建新的位置: " + keyword);
                    }
                }
            }
        }

        @Override
        public void bindItem(LocationItem locationItem, int position, LocationObject data) {
            locationItem.bind(data, false);
            if (position == list.size() - 1) {
                loadMore();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (state == STATE_COMPLETE && position == list.size() - 1
                    && getItem(position).type == LocationObject.Type.newCustom) {
                return footView;
            }
            return super.getView(position, (footView == convertView ? null : convertView), parent);
        }

        @Override
        public void onSearchResult(List<LocationObject> locations) {
            if (locations != null) {
                list.addAll(locations);
            }
            if (searcher.isComplete()) {
                complete();
            }
            searchAdapter.notifyDataSetChanged();
        }
    }
}
