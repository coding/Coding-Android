package net.coding.program.maopao;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;

import net.coding.program.BaseFragmentActivity;
import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.StartActivity;
import net.coding.program.maopao.item.LocationItem;
import net.coding.program.model.LocationObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Neutra on 2015/3/11.
 */
@EActivity(R.layout.activity_choose_location)
@OptionsMenu(R.menu.choose_location)
public class ChooseLocationActivity extends BaseFragmentActivity implements FootUpdate.LoadMore, StartActivity {
    @ViewById
    ListView listView;
    @Extra
    LocationObject selectedLocation;

    private Adapter adapter;
    private String currentCity = null;
    private double latitude, longitude;

    private LocationObject emptyLocation = LocationObject.undefined();
    private LocationSearcherGroup searcher = new LocationSearcherGroup();

    @AfterViews
    void afterViews() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mFootUpdate.init(listView, mInflater, this);
        adapter = new Adapter();
        listView.setAdapter(adapter);
        reloadLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searcher != null) {
            searcher.destory();
        }
    }

    @Override
    public void loadMore() {
        mFootUpdate.showLoading();
        searcher.search();
    }

    private void resetList() {
        currentCity = null;
        latitude = longitude = 0;
        searcher.configure(this, null, null);
        adapter.list.clear();
        adapter.list.add(emptyLocation);
        if (selectedLocation != null && selectedLocation.type != LocationObject.Type.Undefined) {
            adapter.list.add(selectedLocation);
        } else {
            selectedLocation = emptyLocation;
        }
    }

    private void addToList(List<LocationObject> locations) {
        if (locations == null) return;
        if (selectedLocation != null) {
            if (selectedLocation.type == LocationObject.Type.City) {
                String distinceKey = selectedLocation.name;
                if (distinceKey != null) {
                    for (LocationObject item : locations) {
                        if (!distinceKey.equals(item.name)) adapter.list.add(item);
                    }
                    return;
                }
            } else if (selectedLocation.type == LocationObject.Type.Normal) {
                String distinceKey = selectedLocation.id;
                if (distinceKey != null) {
                    for (LocationObject item : locations) {
                        if (!distinceKey.equals(item.id)) adapter.list.add(item);
                    }
                    return;
                }
            }
        }
        adapter.list.addAll(locations);
    }

    private LocationSearcher.SearchResultListener searchResultListener = new LocationSearcher.SearchResultListener() {
        @Override
        public void onSearchResult(List<LocationObject> locations) {
            if (ChooseLocationActivity.this.isFinishing()) return;
            addToList(locations);
            adapter.notifyDataSetChanged();
            if (searcher.isComplete()) {
                mFootUpdate.dismiss();
            } else {
                mFootUpdate.showFail();
            }
        }
    };

    private void reloadLocation() {
        resetList();
        adapter.notifyDataSetChanged();
        LocationProvider.getInstance(this).requestLocation(new LocationProvider.LocationResultListener() {
            @Override
            public void onLocationResult(boolean success, String city, double latitude, double longitude) {
                if (ChooseLocationActivity.this.isFinishing()) return;
                if (success) {
                    currentCity = city;
                    if (!(selectedLocation != null && selectedLocation.type == LocationObject.Type.City && selectedLocation.name.equals(currentCity))) {
                        adapter.list.add(1, LocationObject.city(currentCity));
                        adapter.notifyDataSetChanged();
                    }
                    ChooseLocationActivity.this.latitude = latitude;
                    ChooseLocationActivity.this.longitude = longitude;
                    searcher.configure(ChooseLocationActivity.this, new LatLng(latitude, longitude), searchResultListener);
                    loadMore();
                } else {
                    mFootUpdate.showFail();
                }
            }
        });
    }

    @ItemClick(R.id.listView)
    void onItemClick(LocationObject data) {
        Intent intent = new Intent();
        intent.putExtra("location", data);
        setResult(RESULT_OK, intent);
        finish();
    }

    @OptionsItem
    void action_search() {
        Toast.makeText(this, "搜索位置", Toast.LENGTH_SHORT).show();
    }

    private class Adapter extends BaseAdapter {
        ArrayList<LocationObject> list = new ArrayList<LocationObject>();

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
            locationItem.bind(position, data, selectedLocation == data);
            if (position == list.size() - 1) {
                loadMore();
            }
            return convertView;
        }
    }
}
