package net.coding.program.project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.CustomDialog;
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
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

@EFragment(R.layout.project_list_fragment)
public class ProjectListFragment extends RefreshBaseFragment {

    private static final String URL_PIN_DELETE = Global.HOST_API + "/user/projects/pin?ids=%d";
    private static final String URL_PIN_SET = Global.HOST_API + "/user/projects/pin";
    @FragmentArg
    ArrayList<ProjectObject> mData = new ArrayList<>();
    ArrayList<ProjectObject> mDataBackup = new ArrayList<>();
    @FragmentArg
    ProjectFragment.Type type = ProjectFragment.Type.Main;

    boolean mRequestOk;
    @ViewById
    ExpandableStickyListHeadersListView listView;

    @ViewById
    View blankLayout;

    @ViewById
    SwipeRefreshLayout swipeRefreshLayout;

    MyAdapter myAdapter = new MyAdapter();
    int msectionId = 0;
    private View.OnClickListener mOnClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    public void setData(ArrayList<ProjectObject> data, boolean requestOk) {
        mData = data;
        mDataBackup = data;
        mRequestOk = requestOk;
    }

    public void setDataAndUpdate(ArrayList<ProjectObject> data) {
        mRequestOk = true;
        mData = data;
        mDataBackup = data;
        myAdapter.notifyDataSetChanged();
        // 不让空白画面出现
        BlankViewDisplay.setBlank(1, this, true, blankLayout, mOnClickRetry);
    }

    @AfterViews
    protected final void init() {
        initRefreshLayout();
        msectionId = 0;
        for (ProjectObject item : mData) {
            if (!item.isPin()) {
                break;
            }
            ++msectionId;
        }

        View listViewFooter = getActivity().getLayoutInflater().inflate(R.layout.divide_15_top, null);
        listView.addFooterView(listViewFooter);
        listView.setAdapter(myAdapter);

        if (getParentFragment() == null) { // 搜索
            disableRefreshing();
        }

        if (AccountInfo.isCacheProjects(getActivity())) {
            BlankViewDisplay.setBlank(mData.size(), this, mRequestOk, blankLayout, mOnClickRetry);
        }
    }

    @ItemLongClick
    protected final void listView(int pos) {
        final int projectId = mData.get(pos).getId();

        AlertDialog.Builder builder = new AlertDialog.Builder(listView.getContext());
        if (mData.get(pos).isPin()) {
            builder.setItems(R.array.project_pin_cannel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        String pinDeleteUrl = String.format(URL_PIN_DELETE, projectId);
                        deleteNetwork(pinDeleteUrl, URL_PIN_DELETE, -1, projectId);
                    }
                }
            });
        } else {
            builder.setItems(R.array.project_pin, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        RequestParams params = new RequestParams();
                        params.put("ids", projectId);
                        postNetwork(URL_PIN_SET, params, URL_PIN_SET, -1, projectId);
                    }
                }
            });
        }
        AlertDialog dialog = builder.setCancelable(true).show();
        CustomDialog.dialogTitleLineColor(listView.getContext(), dialog);
    }

    @Override
    public void onRefresh() {
        ((SwipeRefreshLayout.OnRefreshListener) getParentFragment()).onRefresh();
    }

    public void setRead(int id) {
        for (int i = 0; i < mData.size(); ++i) {
            if (mData.get(i).getId() == id) {
                mData.get(i).un_read_activities_count = 0;
                myAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public void setPin(int id, boolean pin) {
        for (int i = 0; i < mData.size(); ++i) {
            if (mData.get(i).getId() == id) {
                mData.get(i).setPin(pin);

                ProjectObject item = mData.remove(i);
                if (pin) {
                    ++msectionId;
                    mData.add(0, item);
                } else {
                    --msectionId;
                    mData.add(msectionId, item);
                }
                myAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(URL_PIN_SET)) {
            if (code == 0) {
                int id = (int) data;
                ((UpdateData) getParentFragment()).updatePin(id, true);
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(URL_PIN_DELETE)) {
            if (code == 0) {
                int id = (int) data;
                ((UpdateData) getParentFragment()).updatePin(id, false);
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @ItemClick
    public void listView(ProjectObject item) {
//        if (item.un_read_activities_count > 0) {
        // 调用此函数，则按hot排序时项目会排序到有动态的项目后面
//        String s = String.format(HOST_VISTIT, item.getId());
//        getNetwork(s, HOST_VISTIT, 0, item.getId());
//        }

        // 在搜索界面不是嵌套的，getParentFragment会返回null
        Fragment fragment = getParentFragment();
        if (fragment == null) {
            fragment = this;
        }

        if (type == ProjectFragment.Type.Main) {
            ProjectHomeActivity_.intent(fragment).mProjectObject(item).startForResult(InitProUtils.REQUEST_PRO_UPDATE);
        } else {
            Intent intent = new Intent();
            intent.putExtra("data", item);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == InitProUtils.REQUEST_PRO_UPDATE) {
            if (resultCode == Activity.RESULT_OK) {
                String action = data.getStringExtra("action");
                if (action.equals(InitProUtils.FLAG_REFRESH)) {
                    onRefresh();
                } else if (action.equals(InitProUtils.FLAG_UPDATE_DYNAMIC)) {
                    int projectId = data.getIntExtra("projectId", 0);
                    if (projectId != 0) {
                        Fragment parentFragment = getParentFragment();
                        FragmentActivity activity = getActivity();
                        if ((parentFragment instanceof UpdateData)
                                && (activity != null)) { // TODO parentFragment 可能为null，因为parent被销毁了还没有恢复
                            ((UpdateData) parentFragment).updateRead(projectId);
                            UnreadNotify.update(activity);
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public interface UpdateData {
        void updateRead(int id);

        void updatePin(int id, boolean pin);
    }

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
            String ownerName = item.isPublic() ? item.owner_user_name : ("      " + item.owner_user_name);
            holder.content.setText(ownerName);

            if (type == ProjectFragment.Type.Pick) {
                holder.badge.setVisibility(View.INVISIBLE);
            } else {
                int count = item.un_read_activities_count;
                BadgeView badge = holder.badge;
                Global.setBadgeView(badge, count);
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
}
