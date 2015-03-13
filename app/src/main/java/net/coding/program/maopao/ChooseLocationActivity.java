package net.coding.program.maopao;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;

import net.coding.program.BaseActivity;
import net.coding.program.FootUpdate;
import net.coding.program.R;
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
public class ChooseLocationActivity extends BaseActivity implements FootUpdate.LoadMore {
    @ViewById
    ListView listView;
    @Extra
    LocationObject selectedLocation;

    private ChooseAdapter chooseAdapter;
    private SearchAdapter searchAdapter;
    private String currentCity = null;
    private double latitude, longitude;

    private boolean isLoadingLocation = false;

    private LocationObject emptyLocation = LocationObject.undefined();
    private LocationSearcherGroup searcher = new LocationSearcherGroup();

    @AfterViews
    void afterViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
            if (currentCity == null) {
                reloadLocation();
            } else {
                searcher.search();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searcher != null) {
            searcher.destory();
        }
    }


    private void reset() {
        currentCity = null;
        latitude = longitude = 0;
        searcher.configure(this, null, null);
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

    private void addToChooseList(List<LocationObject> locations) {
        if (locations == null) return;
        if (selectedLocation != null) {
            if (selectedLocation.type == LocationObject.Type.City) {
                String distinceKey = selectedLocation.name;
                if (distinceKey != null) {
                    for (LocationObject item : locations) {
                        if (!distinceKey.equals(item.name)) chooseAdapter.list.add(item);
                    }
                    return;
                }
            } else if (selectedLocation.type == LocationObject.Type.Normal) {
                String distinceKey = selectedLocation.id;
                if (distinceKey != null) {
                    for (LocationObject item : locations) {
                        if (!distinceKey.equals(item.id)) chooseAdapter.list.add(item);
                    }
                    return;
                }
            }
        }
        chooseAdapter.list.addAll(locations);
    }

    private LocationSearcher.SearchResultListener searchResultListener = new LocationSearcher.SearchResultListener() {
        @Override
        public void onSearchResult(List<LocationObject> locations) {
            if (ChooseLocationActivity.this.isFinishing()) return;
            addToChooseList(locations);
            chooseAdapter.notifyDataSetChanged();
            if (searcher.isComplete()) {
                mFootUpdate.dismiss();
            } else {
                mFootUpdate.showLoading();
            }
        }
    };

    private void reloadLocation() {
        if (isLoadingLocation) return;
        isLoadingLocation = true;
        LocationProvider.getInstance(this).requestLocation(new LocationProvider.LocationResultListener() {
            @Override
            public void onLocationResult(boolean success, String city, double latitude, double longitude) {
                isLoadingLocation = false;
                if (ChooseLocationActivity.this.isFinishing()) return;
                if (success) {
                    currentCity = city;
                    if (!(selectedLocation != null && selectedLocation.type == LocationObject.Type.City && selectedLocation.name.equals(currentCity))) {
                        chooseAdapter.list.add(1, LocationObject.city(currentCity, latitude, longitude));
                        chooseAdapter.notifyDataSetChanged();
                    }
                    ChooseLocationActivity.this.latitude = latitude;
                    ChooseLocationActivity.this.longitude = longitude;
                    LatLng latLng = new LatLng(latitude, longitude);
                    searcher.configure(ChooseLocationActivity.this, latLng, searchResultListener);
                    searchAdapter.searcher.configure(ChooseLocationActivity.this, latLng, searchAdapter);
                    loadMore();
                } else {
                    currentCity = null;
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

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    private abstract class LocationAdapter extends BaseAdapter {
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
            bindItem(locationItem, position, data);
            return convertView;
        }

        protected abstract void bindItem(LocationItem locationItem, int position, LocationObject data);
    }

    private class ChooseAdapter extends LocationAdapter {
        @Override
        public void bindItem(LocationItem locationItem, int position, LocationObject data) {
            locationItem.bind(data, selectedLocation == data);
            if (position == list.size() - 1) {
                loadMore();
            }
        }
    }

    private class SearchAdapter extends LocationAdapter implements LocationSearcher.SearchResultListener {
        private LocationSearcher searcher;
        static final int STATE_DISABLED = 0;
        static final int STATE_SEARCH = 1;
        static final int STATE_COMPLETE = 2;
        private int state = STATE_DISABLED;
        private View footView;
        private TextView customTextView;

        SearchAdapter() {
            searcher = new LocationSearcher();
            searcher.configure(ChooseLocationActivity.this, new LatLng(latitude, longitude), this);
            footView = getLayoutInflater().inflate(R.layout.location_list_custom, listView, false);
            customTextView = (TextView) footView.findViewById(R.id.secondary);
        }

        void disabled() {
            state = STATE_DISABLED;
            list.clear();
            mFootUpdate.dismiss();
        }

        void reload(String keyword) {
            state = STATE_SEARCH;
            list.clear();
            notifyDataSetChanged();
            mFootUpdate.dismiss();
            searcher.keyword(keyword);
            if (TextUtils.isEmpty(searcher.keyword())) {
                mFootUpdate.dismiss();
            } else {
                mFootUpdate.showLoading();
                searcher.search();
            }
        }

        void loadMore() {
            if (state == STATE_SEARCH && !TextUtils.isEmpty(searcher.keyword())) {
                mFootUpdate.showLoading();
                searcher.search();
            }
        }

        private void complete() {
            if (state == STATE_SEARCH) {
                state = STATE_COMPLETE;
                mFootUpdate.dismiss();
                if (!TextUtils.isEmpty(searcher.keyword())) {
                    list.add(LocationObject.newCustom(searcher.keyword(), latitude, longitude));
                    customTextView.setText("创建新的位置: " + searcher.keyword());
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
            if (state == STATE_COMPLETE) {
                if (position == list.size() - 1 && !TextUtils.isEmpty(searcher.keyword())) {
                    return footView;
                }
            }
            if (footView == convertView) {
                return super.getView(position, null, parent);
            }
            return super.getView(position, convertView, parent);
        }

        @Override
        public void onSearchResult(List<LocationObject> locations) {
            if (locations == null) {
                complete();
            } else {
                list.addAll(locations);
            }
            searchAdapter.notifyDataSetChanged();
        }
    }

    private  SearchView searchView;

    private void initActionView(MenuItem searchItem) {
        if(searchView != null) return;
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        try { // 更改搜索按钮的icon
            int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
            ImageView v = (ImageView) searchView.findViewById(searchImgId);
            v.setImageResource(R.drawable.ic_menu_search);
        } catch (Exception e) {
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchAdapter.reload(s);
                return true;
            }
        });

//        if (searchText != null) return;
//        searchText = (EditText) actionView.findViewById(R.id.search_text);
//        final View actionDelete = actionView.findViewById(R.id.action_delete);
//
//        searchText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                actionDelete.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
//                searchAdapter.reload(searchText.getText().toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
//        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
//                searchAdapter.reload(searchText.getText().toString());
//                return false;
//            }
//        });
//        actionDelete.setVisibility(View.GONE);
//        actionDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                searchText.setText("");
//                searchAdapter.reload("");
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (listView != null && searchAdapter != null) {
                    listView.setAdapter(searchAdapter);
                    searchAdapter.reload("");
                    initActionView(item);
                    searchView.requestFocus();
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
        return super.onCreateOptionsMenu(menu);
    }
}
