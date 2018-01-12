package net.coding.program.setting;


import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.UserDetailEditActivity_;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.model.user.ServiceInfo;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.util.PermissionUtil;
import net.coding.program.common.widget.ListItem1;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.mall.MallIndexActivity_;
import net.coding.program.project.detail.file.LocalProjectFileActivity_;
import net.coding.program.user.AddFollowActivity_;
import net.coding.program.user.UserPointActivity_;
import net.coding.program.user.team.TeamListActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EFragment(R.layout.fragment_main_setting)
public class MainSettingFragment extends BaseFragment {

    private static final String TAG_SERVICE_INFO = "TAG_SERVICE_INFO";
    final String url = Global.HOST_API + "/user/service_info";

    @ViewById
    TextView userName, userGK, projectCount, teamCount;

    @ViewById
    Toolbar mainSettingToolbar;

    ServiceInfo serviceInfo;

    @ViewById(R.id.itemShop)
    ListItem1 itemShop;

    @ViewById
    ImageView userIcon;

    @ViewById(R.id.topTip)
    View topTip;

    @AfterViews
    void initMainSettingFragment() {
        initMenuItem();

        // 企业版没有商城
        if (itemShop != null) {
            itemShop.showBadge(RedPointTip.show(getActivity(), RedPointTip.Type.SettingShop_P460));
        }

        bindDataUserinfo();
    }

    protected void initMenuItem() {
        mainSettingToolbar.inflateMenu(R.menu.main_setting);
        mainSettingToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.actionAddFollow) {
                actionAddFollow();
            }
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        loadUser();
        bindDataUserinfo();
    }

    private void bindDataUserinfo() {
        UserObject me = GlobalData.sUserObject;
        userName.setText(me.name);
        userGK.setText(String.format("个性后缀：%s", me.global_key));
        iconfromNetwork(userIcon, me.avatar);
        userIcon.setTag(me);

        if (GlobalData.isEnterprise() || me.isFillInfo() || me.isHighLevel()) {
            if (topTip != null) {
                topTip.setVisibility(View.GONE);
            }
        }
    }

    private void bindData() {
        if (serviceInfo == null) {
            serviceInfo = new ServiceInfo(AccountInfo.getGetRequestCacheData(getActivity(), url));
        }

        projectCount.setText(String.valueOf(serviceInfo.publicProject + serviceInfo.privateProject));
        teamCount.setText(String.valueOf(serviceInfo.team));
    }

    void loadUser() {
        getNetwork(url, TAG_SERVICE_INFO);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_SERVICE_INFO)) {
            if (code == 0) {
                serviceInfo = new ServiceInfo(respanse.optJSONObject("data"));
                bindData();
            } else {
                showErrorMsg(code, respanse);
            }
        }
        super.parseJson(code, respanse, tag, pos, data);
    }

    @Click
    void projectLayout() {
        MyCreateProjectListActivity_.intent(this).start();
    }

    @Click
    void teamLayout() {
        TeamListActivity_.intent(this).start();
    }

    @Click
    void itemAccount() {
        UserPointActivity_.intent(this).start();
    }

    @Click
    void itemShop() {
        RedPointTip.markUsed(getActivity(), RedPointTip.Type.SettingShop_P460);
        itemShop.showBadge(false);

        MallIndexActivity_.intent(this).start();
    }

    @Click
    void itemLocalFile() {
        if (!PermissionUtil.writeExtralStorage(getActivity())) {
            return;
        }

        LocalProjectFileActivity_.intent(this).start();
    }

    @Click
    void itemHelp() {
        final String url = "https://coding.net/help/doc/mobile";
        String title = getString(R.string.title_activity_help);
        HelpActivity_.intent(this).url(url).title(title).start();
    }

    @Click
    void userLayout() {
        CodingCompat.instance().launchMyDetailActivity(getActivity());
    }

    @Click
    void itemSetting() {
        SettingActivity_.intent(this).start();
    }

    @Click
    void itemAbout() {
        AboutActivity_.intent(this).start();
    }

    @Click
    void topTipText() {
        UserDetailEditActivity_
                .intent(this)
                .start();
    }

    @Click
    void closeTipButton() {
        topTip.setVisibility(View.GONE);
    }

    void actionAddFollow() {
        AddFollowActivity_.intent(this).start();
    }

}
