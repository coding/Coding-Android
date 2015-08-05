package net.coding.program.user;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.widget.DataAdapter;
import net.coding.program.model.PointObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.activity_user_point)
@OptionsMenu(R.menu.menu_user_point)
public class UserPointActivity extends BackActivity {

    private static final String TAG_HTTP_USER_POINT = "TAG_HTTP_USER_POINT";
    @ViewById
    ListView listView;
    DataAdapter<PointObject> adapter = new DataAdapter<PointObject>() {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            UserPointListItemBinding binding = UserPointListItemBinding.inflate(
//                    LayoutInflater.from(parent.getContext()), parent, false);
//            binding.setPointObject((PointObject) getItem(position));
//            return binding.getRoot();
            return null;
        }
    };
    private ArrayList<PointObject> mData = new ArrayList<>();

    @AfterViews
    protected final void initUserPointActivity() {
        getNetwork(PointObject.getHttpRecord(), TAG_HTTP_USER_POINT);
        listView.setAdapter(adapter);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_HTTP_USER_POINT)) {
            JSONArray json = respanse.optJSONObject("data").optJSONArray("list");
            for (int i = 0; i < json.length(); ++i) {
                mData.add(new PointObject(json.optJSONObject(i)));
            }
            adapter.notifyDataSetChanged();
        }
    }


}