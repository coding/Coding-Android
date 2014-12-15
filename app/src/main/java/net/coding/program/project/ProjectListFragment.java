package net.coding.program.project;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.detail.ProjectActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EFragment(R.layout.common_refresh_listview)
public class ProjectListFragment extends RefreshBaseFragment {

    @FragmentArg
    ArrayList<ProjectObject> mData = new ArrayList<ProjectObject>();

    boolean mRequestOk;

    public void setData(ArrayList<ProjectObject> data, boolean requestOk) {
        mData = data;
        mRequestOk = requestOk;
    }

    @ViewById
    ListView listView;

    @ViewById
    View blankLayout;

    @ViewById
    SwipeRefreshLayout swipeRefreshLayout;

    MyAdapter myAdapter = new MyAdapter();

    @AfterViews
    protected void init() {
        super.init();
        listView.setAdapter(myAdapter);

//        if (mData.size() == 0) {
//            layout.setVisibility(View.VISIBLE);
//            if (mRequestOk) {
//                layout.findViewById(R.id.icon).setBackgroundResource(R.drawable.ic_exception_blank_task);
//                ((TextView) layout.findViewById(R.id.message)).setText("您还没有项目\n快去coding网站创建吧");
//            } else {
//                layout.findViewById(R.id.icon).setBackgroundResource(R.drawable.ic_exception_no_network);
//                ((TextView) layout.findViewById(R.id.message)).setText("获取数据失败\n请检查下网络是否通畅");
//            }
//        } else {
//            layout.setVisibility(View.GONE);
//        }

        if (AccountInfo.isCacheProjects(getActivity())) {
            BlankViewDisplay.setBlank(mData.size(), this, mRequestOk, blankLayout, null);
        }
    }

//    View.OnClickListener onClickRetry = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            onRefresh();
//        }
//    };

    @Override
    public void onRefresh() {
        ((SwipeRefreshLayout.OnRefreshListener) getParentFragment()).onRefresh();
    }

    public void setRead(String id) {
        for (int i = 0; i < mData.size(); ++i) {
            if (mData.get(i).id.equals(id)) {
                mData.get(i).un_read_activities_count = 0;
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                break;
            }
        }
    }

    final String HOST_VISTIT = Global.HOST + "/api/project/%s/update_visit";

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_VISTIT)) {
            if (respanse.getInt("code") == 0) {
                String id = (String) data;
                ((UpdateData) getParentFragment()).updateRead(id);

                UnreadNotify.update(getActivity());
            }
        }
    }

    @ItemClick
    public void listView(ProjectObject item) {
        if (item.un_read_activities_count > 0) {
            String s = String.format(HOST_VISTIT, item.id);
            getNetwork(s, HOST_VISTIT, 0, item.id);
        }

        Intent intent = new Intent(getActivity(), ProjectActivity_.class);
        intent.putExtra("mProjectObject", item);
        getActivity().startActivity(intent);
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
                holder.name = (TextView) view.findViewById(R.id.name);
                holder.image = (ImageView) view.findViewById(R.id.icon);
                holder.content = (TextView) view.findViewById(R.id.comment);
                holder.badge = (BadgeView) view.findViewById(R.id.badge);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ProjectObject item = (ProjectObject) getItem(position);

            holder.name.setText(item.name);
            holder.content.setText(item.owner_user_name);
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
    }

    ;

    private static class ViewHolder {
        TextView name;
        ImageView image;
        TextView content;
        BadgeView badge;
    }

    public interface UpdateData {
        void updateRead(String id);
    }
}
