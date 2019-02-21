package net.coding.program.project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.quickAdapter.easyRegularAdapter;
import com.readystatesoftware.viewbadger.BadgeView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.event.EventRefresh;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.ui.shadow.RecyclerViewSpace;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.PermissionUtil;
import net.coding.program.message.MessageListActivity_;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.Pager;
import net.coding.program.project.init.create.ProjectCreateActivity_;
import net.coding.program.route.BlankViewDisplay;
import net.coding.program.task.add.TaskAddActivity_;
import net.coding.program.user.UsersListActivity;
import net.coding.program.util.TextWatcherAt;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EFragment(R.layout.fragment_enterprise_project)
public class EnterpriseProjectFragment extends BaseFragment {

    private final String URL_PIN_DELETE = Global.HOST_API + "/user/projects/pin?ids=%s";
    private final String URL_PIN_SET = Global.HOST_API + "/user/projects/pin";

    private final int RESULT_SELECT_USER = 2001;

    int msectionId = 0;

    @ViewById
    UltimateRecyclerView listView;

    @ViewById
    View blankLayout;

    @ViewById
    Toolbar enterpriseProjectToolbar;

    @ViewById
    View toolbarTitle;

    @ViewById
    View topTip, closeTipButton;

    @ViewById
    TextView topTipText;

    ArrayList<ProjectObject> listData = new ArrayList<>();
    ArrayList<ProjectObject> allListData = new ArrayList<>();

    ProjectAdapter projectAdapter;

    private Comparator<ProjectObject> comparator = (lhs, rhs) -> {
        if (lhs.isPin() && !rhs.isPin()) {
            return -1;
        } else if (!lhs.isPin() && rhs.isPin()) {
            return 1;
        } else {
            return 0;
        }
    };

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @AfterViews
    void initEnterpriseProjectFramgent() {
        initActionBar();

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(manager);
        RecyclerViewSpace space = new RecyclerViewSpace(getActivity());
        listView.addItemDecoration(space);
        listView.setEmptyView(R.layout.fragment_enterprise_project_empty,
                R.layout.fragment_enterprise_project_empty);

        listView.setDefaultOnRefreshListener(() -> onRefresh());
        listView.mSwipeRefreshLayout.setColorSchemeResources(R.color.font_green);
        listView.setOnLoadMoreListener((itemsCount, maxLastVisiblePosition) -> loadMore());

        allListData.clear();
        listData.clear();
        allListData.addAll(AccountInfo.loadProjects(getActivity()));
        listData.addAll(allListData);

        projectAdapter = new ProjectAdapter(listData);
        listView.setAdapter(projectAdapter);

        projectActionUtil = new ProjectActionUtil(getActivity().getBaseContext());
        projectActionUtil.setListener(pos -> {
            ProjectObject project = listData.get(pos);
            final int projectId = project.getId();
            if (project.isPin()) {
                String pinDeleteUrl = String.format(URL_PIN_DELETE, projectId);
                deleteNetwork(pinDeleteUrl, URL_PIN_DELETE, -1, project);
            } else {
                RequestParams params = new RequestParams();
                params.put("ids", projectId);
                postNetwork(URL_PIN_SET, params, URL_PIN_SET, -1, project);
            }

            projectActionUtil.close();
        });
        onRefresh();

        setHasOptionsMenu(true);

        if (GlobalData.isPrivateEnterprise()) {
            topTip.setVisibility(View.GONE);
        } else {
            soldOutTip();
        }
    }

    private void soldOutTip() {
        topTip.setVisibility(View.VISIBLE);
        closeTipButton.setOnClickListener(v -> topTip.setVisibility(View.GONE));
        topTipText.setText(R.string.sold_out_app);
    }

    private void initActionBar() {
        enterpriseProjectToolbar.inflateMenu(R.menu.enterprise_main_project);
        enterpriseProjectToolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.actionCreateProject:
                    actionCreateProject();
                    break;
                case R.id.actionCreateTask:
                    actionCreateTask();
                    break;
                case R.id.actionSendMessage:
                    actionSendMessage();
                    break;
                case R.id.action2fa:
                    action2fa();
                    break;
            }
            return true;
        });

        SearchView searchView = enterpriseProjectToolbar.findViewById(R.id.searchBar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchItem(s);
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            toolbarTitle.setVisibility(View.VISIBLE);
            return false;
        });
        searchView.setOnSearchClickListener(v -> toolbarTitle.setVisibility(View.GONE));
    }

    void actionCreateProject() {
        ProjectCreateActivity_.intent(this).start();
    }

    void actionCreateTask() {
        TaskAddActivity_.intent(this).mUserOwner(GlobalData.sUserObject).start();
    }

    void actionSendMessage() {
        TextWatcherAt.startActivityAt(getActivity(), this, RESULT_SELECT_USER);
    }

    @SuppressLint("CheckResult")
    void action2fa() {
        new RxPermissions(getActivity())
                .request(PermissionUtil.CAMERA_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        GlobalCommon.start2FAActivity(getActivity());
                    }
                });
    }

    private void onRefresh() {
        Network.getRetrofit(getContext())
                .getProjects()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Pager<ProjectObject>>(getActivity()) {
                    @Override
                    public void onSuccess(Pager<ProjectObject> data) {
                        super.onSuccess(data);

                        allListData.clear();
                        listData.clear();
                        int pinCount = 0;
                        for (ProjectObject item : data.list) {
                            if (item.isPin()) {
                                allListData.add(pinCount++, item);
                            } else {
                                allListData.add(item);
                            }
                        }
                        listData.addAll(allListData);

                        msectionId = 0;
                        for (ProjectObject item : listData) {
                            if (!item.isPin()) {
                                break;
                            }
                            ++msectionId;
                        }

                        AccountInfo.saveProjects(getActivity(), allListData);
                        projectAdapter.notifyDataSetChanged();

                        BlankViewDisplay.setBlank(allListData.size(), this, true, blankLayout, mOnClickRetry);

                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        projectAdapter.notifyDataSetChanged();
                        BlankViewDisplay.setBlank(allListData.size(), this, true, blankLayout, mOnClickRetry);
                    }
                });
    }

    private View.OnClickListener clickItem = v -> {
        Object object = v.getTag();
        if (object instanceof ProjectObject) {
            ProjectObject item = (ProjectObject) object;
            ProjectHomeActivity_.intent(this)
                    .mProjectObject(item)
                    .start();
        }
    };

    // 用于处理推送
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRefresh(EventRefresh event) {
        if (event.refresh) {
            onRefresh();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventProjectModify(EventProjectModify event) {
        onRefresh();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {


//        searchItem.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void searchItem(String s) {
        s = s.toLowerCase();
        projectAdapter.notifyDataSetChanged();

        listData.clear();
        if (TextUtils.isEmpty(s)) {
            listData.addAll(allListData);
        } else {
            for (ProjectObject item : allListData) {
                if (item.name.toLowerCase().contains(s)) {
                    listData.add(item);
                }
            }
        }

        projectAdapter.notifyDataSetChanged();
    }

    View.OnClickListener mOnClickRetry = v -> onRefresh();

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(URL_PIN_SET)) {
            if (code == 0) {
                umengEvent(UmengEvent.E_PROJECT, "设为常用");
                updatePin((ProjectObject) data, true);
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(URL_PIN_DELETE)) {
            if (code == 0) {
                umengEvent(UmengEvent.E_PROJECT, "取消常用");
                updatePin((ProjectObject) data, false);
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void updatePin(ProjectObject project, boolean pin) {
        project.setPin(pin);

        Collections.sort(allListData, comparator);
        searchItem("");
    }

    @Override
    public void loadMore() {
    }

    @OnActivityResult(RESULT_SELECT_USER)
    void onSelectUser(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            UserObject user = (UserObject) data.getSerializableExtra(UsersListActivity.RESULT_EXTRA_USESR);
            if (user != null) {
                MessageListActivity_.intent(this).mUserObject(user).start();
            }
        }
    }

    protected class ProjectAdapter extends easyRegularAdapter<ProjectObject, ProjectHolder> {

        public ProjectAdapter(List<ProjectObject> list) {
            super(list);
        }

        @Override
        protected int getNormalLayoutResId() {
            return R.layout.enterprise_project_all_list_item;
        }

        @Override
        protected ProjectHolder newViewHolder(View view) {
            view.setOnClickListener(clickItem);
            return new ProjectHolder(view);
        }

        @Override
        protected void withBindHolder(ProjectHolder holder, ProjectObject data, int position) {
            ProjectObject item = getItem(position);

            holder.privatePin.setVisibility(item.isPin() ? View.VISIBLE : View.INVISIBLE);
            String ownerName = item.owner_user_name;

            holder.name.setVisibility(View.VISIBLE);
            holder.name.setText(item.name);

            holder.desc.setText(item.getDescription());
            setClickEvent(holder.fLayoutAction, position);
            int count = item.unReadActivitiesCount;
            BadgeView badge = holder.badge;
            Global.setBadgeView(badge, count);

            holder.fLayoutAction.setVisibility(View.VISIBLE);
            holder.rootLayout.setTag(item);
            iconfromNetwork(holder.image, item.icon, ImageLoadTool.optionsRounded2);
        }

        @Override
        public ProjectHolder newHeaderHolder(View view) {
            return new ProjectHolder(view, true);
        }

        @Override
        public ProjectHolder newFooterHolder(View view) {
            return new ProjectHolder(view, true);
        }
    }

    private ProjectActionUtil projectActionUtil;

    private void setClickEvent(final View fLayoutAction, final int position) {
        fLayoutAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                projectActionUtil.show(fLayoutAction, position);
                if (!listData.get(position).isPin()) {
                    projectActionUtil.getTxtSetting().setText("设为常用");
                    projectActionUtil.getTxtSetting().setTextColor(getActivity().getResources().getColor(R.color.white));
                    projectActionUtil.getTxtSetting().setBackgroundColor(getActivity().getResources().getColor(R.color.font_green));
                } else {
                    projectActionUtil.getTxtSetting().setText("取消常用");
                    projectActionUtil.getTxtSetting().setTextColor(getActivity().getResources().getColor(R.color.font_green));
                    projectActionUtil.getTxtSetting().setBackgroundColor(getActivity().getResources().getColor(R.color.divide));
                }
            }
        });
    }

    public static class ProjectHolder extends UltimateRecyclerviewViewHolder {

        public ImageView image;
        public TextView name;
        public TextView desc;
        public BadgeView badge;
        public ImageView privatePin;
        public View fLayoutAction;
        public View rootLayout;

        public ProjectHolder(View view, boolean isHeader) {
            super(view);
        }

        public ProjectHolder(View view) {
            super(view);
            rootLayout = view;
            image = (ImageView) view.findViewById(R.id.icon);
            badge = (BadgeView) view.findViewById(R.id.badge);
            fLayoutAction = view.findViewById(R.id.flayoutAction);
            desc = (TextView) view.findViewById(R.id.txtDesc);
            privatePin = (ImageView) view.findViewById(R.id.privatePin);
            name = (TextView) view.findViewById(R.id.name);
        }
    }
}
