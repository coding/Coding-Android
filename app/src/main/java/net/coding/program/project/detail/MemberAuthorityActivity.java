package net.coding.program.project.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.constant.MemberAuthority;
import net.coding.program.network.model.user.Member;
import net.coding.program.project.member.MemberAuthorityManualActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_member_authority)
@OptionsMenu(R.menu.member_authority)
public class MemberAuthorityActivity extends BackActivity {

    @Extra
    MemberAuthority authority;

    @Extra
    String globayKey;

    @Extra
    Member me;

    @Extra
    int projectId;

    @ViewById
    DropdownListItemView projectNo, projectManager, projectMember, projectMemberLimited;

    @ViewById
    View managerDivide, divideNo;

    @AfterViews
    void initMemberAuthorityActivity() {
        projectManager.setText("管理员");
        projectMember.setText("项目成员");
        projectMemberLimited.setText("受限成员");

        projectManager.setChecked(false);
        projectMember.setChecked(false);
        projectMemberLimited.setChecked(false);


        if (!GlobalData.isEnterprise()) {
            projectNo.setVisibility(View.GONE);
            divideNo.setVisibility(View.GONE);

            // coding 需要传入 me 的信息，企业版不需要
            if (me.getType() == MemberAuthority.manager) {
                projectManager.setVisibility(View.GONE);
                managerDivide.setVisibility(View.GONE);
            }
        } else {
            projectNo.setVisibility(View.VISIBLE);
            divideNo.setVisibility(View.VISIBLE);
            projectNo.setChecked(false);
            projectNo.setText("无");
        }

        switch (authority) {
            case manager:
                projectManager.setChecked(true);
                break;
            case member:
                projectMember.setChecked(true);
                break;
            case limited:
                projectMemberLimited.setChecked(true);
                break;
            case noJoin:
                projectNo.setChecked(true);
                break;
        }
    }

    @Click
    void projectManager() {
        modifyAuthority(MemberAuthority.manager.getType());
    }

    @Click
    void projectMember() {
        modifyAuthority(MemberAuthority.member.getType());
    }

    @Click
    void projectMemberLimited() {
        modifyAuthority(MemberAuthority.limited.getType());
    }

    @Click
    void projectNo() {
        modifyAuthority(MemberAuthority.noJoin.getType());
    }

    private void modifyAuthority(int id) {
        if (!GlobalData.isEnterprise()) {
            modifyCoding(id);
        } else {
            modifyEnterprise(id);
        }
    }

    private void modifyCoding(int id) {
        String url = String.format(Global.HOST_API + "/project/%s/member/%s/%s", projectId, globayKey, id);
        MyAsyncHttpClient.post(this, url, new MyJsonResponse(MemberAuthorityActivity.this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                showProgressBar(false);
            }
        });
        showProgressBar(true);
    }

    private void modifyEnterprise(int id) {
        String projects = String.valueOf(projectId);
        String roles = String.valueOf(id);
        Network.getRetrofit(this)
                .setUserJoinedProjects(GlobalData.getEnterpriseGK(), globayKey, projects, roles)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpObserver(this) {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        umengEvent(UmengEvent.E_USER_CENTER, "企业项目权限设置");

                        Intent intent = new Intent();
                        intent.putExtra("intentData", id);
                        intent.putExtra("intentData1", projectId);
                        setResult(RESULT_OK, intent);

                        finish();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        showProgressBar(false);
                    }
                });
        showProgressBar(true);
    }

    @OptionsItem
    void action_about() {
        if (!GlobalData.isEnterprise()) {
            showPop("项目所有者：拥有对项目的所有权限。\n" +
                    "项目管理员：拥有对项目的部分权限。不能删除，转让项目，不能对其他管理员进行操作。\n" +
                    "普通成员：可以阅读和推送代码。\n" +
                    "受限成员：不能进入与代码相关的页面。");
        } else {
            MemberAuthorityManualActivity_.intent(this).start();
        }
    }

    private void showPop(String s) {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment newFragment = MyDialogFragment.newInstance(s);
        newFragment.show(ft, "dialog");
    }

    public static class MyDialogFragment extends DialogFragment {
        String mStringData;

        static MyDialogFragment newInstance(String data) {
            MyDialogFragment f = new MyDialogFragment();

            // Supply data input as an argument.
            Bundle args = new Bundle();
            args.putString("data", data);
            f.setArguments(args);

            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mStringData = getArguments().getString("data", "");
            setStyle(DialogFragment.STYLE_NORMAL, R.style.MyDialog);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.skill_workexp_tip, container, false);
            TextView textView = (TextView) v.findViewById(R.id.tipText);
            textView.setText(mStringData);
            v.setOnClickListener(v1 -> getActivity().getSupportFragmentManager().popBackStack());

            return v;
        }
    }
}
