package net.coding.program.user;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.BaseActivity;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.UserObject;
import net.coding.program.project.ProjectHomeActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EFragment(R.layout.fragment_user_project_list)
public class UserProjectListFragment extends RefreshBaseFragment {

    @FragmentArg
    int mType; // 0:joined   1:stared

    @FragmentArg
    UserObject mUserObject;

    @ViewById
    ListView listView;

    @ViewById
    View blankLayout;

    String mUrl;

    ArrayList<ProjectObject> mData = new ArrayList();

    @AfterViews
    protected final void init() {
        initRefreshLayout();

        listView.setAdapter(mAdapter);
        if (mType == 0) {
             mUrl = Global.HOST + "/api/user/" + mUserObject.global_key + "/public_projects?type=joined";
        } else {
             mUrl = Global.HOST + "/api/user/" + mUserObject.global_key + "/public_projects?type=stared";
        }

        loadMore();
    }

    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(mUrl)) {
            setRefreshing(false);
            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mData.clear();
                }

                JSONArray jsonArray = respanse.optJSONObject("data").optJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    ProjectObject projectObject = new ProjectObject(json);
                    mData.add(projectObject);
                }

                if (isLoadingLastPage(tag)) {
                    mFootUpdate.dismiss();
                } else {
                    mFootUpdate.showLoading();
                }
                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry);

                mAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @ItemClick
    protected final void listView(ProjectObject projectObject) {
        ProjectHomeActivity_
                .intent(this)
                .mProjectObject(projectObject)
                .start();
    }

    @Override
    public void onRefresh() {
        initSetting();
        loadMore();
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(mUrl, mUrl);
    }

    BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.project_all_list_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) view.findViewById(R.id.name);
                holder.image = (ImageView) view.findViewById(R.id.icon);
                holder.content = (TextView) view.findViewById(R.id.comment);
                holder.badge = (BadgeView) view.findViewById(R.id.badge);
                holder.privateIcon = view.findViewById(R.id.privateIcon);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ProjectObject item = (ProjectObject) getItem(position);

            holder.name.setText(item.name);

            holder.privateIcon.setVisibility(item.isPublic() ? View.INVISIBLE : View.VISIBLE);
            String ownerName = item.isPublic() ? item.name : ("      " + item.owner_user_name);
            holder.content.setText(ownerName);

            int count = item.un_read_activities_count;
            if (count > 0) {
                String countString = count > 99 ? "99+" : ("" + count);
                holder.badge.setText(countString);
                holder.badge.setVisibility(View.VISIBLE);
            } else {
                holder.badge.setVisibility(View.INVISIBLE);
            }

            iconfromNetwork(holder.image, item.icon, ImageLoadTool.optionsRounded2);

            if (position == (getCount() -1)) {
                loadMore();
            }

            return view;
        }
    };

    private static class ViewHolder {
        TextView name;
        ImageView image;
        TextView content;
        BadgeView badge;
        View privateIcon;
    }
}
