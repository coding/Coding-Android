package net.coding.program.project;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.event.EventRefresh;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.init.create.ProjectCreateActivity_;
import net.coding.program.route.BlankViewDisplay;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

@EFragment(R.layout.project_list_fragment)
public class ProjectListFragment extends RefreshBaseFragment implements View.OnClickListener, ProjectActionUtil.OnSettingListener {

    private static final String TAG = ProjectListFragment.class.getSimpleName();
    private final String URL_PIN_DELETE = Global.HOST_API + "/user/projects/pin?ids=%d";
    private final String URL_PIN_SET = Global.HOST_API + "/user/projects/pin";
    @FragmentArg
    ArrayList<ProjectObject> mData = new ArrayList<>();
    ArrayList<ProjectObject> mDataBackup = new ArrayList<>();

    @FragmentArg
    ProjectFragment.Type type = ProjectFragment.Type.Main;

    @ViewById
    StickyListHeadersListView listView;

    boolean mRequestOk;
    @ViewById
    View blankLayout;
    @ViewById
    Button btn_action;
    @ViewById
    SwipeRefreshLayout swipeRefreshLayout;
    @ViewById
    RelativeLayout project_create_layout;
    MyAdapter myAdapter = null;
    int msectionId = 0;
    private ProjectActionUtil projectActionUtil;
    private String title = "";
    private int pos = 0;
    private View.OnClickListener mOnClickRetry = v -> onRefresh();

    public void setData(ArrayList<ProjectObject> data, boolean requestOk) {
        mData = data;
        mDataBackup = data;
        mRequestOk = requestOk;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public void setDataAndUpdate(ArrayList<ProjectObject> data) {
        mRequestOk = true;
        mData = data;
        mDataBackup = data;
        myAdapter.notifyDataSetChanged();
        // 不让空白画面出现
        BlankViewDisplay.setBlank(1, this, true, blankLayout, mOnClickRetry);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @AfterViews
    protected final void initProjectListFragment() {
        projectActionUtil = new ProjectActionUtil(getActivity().getBaseContext());
        projectActionUtil.setListener(this);
        initRefreshLayout();
        btn_action.setOnClickListener(this);
        msectionId = 0;
        for (ProjectObject item : mData) {
            if (!item.isPin()) {
                break;
            }
            ++msectionId;
        }

        View listViewFooter = getActivity().getLayoutInflater().inflate(R.layout.divide_bottom_15, listView.getWrappedList(), false);
        listView.addFooterView(listViewFooter, null, false);

        if (myAdapter == null) {
            myAdapter = new MyAdapter();
        }
        listView.setAdapter(myAdapter);
        if (getParentFragment() == null) { // 搜索
            disableRefreshing();
        }
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ProjectObject item = (ProjectObject) myAdapter.getItem(position);
            listView(item);
        });
    }

    private void notifyEmputy() {
        if (mData.size() == 0) {
            project_create_layout.setVisibility(View.VISIBLE);
            btn_action.setText("+  去创建");
        } else {
            project_create_layout.setVisibility(View.GONE);
        }
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    // 用于处理推送
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRefresh(EventRefresh event) {
        if (event.refresh) {
            notifyEmputy();
        }
    }

    @Override
    public void onRefresh() {
        EventBus.getDefault().post(new EventProjectModify());
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(URL_PIN_SET)) {
            if (code == 0) {
                setPin((int) data, true);
                EventBus.getDefault().post(new EventProjectModify());

                umengEvent(UmengEvent.PROJECT, "设为常用");
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(URL_PIN_DELETE)) {
            if (code == 0) {
                setPin((int) data, false);
                EventBus.getDefault().post(new EventProjectModify());

                umengEvent(UmengEvent.PROJECT, "取消常用");
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void setPin(int id, boolean pin) {
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

    void listView(ProjectObject item) {
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

        if (type == ProjectFragment.Type.Main || type == ProjectFragment.Type.Create) {
            ProjectJumpParam param = new ProjectJumpParam(item.project_path);
            ProjectHomeActivity_.intent(fragment).mJumpParam(param).start();
        } else {
            Intent intent = new Intent();
            intent.putExtra("data", item);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
    }

    @Override
    public void doAction(int pos) {
        final int projectId = mData.get(pos).getId();
        if (mData.get(pos).isPin()) {
            String pinDeleteUrl = String.format(URL_PIN_DELETE, projectId);
            deleteNetwork(pinDeleteUrl, URL_PIN_DELETE, -1, projectId);
        } else {
            RequestParams params = new RequestParams();
            params.put("ids", projectId);
            postNetwork(URL_PIN_SET, params, URL_PIN_SET, -1, projectId);
        }

        projectActionUtil.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_action:
                ProjectCreateActivity_.intent(this).start();
                break;
        }
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
        TextView name2;
        TextView content;
        TextView desc;
        BadgeView badge;
        View privateIcon;
        ImageView privatePin;
        View fLayoutAction;
    }

    class MyAdapter extends BaseAdapter implements StickyListHeadersAdapter {
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
                holder.fLayoutAction = view.findViewById(R.id.flayoutAction);
                holder.desc = (TextView) view.findViewById(R.id.txtDesc);
                holder.privatePin = (ImageView) view.findViewById(R.id.privatePin);
                holder.name2 = (TextView) view.findViewById(R.id.name2);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ProjectObject item = (ProjectObject) getItem(position);

            holder.privatePin.setVisibility(item.isPin() ? View.VISIBLE : View.INVISIBLE);
            holder.privateIcon.setVisibility(!item.isShared() ? View.INVISIBLE : View.VISIBLE);
            String ownerName = item.owner_user_name;
            holder.content.setText(ownerName);
            if (item.isShared()) {
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(item.name);
                holder.name2.setVisibility(View.INVISIBLE);
            } else {
                holder.name2.setVisibility(View.VISIBLE);
                holder.name2.setText(item.name);
                holder.name.setVisibility(View.INVISIBLE);
            }
            holder.desc.setText(item.getDescription());
            setClickEvent(holder.fLayoutAction, position);
            if (type == ProjectFragment.Type.Pick) {
                holder.badge.setVisibility(View.INVISIBLE);
            } else {
                int count = item.unReadActivitiesCount;
                BadgeView badge = holder.badge;
                Global.setBadgeView(badge, count);
            }
            if (pos == 0) {
                holder.fLayoutAction.setVisibility(View.VISIBLE);
            } else {
                holder.privatePin.setVisibility(View.INVISIBLE);
            }
            iconfromNetwork(holder.image, item.icon, ImageLoadTool.optionsRounded2);

            return view;
        }

        private void setClickEvent(final View fLayoutAction, final int position) {
            fLayoutAction.setOnClickListener(v -> {
                projectActionUtil.show(fLayoutAction, position);
                if (!mData.get(position).isPin()) {
                    projectActionUtil.getTxtSetting().setText("设为常用");
                    projectActionUtil.getTxtSetting().setTextColor(getActivity().getResources().getColor(R.color.white));
                    projectActionUtil.getTxtSetting().setBackgroundColor(getActivity().getResources().getColor(R.color.font_green));
                } else {
                    projectActionUtil.getTxtSetting().setText("取消常用");
                    projectActionUtil.getTxtSetting().setTextColor(getActivity().getResources().getColor(R.color.font_green));
                    projectActionUtil.getTxtSetting().setBackgroundColor(getActivity().getResources().getColor(R.color.divide));
                }
            });
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new View(parent.getContext());
            }
            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            return -1;
        }
    }
}
