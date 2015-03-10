package net.coding.program.maopao;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import net.coding.program.BaseFragmentActivity;
import net.coding.program.R;
import net.coding.program.common.StartActivity;
import net.coding.program.maopao.item.LocationItem;
import net.coding.program.model.LocationObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

/**
 * Created by Neutra on 2015/3/11.
 */
@EActivity(R.layout.activity_choose_location)
public class ChooseLocationActivity extends BaseFragmentActivity implements StartActivity {
    @ViewById
    ListView listView;
    @Extra
    LocationObject currentLocation;

    private Adapter adapter;
    private String currentCity = null;

    @AfterViews
    void afterViews() {
        adapter = new Adapter();
        LocationObject emptyLocation = LocationObject.undefined();
        adapter.list.add(emptyLocation);
        if (currentLocation != null && currentLocation.type != LocationObject.Type.Undefined) {
            adapter.list.add(currentLocation);
        } else {
            currentLocation = emptyLocation;
        }
        listView.setAdapter(adapter);
        loadLocation();
    }

    @Background
    void loadLocation(){
        // todo
    }

    @ItemClick(R.id.listView)
    void onItemClick(LocationObject data) {
        Intent intent = new Intent();
        intent.putExtra("location", data);
        setResult(RESULT_OK, intent);
        finish();
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
            locationItem.bind(position, data, currentLocation == data);
            return convertView;
        }
    }
}
