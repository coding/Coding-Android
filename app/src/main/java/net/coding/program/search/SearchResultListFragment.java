package net.coding.program.search;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.ProjectHomeActivity_;
import net.coding.program.project.detail.ProjectActivity;
import net.coding.program.project.init.InitProUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Vernon on 15/11/21.
 */
@EFragment(R.layout.fragment_search_list)
public class SearchResultListFragment extends RefreshBaseFragment {
    private static final String TAG = SearchResultListFragment.class.getSimpleName();
    final String url = Global.HOST_API + "/esearch/all?q=%s";
    final String tmp = "&types=%s&pageSize=10";
    ArrayList<ProjectObject> mData = new ArrayList<>();
    String page = "&page=%s";
    int pos = 1;
    private String keyword = "";
    private String tabPrams;
    private boolean hasMore = true;
    private boolean isLoading = true;
    @ViewById
    ListView listView;
    @ViewById(R.id.emptyView)
    LinearLayout emptyView;

    MyAdapter adapter;

    @AfterViews
    protected void init() {
        initRefreshLayout();
        setRefreshing(true);
        mFootUpdate.init(listView, mInflater, this);
        adapter = new MyAdapter();
        listView.setAdapter(adapter);
        listView.setOnScrollListener(mOnScrollListener);
        loadMore();
    }


    AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem + visibleItemCount == totalItemCount) {
                if (hasMore && !isLoading) {
                    pos++;
                    isLoading = true;
                    loadMore();
                }
            }
        }
    };

    public String getKeyword() {
        return keyword;
    }

    public String getTabPrams() {
        return tabPrams;
    }

    public void setTabPrams(String tabPrams) {
        this.tabPrams = tabPrams;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    private String getUrl(int pos) {
        String tag = "";
        tag = String.format(url, getKeyword()) + String.format(tmp, getTabPrams()) + String.format(page, pos + "");
        return tag;
    }

    @ItemClick
    final void listView(ProjectObject itemData) {
        itemData.is_public = itemData.getType() == 1 ? true : false;
        itemData.description = itemData.description.replace("<em>", "").replace("</em>", "");
        itemData.name = itemData.name.replace("<em>", "").replace("</em>", "");
        itemData.getOwner().global_key = itemData.project_path.substring(0, itemData.project_path.indexOf("/p/")).replace("/u/", "");
        ProjectActivity.ProjectJumpParam param = new ProjectActivity.ProjectJumpParam(itemData.getOwner().global_key,
                itemData.name);
        ProjectHomeActivity_.intent(this).mJumpParam(param).startForResult(InitProUtils.REQUEST_PRO_UPDATE);
    }

    @Override
    public void loadMore() {
        getNetwork(getUrl(pos), keyword);
    }

    @Override
    public void onRefresh() {
        pos = 1;
        loadMore();
    }

    private static class ViewHolder {
        TextView name2;
        TextView name;
        TextView tv_star_count;
        TextView tv_follow_count;
        TextView tv_fork_count;
        LinearLayout ll_bottom_menu;
        ImageView image;
        TextView desc;
        TextView content;
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
                holder.name = (TextView) view.findViewById(R.id.name);
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

            holder.privateIcon.setVisibility(item.getType() == 1 ? View.INVISIBLE : View.VISIBLE);


            String name = item.project_path.substring(0, item.project_path.indexOf("/p/")).replace("/u/", "");
            holder.content.setText(name);
            HoloUtils.setHoloText(holder.desc, keyword, item.getDescription());
            holder.tv_follow_count.setText(item.getWatch_count() + "");
            holder.tv_star_count.setText(item.getStar_count() + "");
            holder.tv_fork_count.setText(item.getFork_count() + "");
            holder.badge.setVisibility(View.INVISIBLE);
            if (item.getType() == 1) {
                holder.name2.setVisibility(View.VISIBLE);
                HoloUtils.setHoloText(holder.name2, keyword, item.name);
                holder.name.setVisibility(View.INVISIBLE);
                holder.content.setVisibility(View.GONE);
            } else {
                holder.name.setVisibility(View.VISIBLE);
                HoloUtils.setHoloText(holder.name, keyword, item.name);
                holder.name2.setVisibility(View.INVISIBLE);
                holder.content.setVisibility(View.VISIBLE);
            }
            holder.ll_bottom_menu.setVisibility(item.getType() == 1 ? View.VISIBLE : View.GONE);
            iconfromNetwork(holder.image, item.icon, ImageLoadTool.optionsRounded2);
            return view;
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(keyword)) {
            setRefreshing(false);
            if (code == 0) {
                if (pos == 1) {
                    mData.clear();
                }
                JSONArray array = respanse.getJSONObject("data").getJSONObject("projects").getJSONArray("list");
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject item = array.getJSONObject(i);
                    ProjectObject oneData = new ProjectObject(item);
                    mData.add(oneData);
                }
                emptyView.setVisibility(mData.size() == 0 ? View.VISIBLE : View.GONE);
                if (array.length() > 0) {
                    hasMore = true;
                    mFootUpdate.updateState(code, false, mData.size());
                } else {
                    hasMore = false;
                    mFootUpdate.updateState(code, true, mData.size());
                }
                adapter.notifyDataSetChanged();
                isLoading = false;
            } else {
                showErrorMsg(code, respanse);
                hasMore = false;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == InitProUtils.REQUEST_PRO_UPDATE) {
            if (resultCode == Activity.RESULT_OK) {
                String action = data.getStringExtra("action");
                if (action.equals(InitProUtils.FLAG_REFRESH)) {
                    onRefresh();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
