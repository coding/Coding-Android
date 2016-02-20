package net.coding.program.project;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.widget.RefreshBaseActivity;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.init.create.ProjectCreateActivity_;
import net.coding.program.search.SearchProjectActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by Vernon on 15/11/20.
 */
@OptionsMenu(R.menu.menu_project_pick_search)
@EActivity(R.layout.activity_project_sqaure)
public class ProjectSquareActivity extends RefreshBaseActivity implements OnClickListener {
    final String hostSquare = Global.HOST_API + "/public/all?page=1&pageSize=1000";

    @ViewById
    View emptyView, container;

    @ViewById
    ListView listView;
    @ViewById
    View blankLayout;
    private ArrayList<ProjectObject> mData = new ArrayList<>();
    private MyAdapter adapter;
    @ViewById
    LinearLayout project_create_layout;
    @ViewById
    Button btn_action;
    private boolean mRequestOk;
    private boolean requestOk;

    @AfterViews
    void init() {
        initRefreshBaseActivity();
        setRefreshing(true);
        btn_action.setOnClickListener(this);
        showDialogLoading();
        loadMore();
    }

    private void loadMore() {
        getNetwork(hostSquare, hostSquare);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onRefresh() {
        loadMore();
    }

    @ItemClick
    public void listView(ProjectObject item) {
        ProjectHomeActivity_.intent(this).mProjectObject(item).start();
    }

    private OnClickListener mOnClickRetry = new OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_action:
                ProjectCreateActivity_.intent(this).start();
                break;
        }
    }

    @OptionsItem
    void action_search_pick() {
        SearchProjectActivity_.intent(this).start();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    private static class ViewHolder {
        TextView name2;
        TextView tv_star_count;
        TextView tv_follow_count;
        TextView tv_fork_count;
        LinearLayout ll_bottom_menu;
        ImageView image;
        TextView content;
        TextView desc;
        BadgeView badge;
        View privateIcon;
    }

    class MyAdapter extends BaseAdapter {
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
                holder.name2 = (TextView) view.findViewById(R.id.name2);
                holder.image = (ImageView) view.findViewById(R.id.icon);
                holder.content = (TextView) view.findViewById(R.id.comment);
                holder.badge = (BadgeView) view.findViewById(R.id.badge);
                holder.privateIcon = view.findViewById(R.id.privateIcon);
                holder.tv_follow_count = (TextView) view.findViewById(R.id.tv_follow_count);
                holder.tv_fork_count = (TextView) view.findViewById(R.id.tv_fork_count);
                holder.tv_star_count = (TextView) view.findViewById(R.id.tv_start_count);
                holder.ll_bottom_menu = (LinearLayout) view.findViewById(R.id.ll_bottom_menu);
                holder.desc = (TextView) view.findViewById(R.id.txtDesc);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ProjectObject item = (ProjectObject) getItem(position);
            holder.privateIcon.setVisibility(View.INVISIBLE);
            holder.name2.setVisibility(View.VISIBLE);
            holder.name2.setText(item.name);
            holder.privateIcon.setVisibility(item.isPublic() ? View.INVISIBLE : View.VISIBLE);
            holder.content.setVisibility(View.INVISIBLE);
            holder.desc.setText(item.getDescription());
            holder.tv_follow_count.setText(item.getWatch_count() + "");
            holder.tv_star_count.setText(item.getStar_count() + "");
            holder.tv_fork_count.setText(item.getFork_count() + "");
            holder.badge.setVisibility(View.INVISIBLE);
            holder.ll_bottom_menu.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(item.icon, holder.image, ImageLoadTool.optionsRounded2);
            return view;
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(hostSquare)) {
            setRefreshing(false);
            if (code == 0) {
                requestOk = true;
                hideProgressDialog();
                mData.clear();
                JSONArray array = respanse.getJSONObject("data").getJSONArray("list");
                int pinCount = 0;
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject item = array.getJSONObject(i);
                    ProjectObject oneData = new ProjectObject(item);
                    if (oneData.isPin()) {
                        mData.add(pinCount++, oneData);
                    } else {
                        mData.add(oneData);
                    }
                }
                AccountInfo.saveProjects(this, mData);
                if (adapter == null) {
                    adapter = new MyAdapter();
                    listView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
                if (!(mData.size() > 0)) {
                    project_create_layout.setVisibility(View.VISIBLE);
                } else {
                    project_create_layout.setVisibility(View.GONE);
                }
            } else {
                requestOk = false;
                showErrorMsg(code, respanse);
                BlankViewDisplay.setBlank(mData.size(), this, mRequestOk, blankLayout, mOnClickRetry);
//                adapter.notifyDataSetChanged();
            }
        }
    }
}

