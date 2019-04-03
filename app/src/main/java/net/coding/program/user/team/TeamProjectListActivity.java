package net.coding.program.user.team;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.team.TeamListObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.ProjectHomeActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

@EActivity(R.layout.activity_team_project_list)
public class TeamProjectListActivity extends BackActivity {

    private static final String TAG_PROJECT_LIST = "TAG_PROJECT_LIST";
    private static final String TAG_PROJECT_LIST_UNJOINED = "TAG_PROJECT_LIST_UNJOINED";

    ArrayList<ProjectObject> listData = new ArrayList<>();
    int joinedCount = 0;

    @Extra
    TeamListObject teamListObject;

    @ViewById
    StickyListHeadersListView listView;

    ListAdapter listAdapter;

    @AfterViews
    void initTeamProjectListActivity() {
        listViewAddFootSection(listView.getWrappedList());
        listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(((parent, view, position, id) -> {
            int pos = (int) id;
            if (pos >= joinedCount) {
                showMiddleToast("");
                return;
            }
            ProjectObject project = listAdapter.getItem(pos);
            ProjectJumpParam param = new ProjectJumpParam(project.owner_user_name, project.name);
            ProjectHomeActivity_.intent(TeamProjectListActivity.this)
                    .mJumpParam(param)
                    .start();
        }));
        loadJoinUrl();
    }

    void loadUnjoinUrl() {
        String url = String.format("%s/team/%s/projects/unjoined", Global.HOST_API, teamListObject.globalkey);
        getNetwork(url, TAG_PROJECT_LIST_UNJOINED);
    }

    void loadJoinUrl() {
        String url = String.format("%s/team/%s/projects/joined", Global.HOST_API, teamListObject.globalkey);
        getNetwork(url, TAG_PROJECT_LIST);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (TAG_PROJECT_LIST.equals(tag)) {
            if (code == 0) {
                listData.clear();
                JSONArray jsonArray = respanse.optJSONArray("data");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    listData.add(new ProjectObject(jsonArray.optJSONObject(i)));
                }
                joinedCount = jsonArray.length();
                listAdapter.notifyDataSetChanged();

                if (teamListObject.owner.isMe()) {
                    loadUnjoinUrl();
                }
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (TAG_PROJECT_LIST_UNJOINED.equals(tag)) {
            if (code == 0) {
                JSONArray jsonArray = respanse.optJSONArray("data");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    listData.add(new ProjectObject(jsonArray.optJSONObject(i)));
                }
                listAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    class ListAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer {
        @Override
        public long getHeaderId(int position) {
            return getSectionForPosition(position);
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(R.layout.team_project_list_head, parent, false);
            }

            TextView head = (TextView) convertView.findViewById(R.id.head);
            String title = "我参与的";
            if (getSectionForPosition(position) == 1) {
                title = "我未参与的";
            }
            head.setText(title);
            return convertView;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return sectionIndex;
        }

        @Override
        public Integer[] getSections() {
            return new Integer[2];
        }

        @Override
        public int getSectionForPosition(int position) {
            return joinedCount <= position ? 1 : 0;
        }

        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public ProjectObject getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.team_project_list_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ProjectObject data = getItem(position);
            holder.privateIcon.setVisibility(!data.isShared() ? View.GONE : View.VISIBLE);
            holder.name.setText(data.name);
            holder.txtDesc.setText(data.owner_user_name);
            imagefromNetwork(holder.icon, data.icon, ImageLoadTool.optionsRounded2);

            if (position == getCount() - 1 || position == joinedCount - 1) {
                holder.bottomLine.setVisibility(View.INVISIBLE);
            } else {
                holder.bottomLine.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

        class ViewHolder {

            ImageView icon;
            View privateIcon;
            TextView name;
            TextView txtDesc;
            View bottomLine;

            ViewHolder(View view) {
                icon = (ImageView) view.findViewById(R.id.icon);
                privateIcon = view.findViewById(R.id.privateIcon);
                name = (TextView) view.findViewById(R.id.name);
                txtDesc = (TextView) view.findViewById(R.id.txtDesc);
                bottomLine = view.findViewById(R.id.bottomLine);
            }
        }
    }


}
