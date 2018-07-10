package net.coding.program.search;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.EventProjectModify;
import net.coding.program.project.ProjectHomeActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Vernon on 15/11/21.
 */
@EFragment(R.layout.fragment_search_list)
public class SearchResultListFragment extends SearchBaseFragment {
    private static final String TAG = SearchResultListFragment.class.getSimpleName();
    final String url = Global.HOST_API + "/esearch/all?q=%s";
    final String tmp = "&types=%s&pageSize=10";
    @InstanceState
    protected String keyword = "";
    ArrayList<ProjectObject> mData = new ArrayList<>();
    String page = "&page=%s";
    int pos = 1;
    MyAdapter adapter;
    private String tabPrams;
    private boolean hasMore = true;
    private boolean isLoading = true;
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

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventProjectModify(EventProjectModify event) {
        onRefresh();
    }

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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getTabPrams() {
        return tabPrams;
    }

    public void setTabPrams(String tabPrams) {
        this.tabPrams = tabPrams;
    }

    private String getUrl(int pos) {
        String tag = "";
        tag = String.format(url, getKeyword()) + String.format(tmp, getTabPrams()) + String.format(page, pos + "");
        return tag;
    }

    @ItemClick
    final void listView(ProjectObject itemData) {
        itemData.isPublic = itemData.getType() == 1;
        itemData.description = itemData.description.replace("<em>", "").replace("</em>", "");
        itemData.name = itemData.name.replace("<em>", "").replace("</em>", "");

        int endIndex = itemData.project_path.indexOf("/p/");
        if (endIndex == -1) {
            showButtomToast("找不到所选项目");
            return;
        }
        itemData.getOwner().global_key = itemData.project_path.substring(0, endIndex).replace("/u/", "");
        ProjectJumpParam param = new ProjectJumpParam(itemData.getOwner().global_key,
                itemData.name);
        ProjectHomeActivity_.intent(this).mJumpParam(param).start();
    }

    @Override
    public void loadMore() {
        getNetwork(getUrl(pos), keyword);
    }

    @Override
    public void onRefresh() {
//        pos = 1;
        loadMore();
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

            holder.privateIcon.setVisibility(item.getType() == 1 ? View.VISIBLE : View.INVISIBLE);

            String name = "";
            try {
                name = item.project_path.substring(0, item.project_path.indexOf("/p/")).replace("/u/", "");
            } catch (Exception e) {
                Global.errorLog(e);
            }
            holder.content.setText(name);
            HoloUtils.setHoloText(holder.desc, item.getDescription());
            holder.tv_follow_count.setText(item.getWatchCountString());
            holder.tv_star_count.setText(item.getStarString());
            holder.tv_fork_count.setText(item.getForkCountString());
            holder.badge.setVisibility(View.INVISIBLE);
            if (item.getType() == 1) {
                holder.name.setVisibility(View.VISIBLE);
                HoloUtils.setHoloText(holder.name, item.name);
                holder.name2.setVisibility(View.INVISIBLE);
                holder.content.setVisibility(View.VISIBLE);
            } else {
                holder.name2.setVisibility(View.VISIBLE);
                HoloUtils.setHoloText(holder.name2, item.name);
                holder.name.setVisibility(View.INVISIBLE);
                holder.content.setVisibility(View.GONE);
            }
            holder.ll_bottom_menu.setVisibility(item.getType() == 1 ? View.VISIBLE : View.GONE);
            iconfromNetwork(holder.image, item.icon, ImageLoadTool.optionsRounded2);
            return view;
        }
    }
}
