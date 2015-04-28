package net.coding.program.project;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.init.InitProUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

@EFragment(R.layout.project_list_fragment)
public class ProjectListFragment extends RefreshBaseFragment {

    @FragmentArg
    ArrayList<ProjectObject> mData = new ArrayList<ProjectObject>();

    boolean mRequestOk;

    public void setData(ArrayList<ProjectObject> data, boolean requestOk) {
        mData = data;
        mRequestOk = requestOk;
    }

    public void setDataAndUpdate(ArrayList<ProjectObject> data) {
        mRequestOk = true;
        mData = data;
        myAdapter.notifyDataSetChanged();
        // 不让空白画面出现
        BlankViewDisplay.setBlank(1, this, true, blankLayout, null);
    }

    @ViewById
    ExpandableStickyListHeadersListView listView;

    @ViewById
    View blankLayout;

    @ViewById
    SwipeRefreshLayout swipeRefreshLayout;

    MyAdapter myAdapter = new MyAdapter();

    @AfterViews
    protected void init() {
        initRefreshLayout();
        msectionId = 0;
        for (ProjectObject item : mData) {
            if (!item.isPin()) {
                break;
            }
            ++msectionId;
        }
        listView.setAdapter(myAdapter);

        if (getParentFragment() == null) { // 搜索
            disableRefreshing();
        }

        if (AccountInfo.isCacheProjects(getActivity())) {
            BlankViewDisplay.setBlank(mData.size(), this, mRequestOk, blankLayout, null);
        }
    }

    @Override
    public void onRefresh() {
        ((SwipeRefreshLayout.OnRefreshListener) getParentFragment()).onRefresh();
    }

    public void setRead(int id) {
        for (int i = 0; i < mData.size(); ++i) {
            if (mData.get(i).getId() == id) {
                mData.get(i).un_read_activities_count = 0;
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                break;
            }
        }
    }

    public static final String HOST_VISTIT = Global.HOST + "/api/project/%d/update_visit";

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_VISTIT)) {
            if (respanse.getInt("code") == 0) {
                int id = (int) data;
                ((UpdateData) getParentFragment()).updateRead(id);
                UnreadNotify.update(getActivity());
            }
        }
    }

    @ItemClick
    public void listView(ProjectObject item) {
//        if (item.un_read_activities_count > 0) {
        // 调用此函数，则按hot排序时项目会排序到有动态的项目后面
        String s = String.format(HOST_VISTIT, item.getId());
        getNetwork(s, HOST_VISTIT, 0, item.getId());
//        }

        // 在搜索界面不是嵌套的，getParentFragment会返回null
        Fragment fragment = getParentFragment();
        if (fragment == null) {
            fragment = this;
        }

        ProjectHomeActivity_.intent(fragment).mProjectObject(item).startForResult(InitProUtils.REQUEST_PRO_UPDATE);
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

    int msectionId = 0;

    class MyAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer {

        final String[] titles = new String[]{
                "常用项目",
                "一般项目"
        };


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

            return view;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = mInflater.inflate(R.layout.fragment_project_dynamic_list_head, parent, false);
                holder.mHead = (TextView) convertView.findViewById(R.id.head);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }

            int type = getSectionForPosition(position);
            String title = titles[type];
            holder.mHead.setText(title);

            return convertView;
        }


        @Override
        public long getHeaderId(int i) {
            return getSectionForPosition(i);
        }

        @Override
        public Object[] getSections() {
            return titles;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return sectionIndex;
        }

        @Override
        public int getSectionForPosition(int position) {
            if (position < msectionId) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    ;


    static class HeaderViewHolder {
        TextView mHead;
    }

    private static class ViewHolder {
        TextView name;
        ImageView image;
        TextView content;
        BadgeView badge;
        View privateIcon;
    }

    public interface UpdateData {
        void updateRead(int id);
    }
}
