package net.coding.program.user.team;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.team.TeamListObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.route.BlankViewDisplay;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.activity_team_list)
public class TeamListActivity extends BackActivity {

    private static final int RESULT_DETAIL = 1;
    private static final String TAG_TEAM_LIST = "TAG_TEAM_LIST";

    @ViewById
    ListView listView;

    @ViewById
    View blankLayout;

    ArrayList<TeamListObject> listData = new ArrayList<>();
    View.OnClickListener onClickRetry = v -> loadData();
    SimpleAdapter baseAdapter = new SimpleAdapter<TeamListObject, ViewHolder>() {
        @Override
        public void bindData(ViewHolder holder, TeamListObject data, int position) {
            imagefromNetwork(holder.icon, data.avatar, ImageLoadTool.optionsRounded2);
            holder.name.setText(data.name);
            holder.projectCount.setText(String.valueOf(data.projectcount));
            holder.memberCount.setText(String.valueOf(data.membercount));
            holder.bottomLine.setVisibility(position == getCount() - 1 ? View.INVISIBLE : View.VISIBLE);
        }

        @Override
        public int getItemlayoutId() {
            return R.layout.activity_team_list_item;
        }

        @Override
        public ViewHolder createViewHolder(View v) {
            return new ViewHolder(v);
        }
    };

    @AfterViews
    void initTeamListActivity() {
        View listViewFooter = getLayoutInflater().inflate(R.layout.divide_bottom_15, listView, false);
        listView.addFooterView(listViewFooter, null, false);
        baseAdapter.init(listData);
        listView.setAdapter(baseAdapter);
        listView.setVisibility(View.INVISIBLE);
        showDialogLoading();

        loadData();
    }

    private void loadData() {
        String host = Global.HOST_API + "/team/joined";
        getNetwork(host, TAG_TEAM_LIST);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_TEAM_LIST)) {
            hideProgressDialog();
            if (code == 0) {
                listData.clear();

                JSONArray jsonArray = respanse.optJSONArray("data");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    listData.add(new TeamListObject(jsonArray.optJSONObject(i)));
                }
                baseAdapter.notifyDataSetChanged();
                listView.setVisibility(View.VISIBLE);

            } else {
                showErrorMsg(code, respanse);
            }

            BlankViewDisplay.setBlank(listData.size(), this, code == 0, blankLayout, onClickRetry);
        }
    }

    @ItemClick
    public void listView(TeamListObject item) {
        TeamListDetailActivity_.intent(this).team(item).start();
    }

    @OnActivityResult(RESULT_DETAIL)
    void onResultDetail(int resultCode) {
        loadData();
    }

//    BaseAdapter baseAdapter = new SimpleAdapter<TeamListObject, ViewHolder>() {} {
//        @Override
//        public int getCount() {
//            return listData.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return listData.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHolder holder;
//            if (convertView == null) {
//                holder = new ViewHolder(parent);
//                convertView = holder.layout;
//                convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }
//
//            TeamListObject data = (TeamListObject) getItem(position);
//            imagefromNetwork(holder.icon, data.avatar, ImageLoadTool.optionsRounded2);
//            holder.name.setText(data.name);
//            holder.projectCount.setText(String.valueOf(data.projectcount));
//            holder.memberCount.setText(String.valueOf(data.membercount));
//            holder.bottomLine.setVisibility(position == getCount() - 1 ? View.INVISIBLE : View.VISIBLE);
//
//            return convertView;
//        }
//    };

    class ViewHolder {
        ImageView icon;
        TextView name;
        TextView projectCount;
        TextView memberCount;
        View bottomLine;
        View layout;

        public ViewHolder(View view) {
            layout = view;
            icon = (ImageView) view.findViewById(R.id.icon);
            name = (TextView) view.findViewById(R.id.name);
            projectCount = (TextView) view.findViewById(R.id.projectCount);
            memberCount = (TextView) view.findViewById(R.id.memberCount);
            bottomLine = view.findViewById(R.id.bottomLine);
        }
    }
}