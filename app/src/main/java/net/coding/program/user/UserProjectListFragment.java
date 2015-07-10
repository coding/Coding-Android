package net.coding.program.user;


import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
    Type mType;
    @FragmentArg
    UserObject mUserObject;
    // 可选，true表示选择项目
    @FragmentArg
    boolean mPickProject;
    @ViewById
    ListView listView;
    @ViewById
    View blankLayout;
    String mUrl;
    ArrayList<ProjectObject> mData = new ArrayList<>();
    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };
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
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            ProjectObject item = (ProjectObject) getItem(position);
            if (item.description.isEmpty()) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;
            if (convertView == null) {
                int layoutRes = getItemViewType(position) == 0 ? R.layout.user_project_all_list_item : R.layout.user_project_all_list_item2;
                view = mInflater.inflate(layoutRes, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) view.findViewById(R.id.name);
                holder.image = (ImageView) view.findViewById(R.id.icon);
                holder.descrip = (TextView) view.findViewById(R.id.description);
                holder.star = (TextView) view.findViewById(R.id.star);
                holder.watch = (TextView) view.findViewById(R.id.watch);
                holder.fork = (TextView) view.findViewById(R.id.folk);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ProjectObject item = (ProjectObject) getItem(position);

            holder.name.setText(item.name);
            holder.descrip.setText(item.description);
            holder.star.setText(String.valueOf(item.star_count));
            holder.watch.setText(String.valueOf(item.watch_count));
            holder.fork.setText(String.valueOf(item.fork_count));

            iconfromNetwork(holder.image, item.icon, ImageLoadTool.optionsRounded2);

            if (position == (getCount() - 1)) {
                loadMore();
            }

            return view;
        }
    };

    @AfterViews
    protected final void init() {
        initRefreshLayout();

        listView.setAdapter(mAdapter);
        switch (mType) {
            case stared:
                mUrl = Global.HOST_API + "/user/" + mUserObject.global_key + "/public_projects?type=stared";
                break;
            case all_private:
                mUrl = Global.HOST_API + "/projects?page=1&pageSize=1000&type=all"; // 没有取私有的api，只好取全部然后本地过滤
                break;
            default: // 0
                mUrl = Global.HOST_API + "/user/" + mUserObject.global_key + "/public_projects?type=joined";
                break;
        }

        loadMore();
    }

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
                    if (mType == Type.all_private) {
                        if (!projectObject.isPublic()) {
                            mData.add(projectObject);
                        }
                    } else {
                        mData.add(projectObject);
                    }
                }

                mFootUpdate.updateState(code, isLoadingLastPage(tag), mData.size());

                String tip = BlankViewDisplay.OTHER_PROJECT_BLANK;
                if (mUserObject.isMe()) {
                    tip = BlankViewDisplay.MY_PROJECT_BLANK;
                }
                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry, tip);

                mAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @ItemClick
    protected final void listView(ProjectObject projectObject) {
        if (mPickProject) {
            Intent intent = new Intent();
            intent.putExtra("data", projectObject);
            FragmentActivity activity = getActivity();
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
        } else {
            ProjectHomeActivity_
                    .intent(this)
                    .mProjectObject(projectObject)
                    .start();
        }
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

    public enum Type {
        joined,
        stared,
        all_private
    }

    private static class ViewHolder {
        TextView name;
        ImageView image;
        TextView descrip;
        TextView star;
        TextView watch;
        TextView fork;
    }
}
