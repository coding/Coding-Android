package net.coding.program.mall;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.activity_pick_local)
public class PickLocalActivity extends BackActivity {

    public static final String EXTRA_LOCAL = "EXTRA_LOCAL";
    public static final String EXTRA_LIST_DATA = "EXTRA_LIST_DATA";
    public static final String EXTRA_LOCAL_POS = "EXTRA_LOCAL_POS";

    private static final int RESULT_LOCAL = 1;

    ListView listView;

    MallOrderSubmitActivity.Local mLocal;
    private int mPos;
    AdapterView.OnItemClickListener mItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MallOrderSubmitActivity.City data = (MallOrderSubmitActivity.City) parent.getItemAtPosition(position);
            if (mPos >= 3) {
                mLocal.district = data;
                pickFinish();
            } else {
                if (mPos == 1) {
                    mLocal.provicen = data;
                } else if (mPos == 2) {
                    mLocal.city = data;
                }

                AsyncHttpClient client = MyAsyncHttpClient.createClient(PickLocalActivity.this);
                String host = String.format(Global.HOST_API + "/region?parent_id=%d&level=%d", data.getId(), mPos + 1);
                client.get(host, new MyJsonResponse(PickLocalActivity.this) {
                    @Override
                    public void onMySuccess(JSONObject response) {
                        super.onMySuccess(response);
                        ArrayList<MallOrderSubmitActivity.City> citys = PickLocalActivity.citysFromJson(response);
                        if (citys.isEmpty()) {
                            pickFinish();
                        } else {
                            Intent intent = new Intent(PickLocalActivity.this, PickLocalActivity_.class);
                            intent.putExtra(PickLocalActivity.EXTRA_LOCAL_POS, mPos + 1);
                            intent.putExtra(PickLocalActivity.EXTRA_LIST_DATA, citys);
                            intent.putExtra(PickLocalActivity.EXTRA_LOCAL, mLocal);
                            startActivityForResult(intent, RESULT_LOCAL);
                        }

                        showProgressBar(false, "");
                    }

                    @Override
                    public void onMyFailure(JSONObject response) {
                        super.onMyFailure(response);
                        showProgressBar(false, "");
                    }
                });

                showProgressBar(true, "");
            }
        }
    };

    public static ArrayList<MallOrderSubmitActivity.City> citysFromJson(JSONObject response) {
        JSONArray array = response.optJSONArray("data");
        ArrayList<MallOrderSubmitActivity.City> citys = new ArrayList<>();
        for (int i = 0; i < array.length(); ++i) {
            JSONObject item = array.optJSONObject(i);
            MallOrderSubmitActivity.City city = new MallOrderSubmitActivity.City(item.optInt("id"), item.optString("name"));
            citys.add(city);
        }

        return citys;
    }

    @AfterViews
    void initPickLocalActivity() {
        listView = (ListView) findViewById(R.id.listView);
        mLocal = (MallOrderSubmitActivity.Local) getIntent().getSerializableExtra(EXTRA_LOCAL);
        ArrayList<MallOrderSubmitActivity.City> listData = (ArrayList<MallOrderSubmitActivity.City>) getIntent().getSerializableExtra(EXTRA_LIST_DATA);
        mPos = getIntent().getIntExtra(EXTRA_LOCAL_POS, 1);
        ListAdapter mAdapter = new CityAdapter(this, R.layout.list_item_city, listData);
        View head = LayoutInflater.from(this).inflate(R.layout.list_item_city_head, listView, false);
        listView.addHeaderView(head, null, false);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(mItemClick);
    }

    private void pickFinish() {
        if (mPos == 2) {
            mLocal.district.clear();
        } else if (mPos == 1) {
            mLocal.district.clear();
            mLocal.city.clear();
        }

        Intent intent = new Intent();
        intent.putExtra("result", mLocal);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOCAL) {
            if (resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            }
        }
    }

    static class CityAdapter extends ArrayAdapter<MallOrderSubmitActivity.City> {
        public CityAdapter(Context context, int resource, ArrayList<MallOrderSubmitActivity.City> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_city, parent, false);
            }

            MallOrderSubmitActivity.City data = getItem(position);
            TextView tv = (TextView) convertView.findViewById(R.id.text1);
            tv.setText(data.getName());

            return convertView;
        }
    }


}
